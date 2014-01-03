/**
 * PoolHandler.java
 *
 * Copyright 2012 The original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.niolex.network.cli;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.niolex.network.rpc.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The basic client implementation is {@link org.apache.niolex.network.client.SocketClient}
 * which is simple, and correct proven. But it can not be used in multiple threading
 * environment.
 * <br>
 * The {@link org.apache.niolex.network.client.PacketClient} can be used in multiple threads,
 * but it requires two threads for read and write. So in the multi-core server side environment,
 * we need to have a pool to manage large set of connections.
 * <br>
 * This PoolHandler is based on the {@link org.apache.niolex.network.client.SocketClient},
 * using an internal pool to make it working in multiple threading environment.
 * <br>
 * We will also handle the retry problem here. But we will leave the SocketClient creation
 * problem for you, because there maybe multiple servers for one service.
 * <br>
 * User can use this class as an InvocationHandler or just use it as a pool. If user want to
 * use it as a pool, see {@link #take()} and {@link #offer(IServiceHandler)}.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5
 * @since 2012-12-5
 */
public class PoolHandler<Service extends IServiceHandler> extends BaseHandler implements InvocationHandler {
    private static final Logger LOG = LoggerFactory.getLogger(PoolHandler.class);

    private final LinkedBlockingQueue<Service> readyQueue = new LinkedBlockingQueue<Service>();
    private final int retryTimes;
    private final int handlerNum;
    private int waitTimeout = Constants.CLIENT_CONNECT_TIMEOUT;

    /**
     * Create a PoolHandler with the specified parameters.
     *
     * @param retryTimes the number of times to retry when some kind of error occurred
     * @param col the collections of {@link IServiceHandler}
     */
    public PoolHandler(int retryTimes, Collection<Service> col) {
        super();
        this.retryTimes = retryTimes;
        handlerNum = col.size();
        readyQueue.addAll(col);
        this.logDebug = LOG.isDebugEnabled();
    }

    /**
     * Use the pool handler as a invocation handler. We will delegate call to service handler.
     * <br>
     * This is the override of super method.
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Service handler;
        Throwable cause = null;
        long handleStart = 0, handleEnd = 500;
        int curTry = 0;
        while (curTry++ < retryTimes) {
            handler = take();
            if (handler == null) {
                throw new RpcException("Failed to service " + method.toString(), RpcException.Type.NO_SERVER_READY, null);
            }
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
            } finally {
                // Return the resource.
                offer(handler);
            }
        } // End of while.
        throw new RpcException("Failed to service " + method.toString() + ": exceeds retry time [" + retryTimes + "].",
                RpcException.Type.ERROR_EXCEED_RETRY, cause);
    }

    /**
     * Retrieves and removes the head of the ready queue, or returns null if the queue is empty.
     *
     * @return an instance of {@link IServiceHandler}, null if all service handlers are busy.
     */
    public Service take() {
        Service core;
        int anyTried = 0;
        while ((core = takeOne(waitTimeout)) != null) {
            // First check the connection status.
            if (core.isReady())
                return core;
            else {
                repair(core);
            }
            // All the connections has been checked.
            if (++anyTried >= handlerNum) {
                return null;
            }
        }
        return null;
    }

    /**
     * Take one IServiceHandler from the ready queue, will return null if can not take out any
     * element at the given timeout.
     * We will not check the status of this instance, so it maybe already broken.
     *
     * @param connectTimeout the timeout to take item from queue.
     * @return an instance of {@link IServiceHandler}, null if timeout.
     */
    protected Service takeOne(int connectTimeout) {
        try {
            return readyQueue.poll(connectTimeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return null;
        }
    }

    /**
     * Repair the broken rpc service handler. If it can not be repaired, you need to close it.
     *
     * @param core the service handler need to be repaired
     */
    protected void repair(Service core) {
        readyQueue.offer(core);
    }

    /**
     * Offer the internal pool a finished rpc service handler.
     *
     * @param core the service handler
     */
    public void offer(Service core) {
        readyQueue.offer(core);
    }

    /**
     * @return get the current rpc handler wait timeout.
     */
    public int getWaitTimeout() {
        return waitTimeout;
    }

    /**
     * Set the time to wait for a rpc handler in milliseconds.
     *
     * @param waitTimeout
     */
    public void setWaitTimeout(int waitTimeout) {
        this.waitTimeout = waitTimeout;
    }

    /**
     * @return the number of times to retry when handler is under network problems.
     */
    public int getRetryTimes() {
        return retryTimes;
    }

}
