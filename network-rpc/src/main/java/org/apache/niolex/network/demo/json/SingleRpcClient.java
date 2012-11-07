/**
 * SingleRpcClient.java
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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.niolex.network.client.SocketClient;
import org.apache.niolex.network.demo.rpc.RpcService;
import org.apache.niolex.network.rpc.SingleInvoker;
import org.apache.niolex.network.rpc.json.JsonRpcClient;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-13
 */
public class SingleRpcClient {


	public static void main(String[] arg2s) throws IOException, Throwable {
		// SocketClient c = new SocketClient(new InetSocketAddress("10.22.241.233", 8808));
		SocketClient c = new SocketClient(new InetSocketAddress("localhost", 8808));
		JsonRpcClient client = new JsonRpcClient(c, new SingleInvoker());
		client.connect();

		final RpcService ser = client.getService(RpcService.class);

		int k = ser.add(3, 4, 5, 6, 7, 8, 9);
		System.out.println("Out => " + k);
		List<String> args = new ArrayList<String>();
		args.add("3");
		args.add("3");
		args.add("3");
		k = ser.size(args);
		System.out.println("Out => " + k);
		k = ser.size(null);
		System.out.println("Out => " + k);
		k = ser.add(3, 4, 5);
		System.out.println("Out => " + k);

		Runnable r = new Runnable() {

			final int SIZE = 2212;

			@Override
			public void run() {
				int i = SIZE;
				long in = System.currentTimeMillis();
				long maxin = 0;
				while (i-- > 0) {
					long xin = System.currentTimeMillis();
					int k = ser.add(3, 4, 5, 6, 7, 8, 9, i);
					if (k != 42 + i) {
						System.out.println("Out => " + k);
					}

					List<String> args = new ArrayList<String>();
					args.add("3");
					args.add("3");
					k = ser.size(args);
					if (k != 2) {
						System.out.println("Out => " + k);
					}
					k = ser.size(null);
					assert k == 0;
					long xou = System.currentTimeMillis() - xin;
					if (xou > maxin) {
						maxin = xou;
					}
				}
				long t = System.currentTimeMillis() - in;
				System.out.println("rps => " + (SIZE * 3000 / t) + ", Max " + maxin + ", Avg " + (t / (SIZE * 3)));
			}
		};
		Thread t = new Thread(r);
		t.start();
		t.join();
		System.out.println("Join ...");
		System.out.println("Done.....");

		client.stop();
	}

}
