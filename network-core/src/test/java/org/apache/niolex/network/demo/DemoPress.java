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
package org.apache.niolex.network.demo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.LinkedList;

import org.apache.niolex.commons.test.Counter;
import org.apache.niolex.commons.test.MockUtil;
import org.apache.niolex.commons.test.StopWatch;
import org.apache.niolex.commons.test.StopWatch.Stop;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.client.BaseClient;
import org.apache.niolex.network.client.SocketClient;
import org.apache.niolex.network.example.SavePacketHandler;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-8-11
 */
public class DemoPress {

	static int SIZE = 1024;
	static int THREAD_NUM = 5;
	static int SHUFFLE_NUM = 50;
	static final StopWatch stopWatch = new StopWatch(1);
	static final Counter ERROR_CNT = new Counter();

	public static boolean equals2(PacketData a, PacketData b) {
		byte[] b1 = a.getData();
		byte[] b2 = b.getData();
		if (b1.length == b2.length) {
			int k = MockUtil.ranInt(b1.length);
			if (b1[k] != b2[k]) {
				return false;
			}
			k = MockUtil.ranInt(b1.length);
			if (b1[k] != b2[k]) {
				return false;
			}
			k = MockUtil.ranInt(b1.length);
			if (b1[k] != b2[k]) {
				return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws Exception {
		if (args != null && args.length != 0) {
			SIZE = Integer.parseInt(args[0]);
			THREAD_NUM = Integer.parseInt(args[1]);
			SHUFFLE_NUM = Integer.parseInt(args[2]);
        }
		DemoServer.main(null);
		for (int i = 0; i < 10; ++i) {
			BaseClient cli = create();
			LinkedList<PacketData> list = new LinkedList<PacketData>();
			cli.setPacketHandler(new SavePacketHandler(list));
			Stop s = stopWatch.start();
			PacketData pac = new PacketData(2, MockUtil.randByteArray(57));
			cli.handleWrite(pac);
			PacketData par = list.poll();
			s.stop();
			if (!equals2(pac, par)) {
				throw new Exception("Not OK!");
			}
			cli.stop();
		}
		Thread[] ts = new Thread[THREAD_NUM];
		for (int i = 0; i < THREAD_NUM; ++i) {
			BaseClient cli = create();
			Runner r = new Runner(cli, "Hello " + i, " world.");
			Thread t = new Thread(r);
			t.start();
			ts[i] = t;
		}
		stopWatch.begin(true);
		for (int i = 0; i < SHUFFLE_NUM; ++i) {
			BaseClient cli = create();
			LinkedList<PacketData> list = new LinkedList<PacketData>();
			cli.setPacketHandler(new SavePacketHandler(list));
			Stop s = stopWatch.start();
			PacketData pac = new PacketData(2, MockUtil.randByteArray(27));
			cli.handleWrite(pac);
			PacketData par = list.poll();
			s.stop();
			if (!equals2(pac, par)) {
				throw new Exception("Not OK!");
			}
			cli.stop();
			Thread.yield();
		}
		for (int i = 0; i < THREAD_NUM; ++i) {
			ts[i].join();
			System.out.println("Join ... " + i);
		}
		stopWatch.done();
		stopWatch.print();
		System.out.println("Done..... error = " + ERROR_CNT.cnt());
		DemoServer.stop();
	}

	public static SocketClient create() throws IOException {
		SocketClient c = new SocketClient(new InetSocketAddress("localhost", 8808));
		c.connect();
		return c;
	}

	public static class Runner implements Runnable {
		BaseClient cli;
		LinkedList<PacketData> list;
		String a = "hello ", b = "world!";

		public Runner(BaseClient cli, String a, String b) {
			super();
			this.cli = cli;
			list = new LinkedList<PacketData>();
			cli.setPacketHandler(new SavePacketHandler(list));
			this.a = a;
			this.b = b;
		}

		@Override
		public void run() {
			int i = SIZE;
			PacketData pac;
			while (i-- > 0) {
				Stop s = stopWatch.start();
				pac = new PacketData(2, MockUtil.randByteArray(266));
				cli.handleWrite(pac);
				PacketData par = list.poll();
				s.stop();
				if (!equals2(pac, par)) {
					ERROR_CNT.inc();
					System.out.println("Out => " + 1);
				}
				// -------------------------
				s = stopWatch.start();
				pac = new PacketData(2, MockUtil.randByteArray(166));
				cli.handleWrite(pac);
				par = list.poll();
				s.stop();
				if (!equals2(pac, par)) {
					ERROR_CNT.inc();
					System.out.println("Out => " + 2);
				}
				// -------------------------
				s = stopWatch.start();
				pac = new PacketData(2, MockUtil.randByteArray(66));
				cli.handleWrite(pac);
				par = list.poll();
				s.stop();
				Thread.yield();
			}
			cli.stop();
		}
	}
}
