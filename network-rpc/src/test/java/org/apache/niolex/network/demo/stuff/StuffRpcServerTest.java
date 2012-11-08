/**
 * StuffRpcServerTest.java
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
package org.apache.niolex.network.demo.stuff;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-9-4
 */
public class StuffRpcServerTest {

	/**
	 * Test method for {@link org.apache.niolex.network.demo.stuff.StuffRpcServer#main(java.lang.String[])}.
	 * @throws IOException
	 */
	@Test
	public void testMain() throws IOException {
		StuffRpcServer.main(null);
		DemoStuffRpcClient.main(null);
		StuffRpcServer.stop();
	}

	/**
	 * Test method for {@link org.apache.niolex.network.demo.stuff.StuffRpcServer#stop()}.
	 */
	@Test
	public void testStop() {
		StuffRpcServer sr = new StuffRpcServer();
		sr.toString();
		RpcService abc = new RpcServiceImpl();
		IntArray aa = new IntArray();
		aa.arr = new int[] {3, 4, 5, 6, 7, 8, 9};
		int k = abc.add(aa);
		assertEquals(42, k);
		StringArray sarr = new StringArray();
		sarr.arr = new String[] {"Hello ", " world.", " God."};
		String s = abc.concat(sarr);
		assertEquals("Hello  world. God.", s);
		k = abc.size(sarr);
		assertEquals(3, k);
		k = abc.size(null);
		assertEquals(0, k);
	}

}
