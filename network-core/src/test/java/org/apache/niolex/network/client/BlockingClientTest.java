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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;

import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.client.BlockingClient.ReadLoop;
import org.junit.Test;

/**
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5
 * @since 2012-12-18
 */
public class BlockingClientTest {

    /**
     * Test method for {@link org.apache.niolex.network.client.BlockingClient#BlockingClient()}.
     * @throws IOException
     */
    @Test(expected=IllegalStateException.class)
    public void testBlockingClient() throws IOException {
        BlockingClient pc = new BlockingClient();
        OutputStream o = mock(OutputStream.class);
        doThrow(new IOException("Abc")).when(o).write(anyInt());
        pc.out = new DataOutputStream(o);
        pc.handleWrite(PacketData.getHeartBeatPacket());
    }

    /**
     * Test method for {@link org.apache.niolex.network.client.BlockingClient#BlockingClient(java.net.InetSocketAddress)}.
     */
    @Test
    public void testBlockingClientInetSocketAddress() {
        BlockingClient pc = new BlockingClient();
        byte[] abc = new byte[8];
        InputStream in = new ByteArrayInputStream(abc);
        ReadLoop r = pc.new ReadLoop(in);
        pc.isWorking = true;
        IPacketHandler h = mock(IPacketHandler.class);
        pc.setPacketHandler(h);
        r.run();
        assertFalse(pc.isWorking);
        verify(h).handleClose(pc);
    }

    /**
     * Test method for {@link org.apache.niolex.network.client.BlockingClient#connect()}.
     * @throws IOException
     */
    @Test
    public void testConnect() throws IOException {
        final BlockingClient pc = new BlockingClient();
        InputStream in = mock(InputStream.class);
        doThrow(new SocketTimeoutException("Abc")).when(in).read(any(byte[].class), anyInt(), anyInt());
        ReadLoop r = pc.new ReadLoop(in);
        pc.isWorking = true;
        IPacketHandler h = mock(IPacketHandler.class);
        pc.setPacketHandler(h);

        OutputStream o = new FilterOutputStream(null) {

            @Override
            public void write(int b) throws IOException {
                pc.isWorking = false;
            }

            @Override
            public void write(byte[] b) throws IOException {
                pc.isWorking = false;
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                pc.isWorking = false;
            }

        };
        pc.out = new DataOutputStream(o);

        r.run();
        assertFalse(pc.isWorking);
    }

}
