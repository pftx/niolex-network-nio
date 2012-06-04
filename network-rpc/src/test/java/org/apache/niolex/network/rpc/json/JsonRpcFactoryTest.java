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
package org.apache.niolex.network.rpc.json;

import static org.junit.Assert.*;

import org.apache.niolex.network.demo.rpc.RpcService;
import org.apache.niolex.network.rpc.init.RpcServiceFactory;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-4
 */
public class JsonRpcFactoryTest {

	/**
	 * Test method for {@link org.apache.niolex.network.rpc.json.JsonRpcFactory#getInstance(java.lang.String)}.
	 */
	@Test
	public void testGetInstanceString() {
		RpcServiceFactory factory = JsonRpcFactory.getInstance("/org/apache/niolex/network/rpc/json/rpc.properties");
		RpcService ser = factory.getService(RpcService.class);
		for (int i = 0; i < 10; ++i) {
			int r = ser.add(2, 3214, 123, 12, i);
			System.out.println(r);
			assertEquals(3351 + i, r);
		}
	}

}
