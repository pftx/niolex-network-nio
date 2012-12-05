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
package org.apache.niolex.rpc.press;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.niolex.commons.test.Benchmark;
import org.apache.niolex.commons.test.Counter;
import org.apache.niolex.commons.test.MockUtil;
import org.apache.niolex.commons.test.StopWatch;
import org.apache.niolex.commons.test.StopWatch.Stop;
import org.apache.niolex.commons.util.SystemUtil;
import org.apache.niolex.rpc.RpcClient;
import org.apache.niolex.rpc.client.SocketClient;
import org.apache.niolex.rpc.demo.RpcService;
import org.apache.niolex.rpc.demo.RpcService.IntArray;
import org.apache.niolex.rpc.json.JsonProtocol;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-8-11
 */
public class RpcPress {

	static int SIZE = 10240;
	static int THREAD_NUM = 5;
	static int SHUFFLE_NUM = 50;
	static final StopWatch stopWatch = new StopWatch(1);
	static final Counter ERROR_CNT = new Counter();
	static String ADDR = "localhost";

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws Exception {
		// Set some parameter.
		if (args != null && args.length != 0) {
			SIZE = Integer.parseInt(args[0]);
			THREAD_NUM = Integer.parseInt(args[1]);
			SHUFFLE_NUM = Integer.parseInt(args[2]);
			ADDR = args[3];
        }
		// Warm server.
		for (int i = 0; i < 10; ++i) {
			RpcClient cli = create();
			RpcService service = cli.getService(RpcService.class);
			Stop s = stopWatch.start();
			String ok = service.concat("Hello " + i, " world.");
			s.stop();
			if (ok.length() < 14) {
				throw new Exception("OK " + ok + ", i " + i);
			}
			cli.stop();
			Thread.yield();
		}
		// Start many threads to run press test.
		Thread[] ts = new Thread[THREAD_NUM];
		for (int i = 0; i < THREAD_NUM; ++i) {
			RpcClient cli = create();
			Rpc r = new Rpc(cli, "Hello " + i, " world.");
			Thread t = new Thread(r);
			t.start();
			ts[i] = t;
		}
		stopWatch.begin(true);
		// Shuffle, call some function to disturb the main testing threads.
		for (int i = 0; i < SHUFFLE_NUM; ++i) {
			RpcClient cli = create();
			RpcService service = cli.getService(RpcService.class);
			Stop s = stopWatch.start();
			String ok = service.concat("Hello " + i, " world.");
			s.stop();
			if (ok.length() < 14) {
				throw new Exception("OK " + ok + ", i " + i);
			}
			cli.stop();
			SystemUtil.sleep(1);
		}
		// Waiting for done.
		for (int i = 0; i < THREAD_NUM; ++i) {
			ts[i].join();
			System.out.println("Join ... " + i);
		}
		stopWatch.done();
		stopWatch.print();
		System.out.println("Done..... error = " + ERROR_CNT.cnt());
	}

	public static RpcClient create() throws IOException {
		SocketClient c = new SocketClient(new InetSocketAddress(ADDR, 8808));
		RpcClient client = new RpcClient(c, new JsonProtocol());
		client.connect();
		return client;
	}

	public static class Rpc implements Runnable {
		RpcClient cli;
		RpcService service;
		String a = "hello ", b = "world!";

		public Rpc(RpcClient cli) {
			super();
			this.cli = cli;
			this.service = cli.getService(RpcService.class);
		}

		public Rpc(RpcClient cli, String a, String b) {
			super();
			this.cli = cli;
			this.service = cli.getService(RpcService.class);
			this.a = a;
			this.b = b;
		}

		@Override
		public void run() {
			int i = SIZE;
			Benchmark ben = Benchmark.makeBenchmark();
			String str = "This is client.";
			int connn = str.length() + ben.getClassId();
			int lennn = a.length() + b.length();
			int z = MockUtil.ranInt(10240);
            int y = MockUtil.ranInt(10240);
            int x = MockUtil.ranInt(10240);
			while (i-- > 0) {
				Stop s = stopWatch.start();
				ben.setPriv(i);
				int k = service.benchmark(ben, str).i;
				s.stop();
				if (k != connn + i) {
					ERROR_CNT.inc();
					System.out.println("Benchmark => " + k);
				}
				// -------------------------
				List<String> args = new ArrayList<String>();
				args.add("3");
				args.add("3");
				s = stopWatch.start();
				k = service.size(args.toArray(new String[2])).i;
				s.stop();
				if (k != 2) {
					ERROR_CNT.inc();
					System.out.println("Size => " + k);
				}
				// -------------------------
				s = stopWatch.start();
				String c = service.concat(a, b);
				if (c.length() != lennn) {
					ERROR_CNT.inc();
					System.out.println("Concat => " + c);
				}
				s.stop();
				// -------------------------
                s = stopWatch.start();
                int m = service.sum(new IntArray(x, y, z, i)).i;
                if (m != x + y + z + i) {
                    ERROR_CNT.inc();
                    System.out.println("Concat => " + c);
                }
                s.stop();
			}
			cli.stop();
		}
	}
}
