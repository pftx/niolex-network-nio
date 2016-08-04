/**
 * DemoJsonRpcClient.java
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
package org.apache.niolex.network.demo.json;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.niolex.commons.test.Check;
import org.apache.niolex.network.client.PacketClient;
import org.apache.niolex.network.rpc.cli.BaseInvoker;
import org.apache.niolex.network.rpc.cli.BlockingStub;
import org.apache.niolex.network.rpc.conv.JsonConverter;

/**
 * Demo client
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0, Date: 2012-6-2
 */
public class DemoJsonRpcClient {

	/**
	 * The Client Demo
	 *
	 * @param args command line arguments
	 * @throws Exception if necessary
	 */
	public static void main(String[] args) throws Exception {
		PacketClient c = new PacketClient(new InetSocketAddress("localhost", 8808));
        BaseInvoker invoker = new BaseInvoker(c);
        invoker.connect();
        BlockingStub client = new BlockingStub(invoker, new JsonConverter());

		final RpcService ser = client.getService(RpcService.class);

		int k = ser.add(3, 4, 5, 6, 7, 8, 9);
		System.out.println("42 => " + k);
		Check.eq(42, k, "ser.add");
		List<String> list = new ArrayList<String>();
		list.add("3");
		list.add("3");
		list.add("3");
		k = ser.size(list);
		System.out.println("3 => " + k);
		Check.eq(3, k, "ser.size");
		k = ser.size(null);
		System.out.println("NULL => " + k);
		Check.eq(-1, k, "ser.size");
		k = ser.add(3, 4, 5);
		System.out.println("12 => " + k);
		Check.eq(12, k, "ser.add");

		String s = ser.concat("Hello ", "Jiyun!");
		System.out.println("Done..... " + s);
		Check.isTrue(s.equals("Hello Jiyun!"));
        invoker.stop();
	}

}
