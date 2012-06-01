/**
 * CoreRunner.java
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
package org.apache.niolex.network;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.niolex.network.adapter.FaultTolerateAdapter;
import org.apache.niolex.network.example.EchoPacketHandler;
import org.apache.niolex.network.handler.DispatchPacketHandler;
import org.apache.niolex.network.handler.SessionPacketHandler;
import org.apache.niolex.network.handler.SummaryPacketHandler;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-5-30
 */
public class CoreRunner {

	public static NioServer nioServer;
	public static Thread thread;
	public static boolean isOn;
	public static final int PORT = 8809;

	public static void createServer() throws Exception {
		if (isOn) {
			return;
		}
		nioServer = new NioServer();
		DispatchPacketHandler handler = new DispatchPacketHandler();
		handler.addHandler((short) 2, new EchoPacketHandler());
		handler.addHandler((short) 3, new SummaryPacketHandler());
		handler.addHandler((short) 4, new SessionPacketHandler(new TLastTalkFactory()));
		handler.addHandler((short) 5, new SessionPacketHandler(new TLastTalkFactory()));

		IPacketHandler finalHandler = new FaultTolerateAdapter(handler);
		nioServer.setPacketHandler(finalHandler);
		nioServer.setPort(PORT);
		nioServer.start();
		thread = new Thread(nioServer);
		thread.start();
		isOn = true;
	}

	public static void shutdown() throws Exception {
		nioServer.stop();
		thread.join();
		isOn = false;
	}

	public static void main(String[] args) throws Exception {
		String c = null;
		Object a = c;
		Integer b = (Integer) a;
		System.out.println(b);
		//createServer();
		ConcurrentLinkedQueue<Integer> inn = new ConcurrentLinkedQueue<Integer>();
		Iterator<Integer> it = inn.iterator();
		inn.add(4);
		inn.add(6);
		System.out.println(it.next());
		System.out.println(it.next());
		inn.add(8);
		System.out.println(it.next());
	}
}
