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
package org.apache.niolex.network.cli.init;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;

import org.apache.niolex.network.cli.conf.BaseConfiger;
import org.apache.niolex.network.cli.conf.RpcConfiger;
import org.apache.niolex.network.cli.init.RpcClientFactory;
import org.apache.niolex.network.cli.init.RpcServiceFactory;
import org.apache.niolex.network.rpc.RpcConfig;
import org.apache.niolex.network.rpc.anno.RpcMethod;
import org.apache.niolex.network.rpc.json.JsonRpcBuilder;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-5
 */
public class RpcServiceFactoryTest {

	/**
	 * Test method for
	 * {@link org.apache.niolex.network.cli.init.RpcServiceFactory#getInstance(java.lang.String, org.apache.niolex.network.cli.init.RpcClientFactory)}
	 * .
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testGetInstance() {
		RpcClientFactory.registerBuilder("network/json", new JsonRpcBuilder());
		RpcServiceFactory in = RpcServiceFactory.getInstance("/org/apache/niolex/network/rpc/json/rpc.properties");
		in.getService("nan", RpcClientFactory.class);
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.apache.niolex.network.cli.init.RpcServiceFactory#getService(java.lang.String, java.lang.Class)}.
	 */
	@Test
	public void testGetServiceStringClassOfT() {
		RpcClientFactory.registerBuilder("network/json", new JsonRpcBuilder());
		RpcServiceFactory factory = RpcServiceFactory.getInstance("/org/apache/niolex/network/rpc/json/rpc.properties");
		LocalService ser = factory.getService(LocalService.class);
		for (int i = 0; i < 10; ++i) {
			int r = ser.add(2, 3214, 123, 12, i);
			System.out.println(r);
			assertEquals(3351 + i, r);
		}
	}

	/**
	 * Test method for
	 * {@link org.apache.niolex.network.cli.init.RpcServiceFactory#getService(java.lang.String, java.lang.Class)}.
	 */
	@Test
	public void testGetServiceStringClassOfF() {
		RpcClientFactory.registerBuilder("network/json", new JsonRpcBuilder());
		RpcServiceFactory factory = RpcServiceFactory.getInstance("/org/apache/niolex/network/rpc/json/rpc.properties");
		EfService ser = factory.getService(EfService.class);
		for (int i = 0; i < 10; ++i) {
			int r = ser.add(2, 3214, 123, 12, i);
			System.out.println(r);
			assertEquals(3351 + i, r);
		}
	}

	/**
	 * Test method for {@link org.apache.niolex.network.cli.init.RpcServiceFactory#getConfiger()}.
	 */
	@Test
	public void testGetConfiger() {
		RpcClientFactory.registerBuilder("network/json", new JsonRpcBuilder());
		RpcServiceFactory in = RpcServiceFactory.getInstance("/org/apache/niolex/network/rpc/json/rpc.properties");
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

interface EfService {

	@RpcMethod(14)
	public int add(int... args);

	@RpcMethod(15)
	public int size(List<String> arg);

	@RpcMethod(16)
	public String tr();
}
