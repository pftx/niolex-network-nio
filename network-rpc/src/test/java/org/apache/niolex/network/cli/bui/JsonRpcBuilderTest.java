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

import java.io.IOException;

import org.apache.niolex.network.cli.IServiceHandler;
import org.apache.niolex.network.cli.conf.RpcConfigBean;
import org.apache.niolex.network.demo.json.DemoJsonRpcServer;
import org.apache.niolex.network.rpc.RpcClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-4
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
