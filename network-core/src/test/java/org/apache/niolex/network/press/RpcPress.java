/**
 * RpcPress.java
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
package org.apache.niolex.network.press;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.niolex.commons.test.Counter;
import org.apache.niolex.network.client.PacketClient;
import org.apache.niolex.network.demo.rpc.RpcServer;
import org.apache.niolex.network.demo.rpc.RpcService;
import org.apache.niolex.network.rpc.PacketInvoker;
import org.apache.niolex.network.rpc.json.JsonRpcClient;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-8-11
 */
public class RpcPress {

	static final int SIZE = 2212;
	static final int THREAD_NUM = 50;

	static final Counter ERROR_CNT = new Counter();

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws Exception {
		RpcServer.main(args);
		Thread[] ts = new Thread[THREAD_NUM];
		JsonRpcClient[] cs = new JsonRpcClient[THREAD_NUM];
		for (int i = 0; i < THREAD_NUM; ++i) {
			JsonRpcClient cli = create();
			RpcService service = cli.getService(RpcService.class);
			Rpc r = new Rpc(service, "Hello " + i, " world.");
			Thread t = new Thread(r);
			t.start();
			ts[i] = t;
			cs[i] = cli;
		}
		long in = System.currentTimeMillis();
		for (int i = 0; i < 1000; ++i) {
			JsonRpcClient cli = create();
			RpcService service = cli.getService(RpcService.class);
			String ok = service.concat("Hello " + i, " world.");
			if (ok.length() < 14) {
				throw new Exception("OK " + ok + ", i " + i);
			}
			cli.stop();
			Thread.yield();
		}
		for (int i = 0; i < THREAD_NUM; ++i) {
			ts[i].join();
			cs[i].stop();
			System.out.println("Join ... " + i);
		}
		long xou = System.currentTimeMillis() - in;
		System.out.println("Total Rps => " + ((SIZE * THREAD_NUM * 3000 + 1000) / xou));
		System.out.println("Done....., error " + ERROR_CNT.cnt());
	}

	public static JsonRpcClient create() throws IOException {
		// PacketClient c = new PacketClient(new InetSocketAddress("10.22.241.233", 8808));
		PacketClient c = new PacketClient(new InetSocketAddress("localhost", 8808));
		JsonRpcClient client = new JsonRpcClient(c, new PacketInvoker());
		client.connect();
		return client;
	}

	public static class Rpc implements Runnable {
		RpcService service;
		String a = "hello ", b = "world!";

		public Rpc(RpcService service, String a, String b) {
			super();
			this.service = service;
			this.a = a;
			this.b = b;
		}

		@Override
		public void run() {
			int i = SIZE;
			long in = System.currentTimeMillis();
			long maxin = 0;
			while (i-- > 0) {
				long xin = System.currentTimeMillis();
				int k = service.add(3, 4, 5, 6, 7, 8, 9, i);
				if (k != 42 + i) {
					ERROR_CNT.inc();
					System.out.println("Out => " + k);
				}
				// -------------------------
				List<String> args = new ArrayList<String>();
				args.add("3");
				args.add("3");
				k = service.size(args);
				if (k != 2) {
					ERROR_CNT.inc();
					System.out.println("Out => " + k);
				}
				// -------------------------
				String c = service.concat(a, b);
				if (c.length() < 64000) {
					if (a.length() < b.length()) {
						a = c;
					} else {
						b = c;
					}
				}
				long xou = System.currentTimeMillis() - xin;
				if (xou > maxin) {
					maxin = xou;
				}
				Thread.yield();
			}
			long t = System.currentTimeMillis() - in;
			System.out.println("rps => " + (SIZE * 3000 / t) + ", Max " + maxin + ", Avg " + (t / (SIZE * 3)));
		}
	}
}
