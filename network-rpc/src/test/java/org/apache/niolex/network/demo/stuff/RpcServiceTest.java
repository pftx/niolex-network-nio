/**
 * RpcServiceTest.java
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

import org.apache.niolex.network.press.StuffPress;
import org.apache.niolex.network.rpc.RpcClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-9-4
 */
public class RpcServiceTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		StuffRpcServer.main(new String[] {"1", "3"});
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		StuffRpcServer.stop();
	}

	/**
	 * Test method for {@link org.apache.niolex.network.demo.stuff.RpcService#add(org.apache.niolex.network.demo.stuff.IntArray)}.
	 * @throws IOException
	 */
	@Test
	public void testAdd() throws IOException {
		RpcClient scli = StuffPress.create();
		RpcService r = scli.getService(RpcService.class);
		IntArray aa = new IntArray();
		aa.arr = new int[] {3, 4, 5, 6, 7, 8, 9};
		int k = r.add(aa);
		assertEquals(42, k);
		scli.stop();
	}

	/**
	 * Test method for {@link org.apache.niolex.network.demo.stuff.RpcService#size(org.apache.niolex.network.demo.stuff.StringArray)}.
	 * @throws IOException
	 */
	@Test
	public void testSize() throws IOException {
		RpcClient scli = StuffPress.create();
		RpcService r = scli.getService(RpcService.class);
		StringArray sarr = new StringArray();
		sarr.arr = new String[] {"Hello ", " world.", " God."};
		int k = r.size(sarr);
		assertEquals(3, k);
		scli.stop();
	}

}
