/**
 * BaseHandler.java
 *
 * Copyright 2014 the original author or authors.
 *
 * We licenses this file to you under the Apache License, version 2.0
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

import java.io.IOException;
import java.lang.reflect.Method;

import org.apache.niolex.network.cli.handler.IServiceHandler;
import org.apache.niolex.network.rpc.RpcException;

/**
 * The base class of {@link PoolHandler} and {@link RetryHandler}.
 * Handle logging and exception processing.
 *
 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2014-1-3
 */
public abstract class BaseHandler {

    /**
     * For sub-class to decide whether log debug or not.
     */
    protected boolean logDebug;

    /**
     * Log invoke details.
     *
     * @param handler the current service handler
     * @param method the current invocation method
     * @param time the time consumed by this invoke
     * @return the log message
     */
    protected String logInvoke(IServiceHandler handler, Method method, long time) {
        StringBuilder sb = new StringBuilder();
        sb.append(LogContext.prefix()).append(" Succeed to invoke handler on [").append(handler.getServiceUrl());
        sb.append("] time {").append(time).append("}, method {").append(method).append("}");
        return sb.toString();
    }

    /**
     * Log error details.
     *
     * @param handler the current service handler
     * @param method the current invocation method
     * @param time the time consumed by this invoke
     * @param curTry the current time of retry
     * @param e the exception
     * @return the log message
     */
    protected String logError(IServiceHandler handler, Method method, long time, int curTry, Throwable e) {
        StringBuilder sb = new StringBuilder();
        sb.append(LogContext.prefix()).append(" Failed to invoke handler on [").append(handler.getServiceUrl());
        sb.append("] time {").append(time).append("}, method {").append(method);
        sb.append("} RETRY ").append(curTry).append(" ERRMSG: ").append(e.toString());
        return sb.toString();
    }

    /**
     * Process the exception. If it's I/O exception, we invoke {@link IServiceHandler#notReady(IOException)}
     * to notify handler.
     *
     * @param e the exception
     * @param handler the handler
     * @return true if connection problem, false otherwise
     * @throws Throwable if necessary
     */
    protected boolean processException(Throwable e, IServiceHandler handler) throws Throwable {
        if (e instanceof RpcException) {
            RpcException re = (RpcException)e;
            switch (re.getType()) {
                case TIMEOUT:
                case NOT_CONNECTED:
                case CONNECTION_CLOSED:
                    // For some type of RpcException, we need to retry.
                    return true;
                default:
                    // For the others, we just throw.
                    throw re;
            }
        } else if (e instanceof IOException) {
            handler.notReady((IOException)(e));
        } else if (e.getCause() == null) {
            throw e;
        } else if (e.getCause() instanceof IOException) {
            handler.notReady((IOException)(e.getCause()));
        }
        return false;
    }

    /**
     * @return the logDebug
     */
    public boolean isLogDebug() {
        return logDebug;
    }

    /**
     * @param logDebug the logDebug to set
     */
    public void setLogDebug(boolean logDebug) {
        this.logDebug = logDebug;
    }

}
