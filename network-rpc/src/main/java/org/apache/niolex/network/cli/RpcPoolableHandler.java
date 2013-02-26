/**
 * RpcPoolableHandler.java
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
import java.lang.reflect.Method;

import org.apache.niolex.network.rpc.PoolableInvocationHandler;

/**
 * This is the adapter to adapt PoolableInvocationHandler to IServiceHandler, which
 * is the interface could be managed by RetryHandler.
 *
 * We disable the error block functionality in this class.
 * If user want that, use {@link RpcServiceHandler}
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.3, Date: 2012-6-3
 */
public class RpcPoolableHandler implements IServiceHandler {

	private final String serviceUrl;
	private final PoolableInvocationHandler handler;

	public RpcPoolableHandler(String serviceUrl, PoolableInvocationHandler handler) {
		super();
		this.serviceUrl = serviceUrl;
		this.handler = handler;
	}

	/**
	 * This is the override of super method.
	 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
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
		// We will ignore this method, the PoolableInvocationHandler will manage it's status.
	}

	/**
	 * This is the override of super method.
	 * @see org.apache.niolex.network.cli.IServiceHandler#getHandler()
	 */
	@Override
	public InvocationHandler getHandler() {
		return handler;
	}
}
