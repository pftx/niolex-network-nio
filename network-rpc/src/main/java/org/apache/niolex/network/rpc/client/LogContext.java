/**
 * LogContext.java
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
package org.apache.niolex.network.rpc.client;

/**
 * The rpc framework use this class to write log prefix and set current service url.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 *
 * @version @version@, $Date: 2011-7-12$
 *
 */
public abstract class LogContext {

	/**
	 * This inner class, is just for unit test and demo run.
	 * For products, please extend LogContext and create your own class.
	 */
    private static LogContext INSTANCE = new LogContext() {

        public String getLogPrefix() {
            return "LOGID";
        }

        public void setServerUrl(String serverUrl) {
        }

    };

    /**
     * Inject a proper sub class of this LogContext.
     * @param instance
     */
    public static void setInstance(LogContext instance) {
        LogContext.INSTANCE = instance;
    }

    /**
     * Return the log prefix for rpc framework. This framework will log with this prefix when
     * something wrong.
     * @return
     */
    public abstract String getLogPrefix();

    /**
     * This rpc framework will inject the current server URL into this method.
     * @param serverUrl
     */
    public abstract void setServerUrl(String serverUrl);

    /**
     * Framework internal use only.
     * @return
     */
    static String prefix() {
        return INSTANCE.getLogPrefix();
    }

    /**
     * Framework internal use only.
     * @param serverUrl
     */
    static void serviceUrl(String serverUrl) {
        INSTANCE.setServerUrl(serverUrl);
    }
}
