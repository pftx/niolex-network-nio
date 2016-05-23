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

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;
import java.util.LinkedList;

import org.apache.niolex.commons.concurrent.ThreadUtil;
import org.apache.niolex.commons.reflect.FieldUtil;
import org.apache.niolex.commons.test.Counter;
import org.apache.niolex.commons.test.MockUtil;
import org.apache.niolex.commons.util.Const;
import org.apache.niolex.network.CoreRunner;
import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.client.PacketClient;
import org.apache.niolex.network.demo.PrintPacketHandler;
import org.apache.niolex.network.example.EchoPacketHandler;
import org.apache.niolex.network.example.SavePacketHandler;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;


@RunWith(MockitoJUnitRunner.class)
public class MultiNioServerTest {
	@Mock
	private IPacketHandler packetHandler;

	private static int port = 8908;
	private static MultiNioServer nioServer;


	@BeforeClass
	public static void createNioServer() throws Exception {
		nioServer = new MultiNioServer();
		nioServer.setThreadsNumber(3);
		nioServer.setPort(port);
		nioServer.setAcceptTimeout(100);
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
        verify(packetHandler).handlePacket(argument.capture(), any(IPacketWriter.class));
        assertEquals((short)4, argument.getValue().getCode());
        assertEquals((byte)1, argument.getValue().getVersion());
        assertEquals(0, argument.getValue().getLength());
        assertEquals(0, argument.getValue().getData().length);
	}
	
	@Test
	public void testStartWithError() throws Exception {
	    final SelectorProvider p = SelectorProvider.provider();
	    SelectorProvider m = mock(SelectorProvider.class);
	    when(m.openSocketChannel()).thenReturn(p.openSocketChannel());
	    when(m.openServerSocketChannel()).thenReturn(p.openServerSocketChannel());
	    
	    final Counter c = new Counter();
	    when(m.openSelector()).thenAnswer(new Answer<AbstractSelector>(){

            @Override
            public AbstractSelector answer(InvocationOnMock invocation) throws Throwable {
                if (c.cnt() > 0) {
                    throw new IOException("test.");
                } else {
                    c.inc();
                    return p.openSelector();
                }
            }});
	    
	    FieldUtil.setValue(p, "provider", m);
	    
	    MultiNioServer mns = new MultiNioServer();
	    mns.setPort(9091);
	    
	    assertFalse(mns.start());
	    mns.stop();
	    FieldUtil.setValue(p, "provider", p);
	}
	
	@Test
	public void testSelectWithError() throws Exception {
	    final SelectorProvider p = SelectorProvider.provider();
	    SelectorProvider m = mock(SelectorProvider.class);
	    when(m.openSocketChannel()).thenReturn(p.openSocketChannel());
	    when(m.openServerSocketChannel()).thenReturn(p.openServerSocketChannel());
	    final Counter c = new Counter();
	    when(m.openSelector()).thenAnswer(new Answer<AbstractSelector>(){
	        @Override
	        public AbstractSelector answer(InvocationOnMock invocation) throws Throwable {
	            if (c.cnt() == 0) {
	                c.inc();
	                return p.openSelector();
	            }
	            
	            AbstractSelector s = mock(AbstractSelector.class);
	            when(s.select(anyLong())).thenThrow(new IOException("Hello."));
	            return s;
	        }});
	    
	    FieldUtil.setValue(p, "provider", m);
	    
	    MultiNioServer mns = new MultiNioServer();
	    mns.setPort(9091);
	    
	    assertTrue(mns.start());
	    int i = 10;
	    while (i-- > 0) {
	        ThreadUtil.sleepAtLeast(i);
	        if (!mns.isListening) break;
	    }
	    assertFalse(mns.isListening);
	    mns.stop();
	    FieldUtil.setValue(p, "provider", p);
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

    @Test(expected=IllegalStateException.class)
    public void testSetThreadsNumber() throws Exception {
        assertEquals(3, nioServer.getThreadsNumber());
        nioServer.setThreadsNumber(3);
    }

    @Test
    public void testMultiNioServerInt() throws Exception {
        MultiNioServer nms = new MultiNioServer(100);
        assertEquals(100, nms.getThreadsNumber());
    }

    @Test
    public void testComposite() throws Exception {
        LinkedList<PacketData> svr = new LinkedList<PacketData>();
        IPacketHandler serverH = new EchoPacketHandler();
        LinkedList<PacketData> cli = new LinkedList<PacketData>();
        IPacketHandler clientH = new SavePacketHandler(cli);

        nioServer.setPacketHandler(serverH);
        PacketClient c = new PacketClient(new InetSocketAddress("localhost", port));
        c.setPacketHandler(clientH);
        c.connect();

        for (int i = 10; i < Const.M; i *= MockUtil.randInt(1, 10)) {
            PacketData sc = new PacketData(MockUtil.randInt(1, 500), MockUtil.randByteArray(i));
            c.handleWrite(sc);
            svr.add(sc);
        }

        int i = 100, s = svr.size();
        while (i-- > 0 && s != cli.size()) ThreadUtil.sleep(CoreRunner.CO_SLEEP);

        assertEquals(s, cli.size());
        c.stop();

        for (i = 0; i < s; ++i) {
            checkEq(svr.poll(), cli.poll());
        }
    }

    private void checkEq(PacketData a, PacketData b) {
        assertEquals(a.getCode(), b.getCode());
        assertEquals(a.getLength(), b.getLength());
        assertArrayEquals(a.getData(), b.getData());
    }

}
