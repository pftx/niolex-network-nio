/**
 * JsonRpcClientTest.java
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

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;

import org.apache.niolex.commons.reflect.MethodUtil;
import org.apache.niolex.network.CoreRunner;
import org.apache.niolex.network.client.PacketClient;
import org.apache.niolex.network.demo.json.DemoJsonRpcServer;
import org.apache.niolex.network.demo.json.RpcService;
import org.apache.niolex.network.rpc.RpcClient;
import org.apache.niolex.network.rpc.RpcClient.Status;
import org.apache.niolex.network.rpc.PacketInvoker;
import org.apache.niolex.network.rpc.RpcException;
import org.apache.niolex.network.rpc.conv.JsonConverter;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-6-4
 */
public class JsonRpcClientTest {
	private static RpcService ser;
	private static RpcClient client;

	@BeforeClass
	public static void up() throws IOException {
		DemoJsonRpcServer.main(null);
		PacketClient c = new PacketClient(new InetSocketAddress("localhost", 8808));
        client = new RpcClient(c, new PacketInvoker(), new JsonConverter());
        client.connect();

        ser = client.getService(RpcService.class);
	}

	@AfterClass
	public static void down() {
		client.stop();
		DemoJsonRpcServer.stop();
	}
	/**
	 * Test method for {@link org.apache.niolex.network.cli.bui.JsonRpcClient#serializeParams(java.lang.Object[])}.
	 */
	@Test
	public void testAddInferface() {
		client.addInferface(getClass());
		client.addInferface(RpcService.class);
		client.setConnectTimeout(1234);
		assertTrue(client.getConnStatus() == Status.CONNECTED);
	}

	/**
	 * Test method for {@link org.apache.niolex.network.cli.bui.JsonRpcClient#prepareReturn(byte[], java.lang.reflect.Type, int)}.
	 */
	@Test(expected=RpcException.class)
	public void testPrepareReturn() {
		String r = ser.throwEx();
		fail(r);
	}

	@Test(expected=RpcException.class)
	public void invoke() throws Throwable {
		Method method = MethodUtil.getMethods(JsonRpcClientTest.class, "invoke")[0];
		client.invoke(ser, method, null);
	}

	/**
	 * Test method for {@link org.apache.niolex.network.rpc.RpcClient#connect()}.
	 * @throws Throwable
	 */
	@Test
	public void testRetryConnect() throws Throwable {
		client.setSleepBetweenRetryTime(400);
		client.setConnectRetryTimes(2);
		DemoJsonRpcServer.stop();
		Thread.sleep(CoreRunner.CO_SLEEP);
		DemoJsonRpcServer.main(null);
		//client.handleError(null);
		Thread.sleep(10 * CoreRunner.CO_SLEEP);
		assertTrue(client.getConnStatus() == Status.CONNECTED);
	}

}
