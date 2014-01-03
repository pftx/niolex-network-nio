/**
 * RetryHandler.java
 *
 * Copyright 2011 Niolex, Inc.
 *
 * Niolex licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.apache.niolex.network.cli;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.niolex.commons.util.SystemUtil;
import org.apache.niolex.network.rpc.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Retry in rpc is very important. Because the network environment is not stable.
 * This class controls retry and interval between retry.
 * <br>
 * This class have an inner list of IServiceHandler, they are the representative of real
 * clients, for example, RpcClients. For Every method call, we randomly pick one IServiceHandler
 * and invoke this method on it. If error occurred, we pick another and retry this method.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0, Date: 2012-5-27
 */
public class RetryHandler extends BaseHandler implements InvocationHandler {
	private static final Logger LOG = LoggerFactory.getLogger(RetryHandler.class);

	private final AtomicInteger idx = new AtomicInteger(0);
	private final List<IServiceHandler> handlers;
	private final int retryTimes;
	private final int intervalBetweenRetry;
	private final int handlerNum;

	/**
	 * The only one Constructor, initialize handlers.
	 *
	 * @param handlers the handlers to be used
	 * @param retryTimes number of times to retry when certain kinds of exceptions occurred
	 * @param intervalBetweenRetry the milliseconds to sleep between retry
	 */
	public RetryHandler(List<IServiceHandler> handlers, int retryTimes, int intervalBetweenRetry) {
		super();
		// This will make the handlers order different in every retry handler instance.
		Collections.shuffle(handlers);
		this.handlers = handlers;
		this.handlerNum = handlers.size();
		this.retryTimes = retryTimes;
		this.intervalBetweenRetry = intervalBetweenRetry;
		this.logDebug = LOG.isDebugEnabled();
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		// If the idx is too large, we restore it to 0.
	    if (idx.get() > Integer.MAX_VALUE - 1000) {
	    	idx.set(0);
	    }
	    IServiceHandler handler;
		Throwable cause = null;
		long handleStart = 0, handleEnd = intervalBetweenRetry;
		int anyTried = 0;
		int curTry = 0;
		while (curTry < retryTimes && anyTried < handlerNum) {
			// Try this.
			handler = handlers.get(idx.getAndIncrement() % handlerNum);
			// Count the tried server number.
			++anyTried;
			if (!handler.isReady()) {
				continue;
			}
			if (handleEnd - handleStart < intervalBetweenRetry) {
			    // We need this to prevent RetryHandler from retry too fast and lower level has not recovered.
			    SystemUtil.sleep(intervalBetweenRetry + handleStart - handleEnd);
			}
			// Ready to try.
			++curTry;
			try {
				handleStart = System.currentTimeMillis();
				LogContext.serviceUrl(handler.getServiceUrl());
				Object obj = handler.invoke(proxy, method, args);
				handleEnd = System.currentTimeMillis();
				if (logDebug) {
					LOG.debug(logInvoke(handler, method, handleEnd - handleStart));
				}
				return obj;
			} catch (Throwable e) {
			    handleEnd = System.currentTimeMillis();
				LOG.info(logError(handler, method, handleEnd - handleStart, curTry, e));
				processException(e, handler);
				cause = e;
			}
		}
		if (anyTried == handlerNum) {
			throw new RpcException("Failed to service " + method.getName(), RpcException.Type.NO_SERVER_READY, null);
		}
		throw new RpcException("Failed to service " + method.getName() + ": exceeds retry time [" + retryTimes + "].",
				RpcException.Type.ERROR_EXCEED_RETRY, cause);
	}

	@Override
	public String toString() {
		return handlers.toString();
	}

	/**
	 * Get the internal list of handlers.
	 *
	 * @return the managed handler list
	 */
	public List<IServiceHandler> getHandlers() {
		return handlers;
	}

}
