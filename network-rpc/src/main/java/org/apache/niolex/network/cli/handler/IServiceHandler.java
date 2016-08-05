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
package org.apache.niolex.network.cli.handler;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;

import org.apache.niolex.network.cli.PoolHandler;
import org.apache.niolex.network.cli.RetryHandler;

/**
 * The interface for {@link RetryHandler} and {@link PoolHandler} to use, this interface manage
 * the status of the underlying service client, and store the complete url here for logging.
 * <br>
 * For any RPC framework, there will be a client side stub managing connections to the real
 * server, and transmit data between client and server. Application programmers will need to
 * wrap the real client stub by a wrapper class which implement this interface, and then use
 * {@link RetryHandler} or {@link PoolHandler} to manage multiple clients.
 *
 * @see org.apache.niolex.network.cli.PoolHandler
 * @see org.apache.niolex.network.cli.RetryHandler
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0, Date: 2012-6-3
 */
public interface IServiceHandler extends InvocationHandler {

	/**
	 * Get the service url the underlying rpc client is using to connect
	 * to the rpc server, for request tracking.
	 *
	 * @return the current service url
	 */
	public String getServiceUrl();

	/**
	 * Whether the underlying rpc client is ready or not.
	 *
	 * @return true if this client is ready, false otherwise
	 */
	public boolean isReady();

	/**
	 * Make this client not ready with the given exception.
	 *
	 * @param ioe the exception which makes this client not ready
	 */
	public void notReady(IOException ioe);

	/**
	 * Get the underlying client stub, which is also an InvocationHandler.
	 * <p>
	 * If there is no underlying client stub, return this
	 * </p>
	 *
	 * @return the underlying client stub
	 */
	public InvocationHandler getHandler();

}