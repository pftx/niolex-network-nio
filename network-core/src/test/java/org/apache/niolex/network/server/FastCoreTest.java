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
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelector;

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
import org.mockito.runners.MockitoJUnitRunner;

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
	public void testHandleRead() throws IOException {
		client.close();
		fastCore.handleRead();
		fastCore.handleRead();
		verify(packetHandler, times(2)).handleClose(fastCore);
	}

	/**
	 * Test method for {@link org.apache.niolex.network.server.FastCore#handleWrite()}.
	 * @throws IOException
	 */
	@Test
	public void testHandleWrite() throws IOException {
		client.close();
		fastCore.handleWrite();
		verify(packetHandler, times(1)).handleClose(fastCore);
	}

    @Test
    public void testPacketFinished() throws Exception {
        Field f = FieldUtil.getField(FastCore.class, "receivePacket");
        FieldUtil.setFieldValue(f, fastCore, PacketData.getHeartBeatPacket());
        fastCore.packetFinished();
        verify(packetHandler, times(0)).handlePacket(any(PacketData.class), any(IPacketWriter.class));
        System.out.println("not yet implemented");
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
        SocketChannel client = spy(new SocketChannel(null) {

            @Override
            public Socket socket() {
                return FastCoreTest.this.client.socket();
            }

            @Override
            public boolean isConnected() {
                return false;
            }

            @Override
            public boolean isConnectionPending() {
                return false;
            }

            @Override
            public boolean connect(SocketAddress remote) throws IOException {
                return false;
            }

            @Override
            public boolean finishConnect() throws IOException {
                return false;
            }

            @Override
            public int read(ByteBuffer dst) throws IOException {
                return 0;
            }

            @Override
            public long read(ByteBuffer[] dsts, int offset, int length) throws IOException {
                return 0;
            }

            @Override
            public int write(ByteBuffer src) throws IOException {
                return 0;
            }

            @Override
            public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
                return 0;
            }

            @Override
            protected void implCloseSelectableChannel() throws IOException {
                throw new IOException("Test");
            }

            @Override
            protected void implConfigureBlocking(boolean block) throws IOException {
            }});
        client.configureBlocking(false);
        selector = mock(AbstractSelector.class);
        when(selectorH.getSelector()).thenReturn(selector);
        fastCore = new FastCore(packetHandler, selectorH, client);
        Method m = MethodUtil.getMethod(FastCore.class, "handleClose");
        MethodUtil.invokeMethod(m, fastCore);
        verify(packetHandler, times(1)).handleClose(fastCore);
    }

}
