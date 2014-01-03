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
package org.apache.niolex.network.cli;

/**
 * The rpc framework use this class to write log prefix and set current service url to it.
 *
 * User application need to subclass this, and set a instance of it into this class.
 * We will get the global logid from it to maintain all the logs in a consistent logid.
 *
 * User need to override these two methods:
 * {@link #setServerUrl(String)}
 * {@link #getLogPrefix()}
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version @version@, $Date: 2011-7-12$
 */
public class LogContext {

	/**
	 * This class is just for unit test and demo run.
	 * For products, please extend LogContext and create your own subclass.
	 */
    private static LogContext INSTANCE = new LogContext();

    /**
     * Inject a proper sub class of this LogContext.
     *
     * @param instance the instance to provide log prefix(i.e. log id) and store current
     * service url
     */
    public static void setInstance(LogContext instance) {
        LogContext.INSTANCE = instance;
    }

    /**
     * Just return GID here for demo & unit test.
     *
     * Subclass need to override this method to provide their own implementation,
     * return the global log prefix for rpc framework. This framework will log with this prefix.
     *
     * @return the global logid or other log prefix
     */
    protected String getLogPrefix() {
    	return "GID";
    }

    /**
     * Just do nothing here.
     *
     * Subclass need to override this method to provide their own implementation,
     * this rpc framework will inject the current using server URL into this method.
     *
     * @param serverUrl the current using server url
     */
    protected void setServerUrl(String serverUrl) {
    	// Do nothing here, for demo.
    }

    /**
     * Framework internal use only.
     *
     * @return current log prefix(i.e. logid)
     */
    static String prefix() {
        return INSTANCE.getLogPrefix();
    }

    /**
     * Framework internal use only.
     *
     * @param serverUrl the current using server Url
     */
    static void serviceUrl(String serverUrl) {
        INSTANCE.setServerUrl(serverUrl);
    }
}
