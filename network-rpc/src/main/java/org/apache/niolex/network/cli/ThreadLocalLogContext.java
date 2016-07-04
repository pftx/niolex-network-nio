/**
 * ThreadLocalLogContext.java
 *
 * Copyright 2016 the original author or authors.
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

/**
 * Store the log context in thread local.
 * 
 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
 * @version 2.1.2
 * @since Jun 29, 2016
 */
public class ThreadLocalLogContext extends LogContext {
    
    private static final ThreadLocalLogContext INSTANCE = new ThreadLocalLogContext();
    
    /**
     * Wire up this ThreadLocalLogContext into LogContext.
     */
    public static final void wireup() {
        LogContext.setInstance(INSTANCE);
    }
    
    /**
     * Get the global instance of this ThreadLocalLogContext.
     * 
     * @return the global instance
     */
    public static final ThreadLocalLogContext instance() {
        return INSTANCE;
    }
    
    private final ThreadLocal<String> logPrefix = new ThreadLocal<String>();
    private final ThreadLocal<String> serviceUrl = new ThreadLocal<String>();

    /**
     * Set the log prefix of the current thread.
     * 
     * @param logPrefixStr the log prefix
     */
    public void setLogPrefix(String logPrefixStr) {
        logPrefix.set(logPrefixStr);
    }
    
    @Override
    protected String getLogPrefix() {
        return logPrefix.get();
    }

    @Override
    protected void setServiceUrl(String serviceUrlStr) {
        serviceUrl.set(serviceUrlStr);
    }
    
    /**
     * Get the service URL of this current thread.
     * 
     * @return the service URL
     */
    public String getServiceUrl() {
        return serviceUrl.get();
    }

}
