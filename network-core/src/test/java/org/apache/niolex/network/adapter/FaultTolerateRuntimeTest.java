/**
 * FaultTolerateRuntimeTest.java
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
package org.apache.niolex.network.adapter;

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.net.InetSocketAddress;

import org.apache.niolex.network.Config;
import org.apache.niolex.network.CoreRunner;
import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.client.SocketClient;
import org.apache.niolex.network.serialize.PacketTransformer;
import org.apache.niolex.network.serialize.StringSerializer;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-5-31
 */
@RunWith(MockitoJUnitRunner.class)
public class FaultTolerateRuntimeTest {

	@Mock
	private IPacketHandler packetHandler;
	private SocketClient client;
	PacketTransformer pt;

	@BeforeClass
	public static void run() throws Exception {
		CoreRunner.createServer();
	}

	@AfterClass
	public static void down() throws Exception {
		CoreRunner.shutdown();
	}

	/**
	 * Test method for {@link org.apache.niolex.network.serialize.PacketTransformer#getInstance()}.
	 */
	@Before
	public void testGetInstance() throws Exception {
		pt = PacketTransformer.getInstance();
		pt.addSerializer(new StringSerializer(Config.CODE_REGR_UUID));
		client = new SocketClient(new InetSocketAddress("localhost", CoreRunner.PORT));
		client.setPacketHandler(packetHandler);
		client.connect();
	}

	/**
	 * make fault tolerate work again.
	 *
	 * @throws Exception
	 */
	@Test
	public void test() throws Exception {
		final PacketData sc0 = pt.getPacketData(Config.CODE_REGR_UUID, "Fault-Tolerate-Runtime-Test");
		// 1. register SSID
		client.setAutoRead(false);
		client.handleWrite(sc0);

		// 2. write to last talk handler
		client.setAutoRead(true);
		byte[] arr = "Wre rea had to estt this project, please keep clean.".getBytes();
		final PacketData sc1 = new PacketData(4, arr);
		client.handleWrite(sc1);
		verify(packetHandler, times(1)).handlePacket(any(PacketData.class), eq(client));

		// 3. write to last talk handler
		final PacketData sc2 = new PacketData(5, arr);
		client.handleWrite(sc2);
		verify(packetHandler, times(2)).handlePacket(any(PacketData.class), eq(client));
		client.stop();

		Thread.sleep(CoreRunner.CO_SLEEP);
		System.out.println("--------------------------------------------");

		packetHandler = mock(IPacketHandler.class);
		client.setPacketHandler(packetHandler);
		client.connect();
		client.handleWrite(sc0);
		client.handleWrite(sc1);

		ArgumentCaptor<PacketData> argument = ArgumentCaptor.forClass(PacketData.class);
		verify(packetHandler, times(2)).handlePacket(argument.capture(), eq(client));
		assertArrayEquals(arr, argument.getValue().getData());
		client.stop();
	}
}
