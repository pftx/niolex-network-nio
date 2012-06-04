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

import static org.junit.Assert.fail;

import java.lang.reflect.Method;

import org.apache.niolex.commons.reflect.MethodUtil;
import org.apache.niolex.network.PacketClient;
import org.apache.niolex.network.demo.rpc.RpcService;
import org.apache.niolex.network.rpc.json.JsonRpcClient;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-4
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
		RpcClient rr = new JsonRpcClient(pc);
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
	public void testInvoke_2() throws Throwable {
		PacketClient pc = new PacketClient();
		RpcClient rr = new JsonRpcClient(pc);
		rr.stop();
		Method method = MethodUtil.getMethods(RpcService.class, "add")[0];
		rr.invoke(rr, method, null);
		fail("Not yet implemented");
	}

}
