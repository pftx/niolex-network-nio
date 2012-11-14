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

import org.apache.niolex.network.client.PacketClient;
import org.apache.niolex.network.rpc.PacketInvoker;
import org.apache.niolex.network.rpc.RpcClient;
import org.apache.niolex.network.rpc.ser.JsonConverter;

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
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		PacketClient c = new PacketClient(new InetSocketAddress("localhost", 8808));
		RpcClient client = new RpcClient(c, new PacketInvoker(), new JsonConverter());
		client.connect();

		final RpcService ser = client.getService(RpcService.class);

		int k = ser.add(3, 4, 5, 6, 7, 8, 9);
		System.out.println("42 => " + k);
		List<String> list = new ArrayList<String>();
		list.add("3");
		list.add("3");
		list.add("3");
		k = ser.size(list);
		System.out.println("3 => " + k);
		k = ser.size(null);
		System.out.println("0 => " + k);
		k = ser.add(3, 4, 5);
		System.out.println("12 => " + k);

		String s = ser.concat("Hello ", "Jiyun!");
		System.out.println("Done..... " + s);
		client.stop();
	}

}
