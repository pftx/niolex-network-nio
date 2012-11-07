/**
 * RpcConnectionHandlerTest.java
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
package org.apache.niolex.network.rpc.client;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;

import org.apache.niolex.network.rpc.RpcClient;
import org.apache.niolex.network.rpc.client.RpcConnectionHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-3
 */
public class RpcConnectionHandlerTest {

	@Mock
	private RpcClient handler;

	private String serviceUrl;
	private RpcConnectionHandler rpcConnectionHandler;

	@Before
	public void createRpcConnectionHandler() throws Throwable {
		serviceUrl = "GOGO";
		rpcConnectionHandler = new RpcConnectionHandler(serviceUrl, handler);
		when(handler.invoke(any(Object.class), any(Method.class), any(Object[].class))).thenReturn("mailto:xiejiyun");
	}

	/**
	 * Test method for {@link org.apache.niolex.network.rpc.client.RpcConnectionHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])}.
	 * @throws Throwable
	 */
	@Test
	public final void testInvoke() throws Throwable {
		Object o = rpcConnectionHandler.invoke(handler, null, null);
		assertEquals("mailto:xiejiyun", o);
	}

	/**
	 * Test method for {@link org.apache.niolex.network.rpc.client.RpcConnectionHandler#getServiceUrl()}.
	 */
	@Test
	public final void testGetServiceUrl() {
		assertEquals(serviceUrl, rpcConnectionHandler.getServiceUrl());
	}

	/**
	 * Test method for {@link org.apache.niolex.network.rpc.client.RpcConnectionHandler#isReady()}.
	 */
	@Test
	public final void testIsReady() {
		assertFalse(rpcConnectionHandler.isReady());
	}

	/**
	 * Test method for {@link org.apache.niolex.network.rpc.client.RpcConnectionHandler#notReady(java.io.IOException)}.
	 */
	@Test
	public final void testNotReady() {
		rpcConnectionHandler.notReady(null);
	}

	/**
	 * Test method for {@link org.apache.niolex.network.rpc.client.RpcConnectionHandler#getHandler()}.
	 */
	@Test
	public final void testGetHandler() {
		assertEquals(handler, rpcConnectionHandler.getHandler());
	}

}
