/**
 * RpcInvokeExceptionTest.java
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
package org.apache.niolex.network.rpc;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-5-27
 */
public class RpcInvokeExceptionTest {

	/**
	 * Test method for {@link org.apache.niolex.network.rpc.RpcInvokeException#RpcInvokeException()}.
	 */
	@Test
	public final void testRpcInvokeException() {
		RpcInvokeException er = new RpcInvokeException();
		assertEquals(er.getMessage(), null);
	}

	/**
	 * Test method for {@link org.apache.niolex.network.rpc.RpcInvokeException#RpcInvokeException(java.lang.String, java.lang.Throwable)}.
	 */
	@Test
	public final void testRpcInvokeExceptionStringThrowable() {
		Exception e = new Exception();
		RpcInvokeException er = new RpcInvokeException("Pool good thing.", e);
		assertEquals(er.getMessage(), "Pool good thing.");
		assertEquals(er.getCause(), e);
	}

	/**
	 * Test method for {@link org.apache.niolex.network.rpc.RpcInvokeException#RpcInvokeException(java.lang.String)}.
	 */
	@Test
	public final void testRpcInvokeExceptionString() {
		RpcInvokeException er = new RpcInvokeException("Pool good thing.");
		assertEquals(er.getMessage(), "Pool good thing.");
	}

	/**
	 * Test method for {@link org.apache.niolex.network.rpc.RpcInvokeException#RpcInvokeException(java.lang.Throwable)}.
	 */
	@Test
	public final void testRpcInvokeExceptionThrowable() {
		Exception e = new Exception();
		RpcInvokeException er = new RpcInvokeException(e);
		System.out.println(er.getMessage());
		assertEquals(er.getMessage(), "java.lang.Exception");
		assertEquals(er.getCause(), e);
	}

}
