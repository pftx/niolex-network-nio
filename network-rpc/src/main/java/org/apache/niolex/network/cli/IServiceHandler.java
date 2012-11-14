/**
 * IServiceHandler.java
 *
 * Copyright 2012 Niolex, Inc.
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

import java.io.IOException;
import java.lang.reflect.InvocationHandler;

/**
 * The interface for RetryHandler usage, this interface manage
 * the status of the underlying client, and store the complete url here for logging.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0, Date: 2012-6-3
 */
public interface IServiceHandler extends InvocationHandler {

	/**
	 * Get the Service Url of the underlying rpc client.
	 *
	 * @return the current service url
	 */
	public abstract String getServiceUrl();

	/**
	 * Whether this client is ready or not.
	 *
	 * @return true if this client is ready, false otherwise
	 */
	public abstract boolean isReady();

	/**
	 * Make this client not ready.
	 * Mark with the given exception.
	 *
	 * @param ioe
	 */
	public abstract void notReady(IOException ioe);

	/**
	 * Get the underlying client stub, which is also an InvocationHandler
	 *
	 * @return the underlying client stub
	 */
	public InvocationHandler getHandler();

}