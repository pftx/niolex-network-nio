/**
 * RpcServiceHandler.java
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
package org.apache.niolex.network.rpc;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage the Rpc Service Stub, deal with error block time.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-5-27
 */
public class RpcServiceHandler implements IServiceHandler {
	private static final Logger LOG = LoggerFactory.getLogger(RpcServiceHandler.class);

	private final String serviceUrl;
	private final InvocationHandler handler;
	private final int errorBlockTime;
	private long nextWorkTime = -1;

	public RpcServiceHandler(String serviceUrl, InvocationHandler handler, int errorBlockTime, boolean isReady) {
		super();
		this.serviceUrl = serviceUrl;
		this.handler = handler;
		this.errorBlockTime = errorBlockTime;
		if (!isReady)
			notReady(new IOException("Failed to connect when server initialize."));
	}

	/**
	 * This is the override of super method.
	 * @see org.apache.niolex.network.rpc.IServiceHandler#getServiceUrl()
	 */
	@Override
	public String getServiceUrl() {
		return serviceUrl;
	}

	/**
	 * This is the override of super method.
	 * @see org.apache.niolex.network.rpc.IServiceHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		return handler.invoke(proxy, method, args);
	}

	/**
	 * This is the override of super method.
	 * @see org.apache.niolex.network.rpc.IServiceHandler#isReady()
	 */
	@Override
	public boolean isReady() {
		boolean isReady = System.currentTimeMillis() > nextWorkTime;
		if (!isReady && LOG.isDebugEnabled())
			LOG.debug("Server [{}] is not ready for work.", serviceUrl);
		return isReady;
	}

	/**
	 * This is the override of super method.
	 * @see org.apache.niolex.network.rpc.IServiceHandler#notReady(java.io.IOException)
	 */
	@Override
	public void notReady(IOException ioe) {
		nextWorkTime = System.currentTimeMillis() + errorBlockTime;
		LOG.warn("Server [" + serviceUrl + "] has been set to not ready status: " + ioe.getMessage());
	}

	/**
	 * This is the override of super method.
	 * @see org.apache.niolex.network.rpc.IServiceHandler#toString()
	 */
	@Override
	public String toString() {
		return serviceUrl;
	}

	/**
	 * Get the internal handler.
	 * This is the override of super method.
	 * @see org.apache.niolex.network.rpc.IServiceHandler#getHandler()
	 */
	@Override
	public InvocationHandler getHandler() {
		return handler;
	}

}
