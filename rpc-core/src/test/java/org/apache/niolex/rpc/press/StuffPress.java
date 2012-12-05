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

import org.apache.niolex.commons.test.Counter;
import org.apache.niolex.commons.test.StopWatch;
import org.apache.niolex.commons.test.StopWatch.Stop;
import org.apache.niolex.rpc.RpcClient;
import org.apache.niolex.rpc.client.SocketClient;
import org.apache.niolex.rpc.demo.RpcService;
import org.apache.niolex.rpc.stuff.StuffProtocol;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-8-11
 */
public class StuffPress {

	static int SIZE = 51200;
	static int THREAD_NUM = 5;
	static int SHUFFLE_NUM = 50;
	static final StopWatch stopWatch = new StopWatch(1);
	static final Counter ERROR_CNT = new Counter();

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
			Thread.yield();
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
		SocketClient c = new SocketClient(new InetSocketAddress("localhost", 8808));
		RpcClient client = new RpcClient(c, new StuffProtocol());
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
			int lennn = a.length() + b.length();
			while (i-- > 0) {
				Stop s;
				// -------------------------
				s = stopWatch.start();
				String c = service.concat(a, b);
				if (c.length() != lennn) {
					ERROR_CNT.inc();
					System.out.println("Concat => " + c);
				}
				s.stop();
				Thread.yield();
			}
			cli.stop();
		}
	}
}
