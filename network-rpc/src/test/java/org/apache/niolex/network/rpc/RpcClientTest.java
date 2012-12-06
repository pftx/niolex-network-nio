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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;

import org.apache.niolex.commons.reflect.MethodUtil;
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

	/**
	 * Test method for
	 * {@link org.apache.niolex.network.rpc.RpcClient#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])}
	 * .
	 * @throws Throwable
	 */
	@Test(expected = RpcException.class)
	public void testInvoke_1() throws Throwable {
		PacketClient pc = new PacketClient();
		RpcClient rr = new RpcClient(pc, new PacketInvoker(), new JsonConverter());
		rr.addInferface(RpcService.class);
		rr.setServerAddress(new InetSocketAddress("localhost", 8808));
		assertFalse(rr.isValid());
		Method method = MethodUtil.getMethods(RpcService.class, "add")[0];
		rr.invoke(rr, method, null);
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.apache.niolex.network.rpc.RpcClient#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])}
	 * .
	 * @throws Throwable
	 */
	@Test(expected = RpcException.class)
	public void testInvoke_a() throws Throwable {
	    PacketClient pc = mock(PacketClient.class);
	    PacketInvoker in = mock(PacketInvoker.class);
	    RpcClient rr = new RpcClient(pc, in, new JsonConverter());
	    rr.addInferface(RpcService.class);
	    rr.connect();
	    Method method = MethodUtil.getMethods(RpcService.class, "add")[0];
	    rr.invoke(rr, method, new Object[0]);
	}

	/**
	 * Test method for
	 * {@link org.apache.niolex.network.rpc.RpcClient#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])}
	 * .
	 * @throws Throwable
	 */
	@Test(expected = RpcException.class)
	public void testInvoke_2() throws Throwable {
		PacketClient pc = new PacketClient();
		RpcClient rr = new RpcClient(pc, new PacketInvoker(), new JsonConverter());
		rr.addInferface(RpcService.class);
		rr.stop();
		Method method = MethodUtil.getMethods(RpcService.class, "add")[0];
		rr.invoke(rr, method, null);
		fail("Not yet implemented");
	}

	@Test
	public void testHandleClose1() throws Throwable {
		PacketClient pc = new PacketClient(new InetSocketAddress("localhost", 8808));
		RpcClient rr = new RpcClient(pc, new PacketInvoker(), new JsonConverter());
		rr.addInferface(RpcService.class);
		rr.getRemoteName();
		rr.setSleepBetweenRetryTime(10);
		rr.setConnectTimeout(10);
		rr.stop();
		rr.handleClose(pc);
	}

	@Test
	public void testHandleClose2() throws Throwable {
	    PacketClient pc = new PacketClient(new InetSocketAddress("localhost", 8808));
	    RpcClient rr = new RpcClient(pc, new PacketInvoker(), new JsonConverter());
	    rr.addInferface(RpcService.class);
	    rr.getRemoteName();
	    rr.setSleepBetweenRetryTime(10);
	    rr.setConnectTimeout(10);
	    rr.setConnectRetryTimes(1);
	    rr.handleClose(pc);
	}

}
