/**
 * FastCoreTest.java
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
package org.apache.niolex.network.server;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelector;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.niolex.commons.reflect.FieldUtil;
import org.apache.niolex.commons.reflect.MethodUtil;
import org.apache.niolex.commons.util.Const;
import org.apache.niolex.network.CoreRunner;
import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.server.FastCore.Status;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-11-7
 */
@RunWith(MockitoJUnitRunner.class)
public class FastCoreTest {

	private SocketChannel client;

	@Mock
	private IPacketHandler packetHandler;

	private Selector selector;

	@Mock
	private SelectorHolder selectorH;

	private FastCore fastCore;

	@BeforeClass
	public static void start() throws Exception {
		CoreRunner.createServer();
	}

	@AfterClass
	public static void stop() throws Exception {
		CoreRunner.shutdown();
	}

	@Before
	public void init() throws IOException {
		client = SocketChannel.open(new InetSocketAddress("localhost", CoreRunner.PORT));
		client.configureBlocking(false);
		selector = Selector.open();
		when(selectorH.getSelector()).thenReturn(selector);
		fastCore = spy(new FastCore(packetHandler, selectorH, client));
	}

	@After
	public void destroy() throws IOException {
		client.close();
	}

	@Test
    public void testStatus() {
	    assertEquals("HEADER", Status.HEADER.toString());
	    assertEquals(Status.BODY, Status.valueOf("BODY"));
	}

	/**
	 * Test method for {@link org.apache.niolex.network.server.FastCore#handleWrite(org.apache.niolex.network.PacketData)}.
	 */
	@Test
	public void testHandleWritePacketData() {
		PacketData sc = new PacketData(5);
		fastCore.handleWrite(sc);
		fastCore.handleWrite(sc);
		verify(selectorH).changeInterestOps(any(SelectionKey.class));
		fastCore.handleWrite(sc);
		fastCore.handleWrite(sc);
		fastCore.handleWrite(sc);
		verify(selectorH).changeInterestOps(any(SelectionKey.class));
		FieldUtil.setValue(fastCore, "writeAttached", new AtomicBoolean(false));
		fastCore.handleWrite(sc);
        verify(selectorH, times(2)).changeInterestOps(any(SelectionKey.class));
	}

	/**
	 * Test method for {@link org.apache.niolex.network.server.FastCore#getRemoteName()}.
	 */
	@Test
	public void testGetRemoteName() {
		System.out.println(fastCore.getRemoteName());
	}
	

    /**
     * Test method for {@link org.apache.niolex.network.server.FastCore#handleRead()}.
     * @throws IOException
     */
    @Test
    public void testHandleReadFake() throws IOException {
        // 1. Direct read, got nothing.
        fastCore.handleRead();
        verify(packetHandler, times(0)).handlePacket(any(PacketData.class), any(IPacketWriter.class));
        
        // 2. Read zero packet.
        SocketChannel ch = mock(SocketChannel.class);
        when(ch.read(any(ByteBuffer.class))).thenReturn(0);
        FieldUtil.setValue(fastCore, "socketChannel", ch);
        assertFalse(fastCore.handleRead());
        
        // 3. Read part of header.
        when(ch.read(any(ByteBuffer.class))).thenAnswer(new Answer<Integer>(){

            @Override
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                ByteBuffer bf = (ByteBuffer)invocation.getArguments()[0];
                bf.put((byte)1);
                bf.put((byte)2);
                bf.putShort((short)3);
                bf.putInt(8);
                return 8;
            }});
        
