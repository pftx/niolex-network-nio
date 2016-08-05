/**
 * RpcClientTest.java
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
package org.apache.niolex.network.rpc.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;

import org.apache.niolex.commons.reflect.MethodFilter;
import org.apache.niolex.commons.reflect.MethodUtil;
import org.apache.niolex.network.ConnStatus;
import org.apache.niolex.network.client.PacketClient;
import org.apache.niolex.network.demo.json.RpcService;
import org.apache.niolex.network.rpc.RpcException;
import org.apache.niolex.network.rpc.conv.JsonConverter;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-6-4
 */
public class RpcClientTest {

    public static final Method getFirstMethod(Class<?> clazz, String methodName) {
        return MethodUtil.getMethods(clazz, MethodFilter.c().includeAll().n(methodName)).get(0);
    }

	/**
     * Test method for
     * {@link org.apache.niolex.network.rpc.RpcStub#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])}
     * .
     * 
     * @throws Throwable
     */
	@Test(expected = RpcException.class)
	public void testInvokeNotConnected() throws Throwable {
        BaseInvoker pc = new BaseInvoker(new PacketClient());
        RpcStub rr = new RpcStub(pc, new JsonConverter());
		rr.addInferface(RpcService.class);
        pc.setServerAddress(new InetSocketAddress("localhost", 8808));
        assertFalse(pc.isReady());
        assertEquals(ConnStatus.INNITIAL, pc.getConnStatus());
		Method method = getFirstMethod(RpcService.class, "add");
		try {
		    rr.invoke(rr, method, null);
		} catch (RpcException r) {
		    assertEquals(RpcException.Type.NOT_CONNECTED, r.getType());
		    throw r;
		}
		fail("Not yet implemented");
	}

	/**
     * Test method for
     * {@link org.apache.niolex.network.rpc.RpcStub#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])}
     * .
     * 
     * @throws Throwable
     */
	@Test(expected = RpcException.class)
	public void testInvokeTimeout() throws Throwable {
        BaseInvoker in = mock(BaseInvoker.class);
        RpcStub rr = new RpcStub(in, new JsonConverter());
	    rr.addInferface(RpcService.class);
	    Method method = getFirstMethod(RpcService.class, "add");
	    try {
	        rr.invoke(rr, method, new Object[0]);
	    } catch (RpcException r) {
            assertEquals(RpcException.Type.TIMEOUT, r.getType());
            throw r;
        }
	}

	/**
     * Test method for
     * {@link org.apache.niolex.network.rpc.RpcStub#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])}
     * .
     * 
     * @throws Throwable
     */
	@Test(expected = RpcException.class)
	public void testInvokeClosed() throws Throwable {
        BaseInvoker pc = new BaseInvoker(new PacketClient());
        RpcStub rr = new RpcStub(pc, new JsonConverter());
		rr.addInferface(RpcService.class);
		Method method = getFirstMethod(RpcService.class, "add");
		try {
            rr.invoke(rr, method, new Object[0]);
        } catch (RpcException r) {
            assertEquals(RpcException.Type.NOT_CONNECTED, r.getType());
            throw r;
        }
		fail("Not yet implemented");
	}

}
