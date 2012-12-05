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

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.niolex.network.rpc.RpcClient;
import org.apache.niolex.network.rpc.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The basic client implementation is {@link org.apache.niolex.network.client.SocketClient}
 * which is simple, and correct proven. But it's can not be used in multiple threading
 * environment.
 *
 * The {@link org.apache.niolex.network.client.PacketClient} can be used in multiple threads,
 * but it requires two threads for read and write. So in the multi-core server side environment,
 * we need to have a pool to manage large set of connections.
 *
 * This PoolHandler is based on the {@link org.apache.niolex.network.client.SocketClient},
 * using an internal pool to make it working in multiple threading environment.
 *
 * We will also handle the retry problem here. But we will leave the SocketClient creation
 * problem for you, because there maybe multiple servers for one service.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5
 * @since 2012-12-5
 */
public class PoolHandler implements InvocationHandler {
    private static final Logger LOG = LoggerFactory.getLogger(PoolHandler.class);

    private final LinkedBlockingQueue<RpcClient> readyQueue = new LinkedBlockingQueue<RpcClient>();
    private final int retryTimes;
    private final int handlerNum;
    private int waitTimeout = Constants.CLIENT_CONNECT_TIMEOUT;

    public PoolHandler(int retryTimes, Collection<RpcClient> col) {
        super();
        this.retryTimes = retryTimes;
        handlerNum = col.size();
        readyQueue.addAll(col);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcClient core;
        Throwable cause = null;
        long handleStart = 0, handleEnd = 500;
        int curTry = 0;
        while (curTry < retryTimes) {
            core = take();
            if (core == null) {
                throw new RpcException("Failed to service " + method.getName(), RpcException.Type.NO_SERVER_READY, null);
            }
            try {
                handleStart = System.currentTimeMillis();
                LogContext.serviceUrl(core.getRemoteName());
                Object obj = core.invoke(proxy, method, args);
                handleEnd = System.currentTimeMillis();
                if (LOG.isDebugEnabled()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(LogContext.prefix()).append(" Succeed to invoke handler on [").append(core.getRemoteName());
                    sb.append("] time {").append(handleEnd - handleStart).append("}");
                    LOG.debug(sb.toString());
                }
                return obj;
            } catch (Throwable e) {
                handleEnd = System.currentTimeMillis();
                StringBuilder sb = new StringBuilder();
                sb.append(LogContext.prefix()).append(" Failed to invoke handler on [").append(core.getRemoteName());
                sb.append("] time {").append(handleEnd - handleStart).append("} RETRY ").append(curTry);
                sb.append(" ERRMSG: ").append(e.getMessage());
                LOG.info(sb.toString());
                if (e instanceof RpcException) {
                    RpcException re = (RpcException)e;
                    // For some type of RpcException, we need to retry.
                    switch (re.getType()) {
                        case TIMEOUT:
                        case NOT_CONNECTED:
                        case CONNECTION_CLOSED:
                            continue;
                    }
                }
                if (e.getCause() == null)
                    throw e;
                else if (e.getCause() instanceof IOException)
                    continue;
                cause = e;
            }
        }
        throw new RpcException("Failed to service " + method.getName() + ": exceeds retry time [" + retryTimes + "].",
                RpcException.Type.ERROR_EXCEED_RETRY, cause);
    }

    /**
     * Retrieves and removes the head of the ready queue, or returns null if the queue is empty.
     *
     * @return an instance of RpcClient, null if all clients are busy.
     */
    public RpcClient take() {
        RpcClient core;
        int anyTried = 0;
        while ((core = takeOne(waitTimeout)) != null) {
            // All the connections has been checked.
            if (anyTried++ >= handlerNum) {
                return null;
            }
            if (core.isValid())
                return core;
            else {
                readyQueue.offer(core);
            }
        }
        return null;
    }

    /**
     * Take one RpcClient from the ready queue, will return null if can not take out any
     * element at the given timeout.
     * We will not check the status of this instance, so it maybe already broken.
     *
     * @param connectTimeout the timeout to take item from queue.
     * @return an instance of RpcClient, null if timeout.
     */
    protected RpcClient takeOne(int connectTimeout) {
        try {
            return readyQueue.poll(connectTimeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return null;
        }
    }

    /**
     * @return get the current rpc client wait timeout.
     */
    public int getWaitTimeout() {
        return waitTimeout;
    }

    /**
     * Set the time to wait for a rpc client in milliseconds.
     *
     * @param waitTimeout
     */
    public void setWaitTimeout(int waitTimeout) {
        this.waitTimeout = waitTimeout;
    }

    /**
     * @return the number of times to retry when client is under network problems.
     */
    public int getRetryTimes() {
        return retryTimes;
    }

}
