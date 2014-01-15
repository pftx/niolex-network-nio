/**
 * JsonRpcFactoryTest.java
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
package org.apache.niolex.network.cli.bui;

import static org.junit.Assert.*;

import java.io.IOException;

import org.apache.niolex.commons.bean.Pair;
import org.apache.niolex.network.IClient;
import org.apache.niolex.network.cli.IServiceHandler;
import org.apache.niolex.network.cli.conf.RpcConfigBean;
import org.apache.niolex.network.client.BlockingClient;
import org.apache.niolex.network.client.PacketClient;
import org.apache.niolex.network.client.SocketClient;
import org.apache.niolex.network.demo.json.DemoJsonRpcServer;
import org.apache.niolex.network.rpc.PacketInvoker;
import org.apache.niolex.network.rpc.RemoteInvoker;
import org.apache.niolex.network.rpc.RpcClient;
import org.apache.niolex.network.rpc.SingleInvoker;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-6-4
 */
public class JsonRpcBuilderTest {

	@BeforeClass
	public static void up() throws IOException {
		DemoJsonRpcServer.main(null);
	}

	@AfterClass
	public static void down() {
		DemoJsonRpcServer.stop();
	}

	@Test
    public void testBuildClientP() throws Exception {
	    RpcConfigBean bean = new RpcConfigBean("a");
	    bean.clientType = "PacketClient";
	    bean.rpcTimeout = 300;
        JsonRpcBuilder bui = new JsonRpcBuilder();
        Pair<IClient,RemoteInvoker> pair = bui.buildClient(bean, "abc://localhost:8808/gogogo");
        assertTrue(pair.a instanceof PacketClient);
        assertTrue(pair.b instanceof PacketInvoker);
        PacketInvoker pi = (PacketInvoker)pair.b;
        assertEquals(300, pi.getRpcHandleTimeout());
	}

	@Test
	public void testBuildClientB() throws Exception {
	    RpcConfigBean bean = new RpcConfigBean("a");
	    bean.clientType = "BlockingClient";
	    bean.rpcTimeout = 300;
	    JsonRpcBuilder bui = new JsonRpcBuilder();
	    Pair<IClient,RemoteInvoker> pair = bui.buildClient(bean, "abc://localhost:8808/gogogo");
	    assertTrue(pair.a instanceof BlockingClient);
	    assertTrue(pair.b instanceof PacketInvoker);
	    PacketInvoker pi = (PacketInvoker)pair.b;
	    assertEquals(300, pi.getRpcHandleTimeout());
	}

	@Test
	public void testBuildClientS() throws Exception {
	    RpcConfigBean bean = new RpcConfigBean("a");
	    bean.clientType = "SocketClient";
	    bean.rpcTimeout = 300;
	    JsonRpcBuilder bui = new JsonRpcBuilder();
	    Pair<IClient,RemoteInvoker> pair = bui.buildClient(bean, "abc://localhost:8808/gogogo");
	    assertTrue(pair.a instanceof SocketClient);
	    assertTrue(pair.b instanceof SingleInvoker);
	}

	/**
	 * Test method for {@link org.apache.niolex.network.cli.bui.JsonRpcBuilder#getInstance(java.lang.String)}.
	 * @throws IOException
	 */
	@Test
	public void testBuild() throws Exception {
		RpcConfigBean bean = new RpcConfigBean("a");
		JsonRpcBuilder factory = new JsonRpcBuilder();
		bean.connectTimeout = 5000;
		bean.rpcTimeout = 100;
		IServiceHandler cc = factory.build(bean, "abc://localhost:8808/gogogo");
		((RpcClient)cc.getHandler()).stop();
	}

	@Test
	public void testBuild2() throws Exception {
		RpcConfigBean bean = new RpcConfigBean("a");
		JsonRpcBuilder factory = new JsonRpcBuilder();
		bean.connectTimeout = 345;
		bean.rpcTimeout = 10000;
		IServiceHandler cc = factory.build(bean, "abc://localhost:8808/gogogo");
		((RpcClient)cc.getHandler()).stop();
	}

}
