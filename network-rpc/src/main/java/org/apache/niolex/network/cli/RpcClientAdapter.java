/**
 * RpcClientAdapter.java
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
import java.lang.reflect.Method;

import org.apache.niolex.network.rpc.RpcClient;

/**
 * This is the adapter to adapt RpcClient to IServiceHandler, which
 * is the interface could be managed by RetryHandler and PoolHandler.
 *
 * We disable the error block functionality in this class.
 * If user want that, use {@link RpcServiceHandler}
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.3, Date: 2012-6-3
 */
public class RpcClientAdapter implements IServiceHandler {

	private final String serviceUrl;
	private final RpcClient handler;

	/**
	 * Construct a rpc client handler.
	 *
	 * @param serviceUrl the service url
	 * @param handler the rpc client instance
	 */
	public RpcClientAdapter(String serviceUrl, RpcClient handler) {
		super();
		this.serviceUrl = serviceUrl;
		this.handler = handler;
	}

	/**
	 * This is the override of super method.
	 * @see java.lang.reflect.InvocationHandler#invoke(Object, Method, Object[])
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		return handler.invoke(proxy, method, args);
	}

	/**
	 * This is the override of super method.
	 * @see org.apache.niolex.network.cli.IServiceHandler#getServiceUrl()
	 */
	@Override
	public String getServiceUrl() {
		return serviceUrl;
	}

	/**
	 * This is the override of super method.
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return serviceUrl;
	}

	/**
	 * This is the override of super method.
	 * @see org.apache.niolex.network.cli.IServiceHandler#isReady()
	 */
	@Override
	public boolean isReady() {
		return handler.isValid();
	}

	/**
	 * This is the override of super method.
	 * @see org.apache.niolex.network.cli.IServiceHandler#notReady(java.io.IOException)
	 */
	@Override
	public void notReady(IOException ioe) {
		// We will ignore this method, the RpcClient will manage it's status.
	}

	/**
	 * This is the override of super method.
	 * @see org.apache.niolex.network.cli.IServiceHandler#getHandler()
	 */
	@Override
	public RpcClient getHandler() {
		return handler;
	}
}