        assertTrue(fastCore.handleRead());
        verify(packetHandler, times(1)).handlePacket(any(PacketData.class), any(IPacketWriter.class));
    }

	/**
	 * Test method for {@link org.apache.niolex.network.server.FastCore#handleRead()}.
	 * @throws IOException
	 */
	@Test
	public void testHandleReadClosed() throws IOException {
		client.close();
		fastCore.handleRead();
		fastCore.handleRead();
		verify(packetHandler, times(2)).handleClose(fastCore);
	}

    @Test
    public void testPacketFinished() throws Exception {
        Field f = FieldUtil.getField(FastCore.class, "receivePacket");
        FieldUtil.setFieldValue(fastCore, f, new PacketData(7, new byte[65]));
        fastCore.packetFinished();
        verify(packetHandler, times(1)).handlePacket(any(PacketData.class), any(IPacketWriter.class));
    }

    @Test
    public void testPacketFinishedOther() throws Exception {
        Field f = FieldUtil.getField(FastCore.class, "receivePacket");
        FieldUtil.setFieldValue(fastCore, f, PacketData.getHeartBeatPacket());
        fastCore.packetFinished();
        verify(packetHandler, times(0)).handlePacket(any(PacketData.class), any(IPacketWriter.class));
    }

    /**
     * Test method for {@link org.apache.niolex.network.server.FastCore#handleWrite()}.
     * @throws IOException
     */
    @Test
    public void testHandleWriteEx() throws IOException {
        client.close();
        fastCore.handleWrite();
        verify(packetHandler, times(1)).handleClose(fastCore);
    }

    private FastCore createFastCore(IPacketHandler hl) throws IOException {
        SocketChannel ch = mock(SocketChannel.class);
        Socket so = mock(Socket.class);
        when(so.getRemoteSocketAddress()).thenReturn(new InetSocketAddress("localhost", 8888));
        when(ch.socket()).thenReturn(so);
        when(ch.isConnected()).thenReturn(true);
        SelectionKey value = mock(SelectionKey.class);
        FieldUtil.setValue(ch, "open", true);
        FieldUtil.setValue(ch, "regLock", true);
        FieldUtil.setValue(ch, "keyLock", true);
        SelectorHolder sh = new SelectorHolder(Thread.currentThread(), mock(AbstractSelector.class));
        FastCore f = new FastCore(hl, sh, ch);
        FieldUtil.setValue(f, "selectionKey", value);
        when(ch.write(any(ByteBuffer.class))).thenAnswer(new Answer<Integer>(){

            @Override
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                byte[] dst = new byte[7];
                ByteBuffer bf = (ByteBuffer)invocation.getArguments()[0];
                
                if (bf.remaining() > 7) {
                    bf.get(dst, 0, 7);
                } else {
                    bf.get(dst, 0, bf.remaining());
                }
                return 2;
            }});
        return f;
    }
    
    /**
     * Test method for {@link org.apache.niolex.network.server.FastCore#handleWrite()}.
     * @throws IOException
     */
    @Test
    public void testHandleWriteWholePackage() throws IOException {
        IPacketHandler hl = mock(IPacketHandler.class);
        FastCore fc = createFastCore(hl);
        fc.handleWrite(new PacketData(33, "Hello, write packet."));
        // sendNewPacket -- whole packet.
        assertFalse(fc.handleWrite());
        assertFalse(fc.handleWrite());
        assertFalse(fc.handleWrite());
        assertTrue(fc.handleWrite());
        assertFalse(fc.handleWrite());
        assertFalse(fc.handleWrite());
    }
    
    /**
     * Test method for {@link org.apache.niolex.network.server.FastCore#handleWrite()}.
     * @throws IOException
     */
    @Test
    public void testHandleWriteLarge() throws IOException {
        IPacketHandler hl = mock(IPacketHandler.class);
        FastCore fc = createFastCore(hl);
        fc.handleWrite(new PacketData(33, new byte[8188]));
        // sendNewPacket -- header part.
        assertFalse(fc.handleWrite());
        assertTrue(fc.handleWrite());

        // send body.
        for (int i = 0; i < 1169; ++i) {
            assertFalse(fc.handleWrite());
        }
        assertTrue(fc.handleWrite());
        assertFalse(fc.handleWrite());
        assertFalse(fc.handleWrite());
    }

    /**
     * Test method for {@link org.apache.niolex.network.server.FastCore#handleWrite()}.
     * @throws IOException
     */
    @Test
    public void testHandleWriteBody() throws IOException {
        IPacketHandler hl = mock(IPacketHandler.class);
        FastCore fc = createFastCore(hl);
        FieldUtil.setValue(fc, "sendStatus", Status.BODY);
        ByteBuffer bb = ByteBuffer.allocate(8);
        bb.limit(8);
        FieldUtil.setValue(fc, "sendBuffer", bb);
        assertFalse(fc.handleWrite());
        assertTrue(fc.handleWrite());
    }

    /**
     * Test method for {@link org.apache.niolex.network.server.FastCore#handleWrite()}.
     * @throws IOException
     */
    @Test
    public void testHandleWriteHeader() throws IOException {
        IPacketHandler hl = mock(IPacketHandler.class);
        FastCore fc = createFastCore(hl);
        ByteBuffer bb = ByteBuffer.allocate(8);
        bb.limit(0);
        FieldUtil.setValue(fc, "sendBuffer", bb);
        FieldUtil.setValue(fc, "selectionKey", mock(SelectionKey.class));
        FieldUtil.setValue(fc, "sendPacket", new PacketData(6, new byte[8]));
        FieldUtil.setValue(fc, "sendStatus", Status.HEADER);

        assertFalse(fc.handleWrite());
        assertTrue(fc.handleWrite());
    }

    /**
     * Test method for {@link org.apache.niolex.network.server.FastCore#handleWrite()}.
     * @throws IOException
     */
    @Test
    public void testHandleWriteHeaderCase2() throws IOException {
        IPacketHandler hl = mock(IPacketHandler.class);
        FastCore fc = createFastCore(hl);
        ByteBuffer bb = ByteBuffer.allocate(8);
        bb.limit(0);
        FieldUtil.setValue(fc, "sendBuffer", bb);
        FieldUtil.setValue(fc, "selectionKey", mock(SelectionKey.class));
        FieldUtil.setValue(fc, "sendPacket", new PacketData(6, new byte[6]));
        FieldUtil.setValue(fc, "sendStatus", Status.HEADER);

        assertTrue(fc.handleWrite());
    }

    /**
     * Test method for {@link org.apache.niolex.network.server.FastCore#doSendNewPacket()}.
     * @throws Exception
     */
    @Test
    public void testDoSendNewPacket() throws Exception {
        IPacketHandler hl = mock(IPacketHandler.class);
        FastCore fc = createFastCore(hl);
        ByteBuffer bb = ByteBuffer.allocate(8);
        bb.limit(0);
        FieldUtil.setValue(fc, "sendBuffer", bb);
        FieldUtil.setValue(fc, "selectionKey", mock(SelectionKey.class));
        FieldUtil.setValue(fc, "sendPacket", new PacketData(6, new byte[6]));

        Method m = MethodUtil.getMethod(FastCore.class, "doSendNewPacket");
        Boolean b = (Boolean) MethodUtil.invokeMethod(fc, m);
        assertFalse(b);
    }

    /**
     * Test method for {@link org.apache.niolex.network.server.FastCore#handleWrite()}.
     * @throws IOException
     */
    @Test
    public void testHandleWriteLargeBuffer() throws IOException {
        PacketData pk = new PacketData(0, new byte[65 * Const.K]);
        fastCore.handleWrite(PacketData.getHeartBeatPacket());
        fastCore.handleWrite(pk);
        while(fastCore.handleWrite());
        while(fastCore.handleWrite());
        while(fastCore.handleWrite());
    }

    @Test
    public void testSendNewPacket() throws Exception {
        fastCore = new FastCore(packetHandler, selectorH, client){

            /**
             * This is the override of super method.
             * @see org.apache.niolex.network.server.BasePacketWriter#isEmpty()
             */
            @Override
            public boolean isEmpty() {
                return false;
            }};
        assertTrue(fastCore.handleWrite());
        assertTrue(fastCore.handleWrite());
        assertTrue(fastCore.handleWrite());
        assertTrue(fastCore.handleWrite());
        assertTrue(fastCore.handleWrite());
        assertTrue(fastCore.handleWrite());
    }

    @Test
    public void testHandleClose() throws Exception {
        doThrow(new IllegalArgumentException("Test")).when(packetHandler).handleClose(any(IPacketWriter.class));
        FastCore fc = createFastCore(packetHandler);
        Method m = MethodUtil.getMethod(FastCore.class, "handleClose");
        MethodUtil.invokeMethod(fc, m);
        verify(packetHandler, times(1)).handleClose(fc);
    }

}
