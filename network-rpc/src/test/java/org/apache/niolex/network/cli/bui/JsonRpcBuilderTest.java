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

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.niolex.network.cli.bui.JsonRpcBuilder;
import org.apache.niolex.network.demo.json.RpcService;
import org.apache.niolex.network.rpc.RpcClient;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-4
 */
public class JsonRpcBuilderTest {

	/**
	 * Test method for {@link org.apache.niolex.network.cli.bui.JsonRpcBuilder#getInstance(java.lang.String)}.
	 * @throws IOException
	 */
	@Test
	public void testGetInstanceString() throws IOException {
		JsonRpcBuilder factory = new JsonRpcBuilder();
		factory.setClientUrl("10.22.241.233:8808");
		factory.setConnectTimeout(5000000);
		factory.setRpcHandleTimeout(10000);
		RpcClient cc = factory.build();
		cc.connect();
		RpcService ser = cc.getService(RpcService.class);
		for (int i = 0; i < 10; ++i) {
			int r = ser.add(2, 3214, 123, 12, i);
			System.out.println(r);
			assertEquals(3351 + i, r);
		}
	}

	@Test
	public void testBuild() throws Exception {
		JsonRpcBuilder factory = new JsonRpcBuilder();
		factory.setClientUrl("10.22.241.233:8808");
		factory.setConnectTimeout(100);
		factory.setRpcHandleTimeout(5000);
		RpcClient cc = factory.build();
		cc.stop();
	}

}
