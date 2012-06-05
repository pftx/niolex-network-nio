/**
 * RpcServiceFactoryTest.java
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
package org.apache.niolex.network.rpc.init;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;

import org.apache.niolex.network.rpc.RpcConfig;
import org.apache.niolex.network.rpc.RpcMethod;
import org.apache.niolex.network.rpc.conf.BaseConfiger;
import org.apache.niolex.network.rpc.conf.RpcConfiger;
import org.apache.niolex.network.rpc.json.JsonRpcFactory;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-5
 */
public class RpcServiceFactoryTest {

	/**
	 * Test method for
	 * {@link org.apache.niolex.network.rpc.init.RpcServiceFactory#getInstance(java.lang.String, org.apache.niolex.network.rpc.init.RpcClientFactory)}
	 * .
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testGetInstance() {
		RpcClientFactory factory = new JsonRpcFactory.JsonRpcClientFactory();
		RpcServiceFactory in = RpcServiceFactory.getInstance("/org/apache/niolex/network/rpc/json/rpc.properties",
				factory);
		in.getService("nan", RpcClientFactory.class);
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.apache.niolex.network.rpc.init.RpcServiceFactory#getService(java.lang.String, java.lang.Class)}.
	 */
	@Test
	public void testGetServiceStringClassOfT() {
		RpcServiceFactory factory = JsonRpcFactory.getInstance("/org/apache/niolex/network/rpc/json/rpc.properties");
		LocalService ser = factory.getService(LocalService.class);
		for (int i = 0; i < 10; ++i) {
			int r = ser.add(2, 3214, 123, 12, i);
			System.out.println(r);
			assertEquals(3351 + i, r);
		}
	}

	/**
	 * Test method for {@link org.apache.niolex.network.rpc.init.RpcServiceFactory#getConfiger()}.
	 */
	@Test
	public void testGetConfiger() {
		RpcClientFactory factory = new JsonRpcFactory.JsonRpcClientFactory();
		RpcServiceFactory in = RpcServiceFactory.getInstance("/org/apache/niolex/network/rpc/json/rpc.properties",
				factory);
		RpcConfiger con = in.getConfiger();
		con.getConfig();
	}

}

@RpcConfig(BaseConfiger.DEFAULT)
interface LocalService {

	@RpcMethod(14)
	public int add(int... args);

	@RpcMethod(15)
	public int size(List<String> arg);

	@RpcMethod(16)
	public String tr();
}
