/**
 * FaultTolerateTest.java
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
package org.apache.niolex.network.handler;

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.net.InetSocketAddress;

import org.apache.niolex.network.Config;
import org.apache.niolex.network.CoreRunner;
import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.PacketClient;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.packet.PacketTransformer;
import org.apache.niolex.network.packet.StringSerializer;
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
 * @Date: 2012-5-31
 */
@RunWith(MockitoJUnitRunner.class)
public class FaultTolerateTest {

	@Mock
	private IPacketHandler packetHandler;
	private PacketClient packetClient;
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
	 * Test method for {@link org.apache.niolex.network.packet.PacketTransformer#getInstance()}.
	 */
	@Before
	public void testGetInstance() {
		pt = PacketTransformer.getInstance();
		pt.addSerializer(new StringSerializer(Config.CODE_SESSN_REGR));
	}

	@Test
	public void test() throws Exception {
		packetClient = new PacketClient(new InetSocketAddress("localhost", CoreRunner.PORT));
		packetClient.setPacketHandler(packetHandler);
		packetClient.connect();

		final PacketData sc0 = pt.getPacketData(Config.CODE_SESSN_REGR, "FaultTolerateTest");
		packetClient.handleWrite(sc0);
		verify(packetHandler, never()).handleRead(any(PacketData.class), eq(packetClient));

		byte[] arr = "Wre rea had to estt this project, please keep clean.".getBytes();
		final PacketData sc1 = new PacketData(4, arr);
		packetClient.handleWrite(sc1);
		Thread.sleep(200);

		verify(packetHandler, times(1)).handleRead(any(PacketData.class), eq(packetClient));
		final PacketData sc2 = new PacketData(5, arr);
		packetClient.handleWrite(sc2);
		Thread.sleep(20);
		packetClient.stop();

		Thread.sleep(500);
		System.out.println("--------------------------------------------");
		packetHandler = mock(IPacketHandler.class);
		packetClient.setPacketHandler(packetHandler);
		packetClient.connect();
		packetClient.handleWrite(sc0);
		Thread.sleep(500);

		ArgumentCaptor<PacketData> argument = ArgumentCaptor.forClass(PacketData.class);
		verify(packetHandler, times(2)).handleRead(argument.capture(), eq(packetClient));
		assertArrayEquals(arr, argument.getValue().getData());
		packetClient.stop();
	}
}
