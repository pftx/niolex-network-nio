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

import org.apache.niolex.network.adapter.FaultTolerateAdapter;
import org.apache.niolex.network.example.EchoPacketHandler;
import org.apache.niolex.network.handler.DispatchPacketHandler;
import org.apache.niolex.network.handler.SessionPacketHandler;
import org.apache.niolex.network.handler.SummaryPacketHandler;
import org.apache.niolex.network.rpc.RpcUtil;
import org.apache.niolex.network.server.NioServer;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-5-30
 */
public class CoreRunner {

	public static NioServer nioServer;
	public static boolean isOn;
	public static final int PORT = 8809;
	public static final int CO_SLEEP = 100;

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
		isOn = true;
	}

	public static void shutdown() throws Exception {
		nioServer.stop();
		isOn = false;
	}

	public static void main(String[] args) throws Exception {
		short a = (short) 0xcffc;
		int q = a << 16;
		System.out.println(Integer.toHexString(q));
		byte b = -4;
		q += (b & 0xFF) << 8;
		System.out.println(Integer.toHexString(q));
		byte c = -11;
		q += (c & 0xFF);
		System.out.println(Integer.toHexString(q));
		int l = RpcUtil.generateKey(a, b, c);
		System.out.println(Integer.toHexString(l));

		byte d = -4;
		System.out.println(d % 2 == 0);
		System.out.println(d & 0xFF);
		if (d % 2 == 0) {
			--d;
		}
		System.out.println(d & 0xFF);
		System.out.println("--");
		d = 8;
		byte x = -1, y = 126;
		while (d-- > 0) {
			x = y++;
			int z = y - x;
			int s = 1;
			byte m = (byte) (y + s);
			System.out.println(y + " " + (z) + " " + (x - y) + " " + (m));
		}
	}
}
