/**
 * BlockingClientTest.java
 *
 * Copyright 2012 The original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.niolex.network.client;

import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.util.concurrent.CountDownLatch;

import org.apache.niolex.network.CoreRunner;
import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5
 * @since 2012-12-18
 */
public class BlockingClientTest extends BlockingClient {

    @BeforeClass
    public static void setup() throws Exception {
        CoreRunner.createServer();
    }

    @AfterClass
    public static void stop2() throws Exception {
        CoreRunner.shutdown();
    }

    /**
     * Test method for {@link org.apache.niolex.network.client.BlockingClient#BlockingClient()}.
     * @throws IOException
     */
    @Test(expected=IllegalStateException.class)
    public void testBlockingClient() throws IOException {
        BlockingClient pc = new BlockingClient();
        OutputStream o = mock(OutputStream.class);
        doThrow(new IOException("Abc")).when(o).write(any(byte[].class));
        pc.out = o;
        pc.handleWrite(PacketData.getHeartBeatPacket());
    }

    /**
     * Test method for {@link org.apache.niolex.network.client.BlockingClient#BlockingClient(java.net.InetSocketAddress)}.
     */
    @Test
    public void testBlockingClientInetSocketAddress() {
        BlockingClient pc = new BlockingClient(CoreRunner.SERVER_ADDR);
        byte[] abc = new byte[10];
        pc.in = new ByteArrayInputStream(abc);
        ReadLoop r = pc.rLoop;
        pc.isWorking = true;
        IPacketHandler h = mock(IPacketHandler.class);
        pc.setPacketHandler(h);
        r.run();
        assertFalse(pc.isWorking);
        verify(h).handleClose(pc);
    }

    /**
     * Test method for {@link org.apache.niolex.network.client.BlockingClient#connect()}.
     * @throws Exception
     */
    @Test
    public void testConnect() throws Exception {
        BlockingClient pc = new BlockingClient(CoreRunner.SERVER_ADDR);
        final CountDownLatch latch = new CountDownLatch(1);
        IPacketHandler h = new IPacketHandler(){

            @Override
            public void handlePacket(PacketData sc, IPacketWriter wt) {
                if (sc.getCode() == 2) {
                    latch.countDown();
                }
            }

            @Override
            public void handleClose(IPacketWriter wt) {
            }};;
        pc.setPacketHandler(h);
        pc.connect();
        pc.handleWrite(new PacketData(3, "Give me a hand.".getBytes()));
        pc.handleWrite(new PacketData(2, "Hellow, world.".getBytes()));
        latch.await();
        pc.stop();
    }

    @Test
    public void testReadLoop() throws IOException {
        final BlockingClient pc = new BlockingClient();
        InputStream in = mock(InputStream.class);
        doThrow(new SocketTimeoutException("Abc")).when(in).read();
        doThrow(new SocketTimeoutException("Abc")).when(in).read(any(byte[].class), anyInt(), anyInt());
        pc.in = in;
        ReadLoop r = pc.rLoop;
        pc.isWorking = true;

        OutputStream o = new ByteArrayOutputStream() {

            @Override
            public void write(int b) {
                pc.isWorking = false;
            }

            @Override
            public void write(byte[] b) {
                pc.isWorking = false;
            }

            @Override
            public void write(byte[] b, int off, int len) {
                pc.isWorking = false;
            }

        };
        pc.out = new DataOutputStream(o);
        IPacketHandler h = mock(IPacketHandler.class);
        pc.setPacketHandler(h);

        r.run();
        assertFalse(pc.isWorking);
        verify(h, never()).handlePacket(any(PacketData.class), eq(pc));
        verify(h, never()).handleClose(pc);
    }

    @Test(expected=NullPointerException.class)
    public void testHandleWrite() throws Exception {
        in = mock(InputStream.class);
        ReadLoop r = rLoop;
        r.run();
        handleWrite(PacketData.getHeartBeatPacket());
    }

}
