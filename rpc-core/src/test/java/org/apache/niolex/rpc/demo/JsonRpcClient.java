/**
 * JsonRpcClient.java
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
package org.apache.niolex.rpc.demo;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.niolex.commons.test.Benchmark;
import org.apache.niolex.rpc.RpcClient;
import org.apache.niolex.rpc.client.SocketClient;
import org.apache.niolex.rpc.json.JsonProtocol;
import org.junit.Assert;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-2
 */
public class JsonRpcClient {

	/**
	 * The Client Demo
	 *
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		SocketClient c = new SocketClient(new InetSocketAddress("localhost", 8808));
//		NioClient c = new NioClient();
//		c.setConnectionNumber(10);
//		c.setServerAddress("localhost:8808");
		RpcClient client = new RpcClient(c);
		client.setClientProtocol(new JsonProtocol());
		client.connect();

		final RpcService ser = client.getService(RpcService.class);

		int k = ser.benchmark(Benchmark.makeBenchmark(), "This is client.").i;
		System.out.println("benchmark => " + k);

		List<String> list = new ArrayList<String>();
		list.add("3");
		list.add("3");
		list.add("3");
		k = ser.size(list.toArray(new String[3])).i;
		System.out.println("3 => " + k);
		k = ser.size(null).i;
		System.out.println("0 => " + k);

		String s = ser.concat("Hello ", "Jiyun!");
		System.out.println("concat => " + s);
		try {
			ser.throwEx();
		} catch (Exception e) {
			System.out.println("--------------#throwEx()-------------------");
			Assert.assertEquals("Demo ex throw from #throwEx()", e.getCause().getCause().getMessage());
		}
		try {
			ser.testMe();
		} catch (Exception e) {
			System.out.println("--------------#testMe()--------------------");
			Assert.assertEquals("The method you want to invoke is not a remote procedure call.", e.getMessage());
		}
		System.out.println("Done.5.methods... client will stop.");
		client.stop();
	}

}
