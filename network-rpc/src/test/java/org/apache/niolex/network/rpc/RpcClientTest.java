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
package org.apache.niolex.network.rpc;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;

import org.junit.Assert;

import org.apache.niolex.commons.reflect.MethodFilter;
import org.apache.niolex.commons.reflect.MethodUtil;
import org.apache.niolex.network.ConnStatus;
import org.apache.niolex.network.client.PacketClient;
import org.apache.niolex.network.demo.json.RpcService;
import org.apache.niolex.network.rpc.PacketInvoker;
import org.apache.niolex.network.rpc.RpcClient;
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
	 * {@link org.apache.niolex.network.rpc.RpcClient#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])}
	 * .
	 * @throws Throwable
	 */
	@Test(expected = RpcException.class)
	public void testInvokeNotConnected() throws Throwable {
		PacketClient pc = new PacketClient();
		RpcClient rr = new RpcClient(pc, new PacketInvoker(), new JsonConverter());
		rr.addInferface(RpcService.class);
		rr.setServerAddress(new InetSocketAddress("localhost", 8808));
		assertFalse(rr.isValid());
		assertEquals(ConnStatus.INNITIAL, rr.getConnStatus());
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
	 * {@link org.apache.niolex.network.rpc.RpcClient#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])}
	 * .
	 * @throws Throwable
	 */
	@Test(expected = RpcException.class)
	public void testInvokeTimeout() throws Throwable {
	    PacketClient pc = mock(PacketClient.class);
	    PacketInvoker in = mock(PacketInvoker.class);
	    RpcClient rr = new RpcClient(pc, in, new JsonConverter());
	    rr.addInferface(RpcService.class);
	    rr.connect();
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
	 * {@link org.apache.niolex.network.rpc.RpcClient#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])}
	 * .
	 * @throws Throwable
	 */
	@Test(expected = RpcException.class)
	public void testInvokeClosed() throws Throwable {
		PacketClient pc = new PacketClient();
		RpcClient rr = new RpcClient(pc, new PacketInvoker(), new JsonConverter());
		rr.addInferface(RpcService.class);
		rr.stop();
		Method method = getFirstMethod(RpcService.class, "add");
		try {
            rr.invoke(rr, method, new Object[0]);
        } catch (RpcException r) {
            assertEquals(RpcException.Type.CONNECTION_CLOSED, r.getType());
            throw r;
        }
		fail("Not yet implemented");
	}

	@Test
	public void testIsException() throws Throwable {
	    PacketClient pc = mock(PacketClient.class);
        PacketInvoker in = mock(PacketInvoker.class);
        RpcClient rr = new RpcClient(pc, in, new JsonConverter());

        rr.connect(); // 1
        assertTrue(rr.isValid());
        rr.connect(); // 2
        assertTrue(rr.isValid());

        Method m = MethodUtil.getFirstMethod(rr, "isException");
        m.setAccessible(true);
        assertTrue((Boolean)m.invoke(rr, 1));
        assertTrue((Boolean)m.invoke(rr, -255));

        rr.stop(); // -- stopped
        assertFalse(rr.isValid());
        rr.stop(); // -- this time will be skipped
        assertFalse(rr.isValid());
	}

	@Test
	public void testHandleCloseAlreadyClosed() throws Throwable {
		PacketClient pc = new PacketClient(new InetSocketAddress("localhost", 8808));
		RpcClient rr = new RpcClient(pc, new PacketInvoker(), new JsonConverter());
		rr.addInferface(RpcService.class);
		rr.getRemoteName();
		rr.setSleepBetweenRetryTime(10);
		Assert.assertEquals(10, rr.getSleepBetweenRetryTime());
		rr.setConnectTimeout(120);
		rr.stop();
		rr.handleClose(pc);
	}

	@Test
	public void testHandleCloseFailedToReconnect() throws Throwable {
	    PacketClient pc = new PacketClient(new InetSocketAddress("localhost", 8808));
	    RpcClient rr = new RpcClient(pc, new PacketInvoker(), new JsonConverter());
	    rr.addInferface(RpcService.class);
	    rr.getRemoteName();
	    rr.setSleepBetweenRetryTime(10);
	    rr.setConnectTimeout(10);
	    rr.setConnectRetryTimes(1);
	    Assert.assertEquals(1, rr.getConnectRetryTimes());
	    rr.handleClose(pc);
	}

    @Test
    public void testPrepareReturn() throws Exception {
        PacketClient pc = mock(PacketClient.class);
        PacketInvoker in = mock(PacketInvoker.class);
        RpcClient rr = new RpcClient(pc, in, new JsonConverter());
        assertNull(rr.prepareReturn(null, null, false));
        assertNull(rr.prepareReturn(null, void.class, false));
    }

    @Test
    public void testAddInferface() throws Exception {
        System.out.println("not yet implemented");
    }

}
