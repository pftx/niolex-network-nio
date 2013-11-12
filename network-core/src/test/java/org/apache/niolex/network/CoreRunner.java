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

import java.net.InetSocketAddress;

import org.apache.niolex.network.adapter.FaultTolerateAdapter;
import org.apache.niolex.network.example.EchoPacketHandler;
import org.apache.niolex.network.handler.DispatchPacketHandler;
import org.apache.niolex.network.handler.SessionPacketHandler;
import org.apache.niolex.network.handler.SummaryPacketHandler;
import org.apache.niolex.network.server.NioServer;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-5-30
 */
public class CoreRunner implements Config {

	public static NioServer nioServer;
	public static boolean isOn;
	public static final int PORT = 8809;
	public static final int CO_SLEEP = 100;
	public static final InetSocketAddress SERVER_ADDR = new InetSocketAddress("localhost", PORT);

	public static void createServer() throws Exception {
		if (isOn) {
			return;
		}
		nioServer = new NioServer();
		DispatchPacketHandler handler = new DispatchPacketHandler();
		handler.addHandler((short) 2, new EchoPacketHandler());
		handler.addHandler((short) 3, new SummaryPacketHandler());
		IPacketHandler ssHandler = new SessionPacketHandler(new TLastTalkFactory());
		handler.addHandler((short) 4, ssHandler);
		handler.addHandler((short) 5, ssHandler);
		handler.addHandler((short) 6, ssHandler);

		IPacketHandler finalHandler = new FaultTolerateAdapter(handler);
		nioServer.setPacketHandler(finalHandler);
		nioServer.setPort(PORT);
		nioServer.start();
		isOn = true;
		System.out.println("CoreRunner#createServer() - " + SERVER_ENCODING);
	}

	public static void shutdown() throws Exception {
		nioServer.stop();
		isOn = false;
	}

}
