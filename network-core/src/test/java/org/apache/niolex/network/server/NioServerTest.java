/**
 * NioServerTest.java
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
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;

import org.apache.niolex.commons.reflect.FieldUtil;
import org.apache.niolex.network.CoreRunner;
import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.client.PacketClient;
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


/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-5-28
 */
@RunWith(MockitoJUnitRunner.class)
public class NioServerTest {
	@Mock
	private IPacketHandler packetHandler;

	private static int port = 9806;
	private static NioServer nioServer;

	@BeforeClass
	public static void createNioServer() throws Exception {
		nioServer = new NioServer();
		nioServer.setPort(port);
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
    public void testCannotStart() {
        MultiNioServer nioServer  = new MultiNioServer(3);
        nioServer.setPort(-1);
        nioServer.setAcceptTimeOut(10);
        nioServer.start();
        nioServer.stop();
        nioServer.stop();
    }

	@Test
	public void testSetter() throws Exception {
		nioServer.setAcceptTimeOut(6233);
		assertEquals(6233, nioServer.getAcceptTimeOut());
		assertEquals(port, nioServer.getPort());
		assertEquals(packetHandler, nioServer.getPacketHandler());
	}

	@Test
	public void testStart() throws Exception {
		PacketClient c = new PacketClient(new InetSocketAddress("localhost", port));
        c.setPacketHandler(new PrintPacketHandler());
        c.connect();
        PacketData sc = new PacketData();
        sc.setCode((short)4);
        sc.setVersion((byte)1);
        sc.setLength(0);
        sc.setData(new byte[0]);
        c.handleWrite(sc);
        Thread.sleep(3 * CoreRunner.CO_SLEEP);
        c.stop();
        ArgumentCaptor<PacketData> argument = ArgumentCaptor.forClass(PacketData.class);
        verify(packetHandler).handlePacket(argument.capture(), any(IPacketWriter.class));
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
		Thread.sleep(5 * CoreRunner.CO_SLEEP);
		c.stop();
		ArgumentCaptor<PacketData> argument = ArgumentCaptor.forClass(PacketData.class);
		verify(packetHandler).handlePacket(argument.capture(), any(IPacketWriter.class));
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
		verify(packetHandler).handlePacket(argument.capture(), any(IPacketWriter.class));
		assertEquals((short)4, argument.getValue().getCode());
		assertEquals((byte)1, argument.getValue().getVersion());
		assertEquals(1024 * 1024 + 6, argument.getValue().getLength());
		assertEquals(1024 * 1024 + 6, argument.getValue().getData().length);
		assertEquals((byte)145, argument.getValue().getData()[77]);
		assertEquals((byte)63, argument.getValue().getData()[145]);
		argument = ArgumentCaptor.forClass(PacketData.class);
		verify(h, times(1)).handlePacket(argument.capture(), any(IPacketWriter.class));
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
		verify(h, times(4)).handlePacket(any(PacketData.class), any(IPacketWriter.class));
		verify(packetHandler, times(4)).handlePacket(any(PacketData.class), any(IPacketWriter.class));
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
		verify(h, times(2)).handlePacket(any(PacketData.class), any(IPacketWriter.class));
		verify(packetHandler, times(2)).handlePacket(any(PacketData.class), any(IPacketWriter.class));
	}

    @Test
    public void testRun() throws Exception {
        NioServer ns = new NioServer();
        ns.isListening = true;
        ns.run();
    }

    @Test
    public void testStopNA() throws Exception {
        NioServer ns = new NioServer();
        ns.stop();
    }

    @Test
    public void testStopEx() throws Exception {
        NioServer ns = new NioServer();
        ns.isListening = true;
        ns.stop();
    }

    @Test
    public void testHandleKey() throws Exception {
        SelectionKey selectionKey = mock(SelectionKey.class);
        when(selectionKey.readyOps()).thenReturn(SelectionKey.OP_ACCEPT | SelectionKey.OP_READ);
        nioServer.handleKey(selectionKey);
    }

    @Test
    public void testHandleKeyInvalidRead() throws Exception {
        SelectionKey selectionKey = mock(SelectionKey.class);
        when(selectionKey.readyOps()).thenReturn(SelectionKey.OP_READ);
        when(selectionKey.isValid()).thenReturn(false);
        nioServer.handleKey(selectionKey);
    }

    @Test
    public void testHandleKeyCancelled() throws Exception {
        SelectionKey selectionKey = mock(SelectionKey.class);
        when(selectionKey.readyOps()).thenThrow(new CancelledKeyException());
        when(selectionKey.isValid()).thenReturn(false);
        nioServer.handleKey(selectionKey);
    }

    @Test
    public void testHandleKeyClosed() throws Exception {
        NioServer ns = new NioServer();
        Field f = FieldUtil.getField(NioServer.class, "ss");
        ServerSocketChannel ss = mock(ServerSocketChannel.class);
        FieldUtil.setFieldValue(f, ns, ss);
        when(ss.accept()).thenThrow(new ClosedChannelException());

        SelectionKey selectionKey = mock(SelectionKey.class);
        when(selectionKey.readyOps()).thenReturn(SelectionKey.OP_ACCEPT);
        when(selectionKey.isValid()).thenReturn(false);

        nioServer.handleKey(selectionKey);
    }

    @Test
    public void testHandleKeyOther() throws Exception {
        SelectionKey selectionKey = mock(SelectionKey.class);
        when(selectionKey.readyOps()).thenThrow(new IllegalArgumentException("test"));
        when(selectionKey.isValid()).thenReturn(false);
        nioServer.handleKey(selectionKey);
    }

}
