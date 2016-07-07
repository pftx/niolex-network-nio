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
package org.apache.niolex.network.cli;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;

import org.apache.niolex.network.cli.RpcClientAdapter;
import org.apache.niolex.network.rpc.RpcClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-6-3
 */
@RunWith(MockitoJUnitRunner.class)
public class RpcClientAdapterTest {

	@Mock
	private RpcClient handler;

	private String serviceUrl;
	private RpcClientAdapter rpcClientHandler;

	@Before
	public void createRpcConnectionHandler() throws Throwable {
		serviceUrl = "GOGO";
		rpcClientHandler = new RpcClientAdapter(serviceUrl, handler);
		when(handler.invoke(any(Object.class), any(Method.class), any(Object[].class))).thenReturn("mailto:xiejiyun");
	}

	/**
	 * Test method for {@link org.apache.niolex.network.cli.RpcClientAdapter#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])}.
	 * @throws Throwable
	 */
	@Test
	public final void testInvoke() throws Throwable {
		Object o = rpcClientHandler.invoke(handler, null, null);
		assertEquals("mailto:xiejiyun", o);
	}

	/**
	 * Test method for {@link org.apache.niolex.network.cli.RpcClientAdapter#getServiceUrl()}.
	 */
	@Test
	public final void testGetServiceUrl() {
		assertEquals(serviceUrl, rpcClientHandler.getServiceUrl());
	}

	/**
	 * Test method for {@link org.apache.niolex.network.cli.RpcClientAdapter#isReady()}.
	 */
	@Test
	public final void testIsReady() {
		assertFalse(rpcClientHandler.isReady());
	}

	@Test
	public final void testIsReadyTrue() {
		when(handler.isValid()).thenReturn(true);
		assertTrue(rpcClientHandler.isReady());
	}

	/**
	 * Test method for {@link org.apache.niolex.network.cli.RpcClientAdapter#notReady(java.io.IOException)}.
	 */
	@Test
	public final void testNotReady() {
		rpcClientHandler.notReady(null);
	}

	/**
	 * Test method for {@link org.apache.niolex.network.cli.RpcClientAdapter#getHandler()}.
	 */
	@Test
	public final void testGetHandler() {
		assertEquals(handler, rpcClientHandler.getHandler());
	}

	@Test
	public void testToString()
	 throws Exception {
		assertEquals(serviceUrl, rpcClientHandler.toString());
	}

}
