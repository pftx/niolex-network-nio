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
 * The rpc framework use this class to get log prefix and set current service url to it.
 * <br>
 * User application need to subclass this to provide log prefix, i.e. a global LOG ID,
 * and store the service URL for proper use. Set a instance of your class into the global
 * field by {@link #setInstance(LogContext)} to make your class work.
 * We will get the global logid from it to maintain all the logs in a consistent logid.
 * <br>
 * User need to override these two methods:
 * {@link #setServiceUrl(String)}
 * {@link #getLogPrefix()}
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version @version@, $Date: 2011-7-12$
 */
public class LogContext {

	/**
	 * This class is just for unit test and demo run.
	 * For productions, please extend LogContext and create your own subclass.
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
     * <br>
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
     * <br>
     * Subclass need to override this method to provide their own implementation,
     * this rpc framework will inject the current using service URL into this method.
     *
     * @param serviceUrl the current using service url
     */
    protected void setServiceUrl(String serviceUrl) {
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
     * @param serviceUrl the current using service Url
     */
    static void serviceUrl(String serviceUrl) {
        INSTANCE.setServiceUrl(serviceUrl);
    }
}
