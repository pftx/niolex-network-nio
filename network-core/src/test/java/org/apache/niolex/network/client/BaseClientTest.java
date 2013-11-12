/**
 * BaseClientTest.java
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
package org.apache.niolex.network.client;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.PacketData;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5, $Date: 2012-12-4$
 */
public class BaseClientTest extends BaseClient {

    /**
     * This is the override of super method.
     * @see org.apache.niolex.network.IClient#connect()
     */
    @Override
    public void connect() {
        socket = mock(Socket.class);
        when(socket.getLocalPort()).thenReturn(3527);
        try {
            doThrow(new IOException("Moke#Mock#Make")).when(socket).close();
        } catch (IOException e) {
        }
    }

    /**
     * This is the override of super method.
     * @see org.apache.niolex.network.IClient#stop()
     */
    @Override
    public void stop() {
        socket = null;
    }

    /**
     * This is the override of super method.
     * @see org.apache.niolex.network.IPacketWriter#handleWrite(org.apache.niolex.network.PacketData)
     */
    @Override
    public void handleWrite(PacketData sc) {
    }

    //------------------------------------------------------------------------------------------------
    // START TEST
    //------------------------------------------------------------------------------------------------

    @Test
    public void testGetRemoteName() throws Exception {
        setServerAddress("localhost:8823");
        stop();
        assertEquals("localhost/127.0.0.1:8823-0000", getRemoteName());
        connect();
        assertEquals("localhost/127.0.0.1:8823-3527", getRemoteName());
        stop();
    }

    /**
     * Test method for {@link org.apache.niolex.network.client.BaseClient#safeClose()}.
     * @throws IOException
     */
    @Test
    public void testSafeClose() throws IOException {
        stop();
        Exception e = safeClose();
        assertNull(e);
        connect();
        e = safeClose();
        assertEquals(e.getMessage(), "Moke#Mock#Make");
        stop();
    }

    /**
     * Test method for {@link org.apache.niolex.network.client.BaseClient#safeClose()}.
     * @throws IOException
     */
    @Test
    public void testSafeCloseOther() throws IOException {
        BaseClient cli = new BaseClient(){
            @Override
            public void connect() throws IOException {
                this.socket = mock(Socket.class);
            }

            @Override
            public void stop() {
            }
            @Override
            public void handleWrite(PacketData sc) {

            }};
        cli.setServerAddress("localhost:8123");
        cli.connect();
        Exception e = cli.safeClose();
        assertNull(e);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testAddEventListener() throws Exception {
        addEventListener(null);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testAttachData() throws Exception {
        attachData("abc", null);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testGetAttached() throws Exception {
        getAttached(null);
    }

    @Test
    public void testSetPacketHandler() throws Exception {
        IPacketHandler packetHandler = mock(IPacketHandler.class);
        setPacketHandler(packetHandler);
        assertEquals(packetHandler, this.packetHandler);
    }

    @Test
    public void testGetServerAddress() throws Exception {
        setServerAddress("www.baidu.com:8123");
        System.out.println(getServerAddress());
    }

    @Test
    public void testSetServerAddressInetSocketAddress() throws Exception {
        setServerAddress(new InetSocketAddress("www.baidu.com", 8080));
        System.out.println(getServerAddress());
    }

    @Test
    public void testSetServerAddressString() throws Exception {
        setServerAddress("localhost:9001");
        assertEquals("localhost/127.0.0.1:9001", getServerAddress().toString());
    }

    @Test
    public void testGetConnectTimeout() throws Exception {
        setConnectTimeout(4352);
        assertEquals(4352, getConnectTimeout());
    }

    @Test
    public void testSetConnectTimeout() throws Exception {
        setConnectTimeout(2344537);
        assertEquals(2344537, getConnectTimeout());
    }

    @Test
    public void testIsWorking() throws Exception {
        assertFalse(isWorking());
    }

}
