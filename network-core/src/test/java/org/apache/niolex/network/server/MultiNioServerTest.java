/**
 * MultiNioServerTest.java
 *
 * Copyright 2011 Niolex, Inc.
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
package org.apache.niolex.network.server;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.net.InetSocketAddress;

import org.apache.niolex.network.CoreRunner;
import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketClient;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.demo.PrintPacketHandler;
import org.apache.niolex.network.example.EchoPacketHandler;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class MultiNioServerTest {
	@Mock
	private IPacketHandler packetHandler;

	private static int port = 8908;
	private static MultiNioServer nioServer;


	@BeforeClass
	public static void createNioServer() throws Exception {
		nioServer = new MultiNioServer();
		nioServer.setPort(port);
		nioServer.setAcceptTimeOut(10);
		nioServer.start();
	}

	@AfterClass
	public static void stopNioServer() throws Exception {
		nioServer.stop();
	}

	@Before
	public void setHandler() throws Exception {
		nioServer.setPacketHandler(packetHandler);
	}

	@Test
	public void testStart() throws Exception {
		assertEquals(port, nioServer.getPort());
		nioServer.setThreadsNumber(3);
		assertEquals(3, nioServer.getThreadsNumber());
		assertEquals(packetHandler, nioServer.getPacketHandler());
		PacketClient c = new PacketClient(new InetSocketAddress("localhost", port));
        c.setPacketHandler(new PrintPacketHandler());
        c.connect();
        PacketData sc = new PacketData();
        sc.setCode((short)4);
        sc.setVersion((byte)1);
        sc.setLength(0);
        sc.setData(new byte[0]);
        c.handleWrite(sc);
        Thread.sleep(5 * CoreRunner.CO_SLEEP);
        c.stop();
        ArgumentCaptor<PacketData> argument = ArgumentCaptor.forClass(PacketData.class);
        verify(packetHandler).handleRead(argument.capture(), any(IPacketWriter.class));
        assertEquals((short)4, argument.getValue().getCode());
        assertEquals((byte)1, argument.getValue().getVersion());
        assertEquals(0, argument.getValue().getLength());
        assertEquals(0, argument.getValue().getData().length);
	}

	@Test
	public void testStop() throws Exception {
		PacketClient c = new PacketClient(new InetSocketAddress("localhost", port));
		c.setPacketHandler(new PrintPacketHandler());
		c.connect();
		PacketData sc = new PacketData();
		sc.setCode((short)4);
		sc.setVersion((byte)1);
		sc.setLength(1024 * 1024 + 6);
		sc.setData(new byte[1024 * 1024 + 6]);
		sc.getData()[6] = (byte)145;
		sc.getData()[145] = (byte)63;
		c.handleWrite(sc);
		Thread.sleep(6 * CoreRunner.CO_SLEEP);
		c.stop();
		ArgumentCaptor<PacketData> argument = ArgumentCaptor.forClass(PacketData.class);
		verify(packetHandler).handleRead(argument.capture(), any(IPacketWriter.class));
		assertEquals((short)4, argument.getValue().getCode());
		assertEquals((byte)1, argument.getValue().getVersion());
		assertEquals(1024 * 1024 + 6, argument.getValue().getLength());
		assertEquals(1024 * 1024 + 6, argument.getValue().getData().length);
		assertEquals((byte)145, argument.getValue().getData()[6]);
		assertEquals((byte)63, argument.getValue().getData()[145]);
	}

	@Test
	public void testSend() throws Exception {
		packetHandler = spy(new EchoPacketHandler());
		nioServer.setPacketHandler(packetHandler);

		PacketClient c = new PacketClient(new InetSocketAddress("localhost", port));
		IPacketHandler h = spy(new PrintPacketHandler());
		c.setPacketHandler(h);
		c.connect();
		PacketData sc = new PacketData();
		sc.setCode((short)4);
		sc.setVersion((byte)1);
		sc.setLength(1024 * 1024 + 6);
		sc.setData(new byte[1024 * 1024 + 6]);
		sc.getData()[77] = (byte)145;
		sc.getData()[145] = (byte)63;
		c.handleWrite(sc);
		Thread.sleep(5 * CoreRunner.CO_SLEEP);
		c.stop();
		ArgumentCaptor<PacketData> argument = ArgumentCaptor.forClass(PacketData.class);
		verify(packetHandler).handleRead(argument.capture(), any(IPacketWriter.class));
		assertEquals((short)4, argument.getValue().getCode());
		assertEquals((byte)1, argument.getValue().getVersion());
		assertEquals(1024 * 1024 + 6, argument.getValue().getLength());
		assertEquals(1024 * 1024 + 6, argument.getValue().getData().length);
		assertEquals((byte)145, argument.getValue().getData()[77]);
		assertEquals((byte)63, argument.getValue().getData()[145]);
		argument = ArgumentCaptor.forClass(PacketData.class);
		verify(h, times(1)).handleRead(argument.capture(), any(IPacketWriter.class));
		assertEquals((short)4, argument.getValue().getCode());
		assertEquals((byte)1, argument.getValue().getVersion());
		assertEquals(1024 * 1024 + 6, argument.getValue().getLength());
		assertEquals(1024 * 1024 + 6, argument.getValue().getData().length);
		assertEquals((byte)145, argument.getValue().getData()[77]);
		assertEquals((byte)63, argument.getValue().getData()[145]);
	}

	@Test
	public void testListen() throws Exception {
		packetHandler = spy(new EchoPacketHandler());
		nioServer.setPacketHandler(packetHandler);
		PacketClient c = new PacketClient(new InetSocketAddress("localhost", port));
		IPacketHandler h = spy(new PrintPacketHandler());
		c.setPacketHandler(h);
		c.connect();
		for (int i = 1; i < 5; ++ i) {
            PacketData sc = new PacketData();
            sc.setCode((short)i);
            sc.setVersion((byte)1);
            byte[] data = ("This is a hello world test. Round " + i).getBytes();
            sc.setLength(data.length);
            sc.setData(data);
            c.handleWrite(sc);
        }
		Thread.sleep(5 * CoreRunner.CO_SLEEP);
		c.stop();
		verify(h, times(4)).handleRead(any(PacketData.class), any(IPacketWriter.class));
		verify(packetHandler, times(4)).handleRead(any(PacketData.class), any(IPacketWriter.class));
	}

	@Test
	public void testHugeData() throws Exception {
		packetHandler = spy(new EchoPacketHandler());
		nioServer.setPacketHandler(packetHandler);
		PacketClient c = new PacketClient(new InetSocketAddress("localhost", port));
		IPacketHandler h = mock(IPacketHandler.class);
		c.setPacketHandler(h);
		c.connect();
		for (int i = 1; i < 3; ++ i) {
			PacketData sc = new PacketData();
			sc.setCode((short)i);
			sc.setVersion((byte)1);
			byte[] data = new byte[102400 + i];
			data[134] = (byte)165;
			sc.setLength(data.length);
			sc.setData(data);
			c.handleWrite(sc);
		}
		Thread.sleep(3 * CoreRunner.CO_SLEEP);
		c.stop();
		verify(h, times(2)).handleRead(any(PacketData.class), any(IPacketWriter.class));
		verify(packetHandler, times(2)).handleRead(any(PacketData.class), any(IPacketWriter.class));
	}
}
