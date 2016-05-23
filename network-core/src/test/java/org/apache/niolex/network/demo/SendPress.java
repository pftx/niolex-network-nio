/**
 * SendPress.java
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
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.niolex.commons.test.MockUtil;
import org.apache.niolex.commons.test.StopWatch;
import org.apache.niolex.commons.test.StopWatch.Stop;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.client.BaseClient;
import org.apache.niolex.network.client.BlockingClient;
import org.apache.niolex.network.client.SocketClient;
import org.apache.niolex.network.example.SavePacketHandler;

/**
 * Packet send to server, then discarded.
 * 
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-8-11
 */
public class SendPress {

    static final StopWatch STOP_WATCH = new StopWatch(1);
    static final AtomicInteger RECV_CNT = new AtomicInteger();

    static int BUF_SIZE = 512;
	static int RUN_SIZE = 10000;
	static int THREAD_NUM = 5;

	/**
	 * The main.
	 *
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws Exception {
	    DemoUtil.parseArgs(args);
	    BUF_SIZE = DemoUtil.BUF_SIZE;
	    RUN_SIZE = DemoUtil.TIMEOUT;
	    if (DemoUtil.POOL_SIZE != 0)
	        THREAD_NUM = DemoUtil.POOL_SIZE;
	    System.out.println("Thread number [" + THREAD_NUM + "], Buffer size [" +
	            BUF_SIZE + "], Send packets [" + RUN_SIZE * THREAD_NUM + "].");
	    // 1 test one run.
		for (int i = 0; i < 10; ++i) {
			BaseClient cli = create();
			LinkedList<PacketData> list = new LinkedList<PacketData>();
			cli.setPacketHandler(new SavePacketHandler(list));
			Stop s = STOP_WATCH.start();
			PacketData pac = new PacketData(2, MockUtil.randByteArray(BUF_SIZE));
			cli.handleWrite(pac);
			PacketData par = list.poll();
			s.stop();
			if (!equals2(pac, par)) {
				throw new Exception("Not OK when init!");
			}
			cli.stop();
		}
		// 2 start threads.
		Thread[] ts = new Thread[THREAD_NUM];
		Runner[] rn = new Runner[THREAD_NUM];
		for (int i = 0; i < THREAD_NUM; ++i) {
		    rn[i] = new Runner();
			Thread t = new Thread(rn[i], "runner-" + i);
			t.start();
			ts[i] = t;
		}
		STOP_WATCH.begin(true);
		for (int i = 0; i < THREAD_NUM; ++i) {
			ts[i].join();
			System.out.println("Join ... " + i);
			rn[i].stop();
		}
		STOP_WATCH.done();
		STOP_WATCH.print();
		System.out.println("Done..... , recv = " + RECV_CNT.get() + ", send = " + RUN_SIZE * THREAD_NUM);
	}

	public static SocketClient create() throws IOException {
		SocketClient c = new SocketClient(new InetSocketAddress(DemoUtil.HOST, DemoUtil.PORT));
		c.connect();
		return c;
	}

    public static boolean equals2(PacketData p1, PacketData p2) {
        byte[] a = p1.getData();
        byte[] b = p2.getData();
        if (a.length == b.length) {
            int inc = a.length / 100 + 1;
            for (int k = 0; k < a.length; k += inc) {
                if (a[k] != b[k]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

	public static class Runner implements Runnable {
	    BlockingClient cli;

		public Runner() throws IOException {
			this.cli = new BlockingClient(new InetSocketAddress(DemoUtil.HOST, DemoUtil.PORT));
			cli.setPacketHandler(new PrintPacketHandler());
			cli.connect();
		}

		@Override
		public void run() {
			int i = RUN_SIZE;
			PacketData pac;
			while (i-- > 0) {
				pac = new PacketData(101, MockUtil.randByteArray(BUF_SIZE));
				Stop s = STOP_WATCH.start();
				cli.handleWrite(pac);
				s.stop();
			}
			stop();
		}

		public void stop() {
		    cli.stop();
		}
	}
}
