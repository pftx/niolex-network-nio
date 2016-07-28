/**
 * ClientManagerTest.java
 *
 * Copyright 2013 The original author or authors.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.niolex.network.ConnStatus;
import org.apache.niolex.network.CoreRunner;
import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.PacketData;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5
 * @since 2013-1-11
 */
public class ClientManagerTest {

    @BeforeClass
    public static void setup() throws Exception {
        CoreRunner.createServer();
    }

    @AfterClass
    public static void stop2() throws Exception {
        CoreRunner.shutdown();
    }

    /**
     * Test method for {@link org.apache.niolex.network.client.ClientManager#ClientManager(org.apache.niolex.network.IClient)}.
     * @throws InterruptedException
     */
    @Test
    public void testClientManager() throws InterruptedException {
        SocketClient sc = new SocketClient();
        IPacketHandler han = mock(IPacketHandler.class);
        ClientManager cm = new ClientManager(sc);
        cm.setPacketHandler(han);
        assertEquals(han, sc.packetHandler);
        assertEquals(sc, cm.client());
        cm.setAddressList("localhost:8809");
        boolean b = cm.connect();
        assertTrue(b);
        assertEquals(cm.getConnStatus(), ConnStatus.CONNECTED);
        assertTrue(cm.connect());
        assertTrue(cm.retryConnect());
        cm.close();
        assertEquals(0, cm.getConnectRetryTimes());
        b = cm.waitForConnected();
        assertFalse(b);
        cm.setConnectRetryTimes(2);
        b = cm.retryConnect();
        assertTrue(b);
        b = cm.waitForConnected();
        assertTrue(b);
    }

    /**
     * Test method for {@link org.apache.niolex.network.client.ClientManager#connect()}.
     */
    @Test(expected = IllegalStateException.class)
    public void testConnectNoAddress() {
        PacketClient sc = new PacketClient();
        ClientManager cm = new ClientManager(sc);
        cm.handleWrite(new PacketData(5));
        assertTrue(cm.connect());
    }

    /**
     * Test method for {@link org.apache.niolex.network.client.ClientManager#connect()}.
     */
    @Test
    public void testConnectIsWorking() {
        SocketClient sc = new SocketClient();
        sc.isWorking = true;
        ClientManager cm = new ClientManager(sc);
        cm.setAddressList(Arrays.asList(new InetSocketAddress("localhost", 8899)));
        assertTrue(cm.connect());
    }

    /**
     * Test method for {@link org.apache.niolex.network.client.ClientManager#connect()}.
     */
    @Test
    public void testConnectNoServer() {
        SocketClient sc = new SocketClient();
        sc.setConnectTimeout(500);
        ClientManager cm = new ClientManager(sc);
        cm.setAddressList(Arrays.asList(new InetSocketAddress("127.0.0.1", 8090)));
        boolean b = cm.connect();
        assertFalse(b);
        cm.close();
        assertEquals(cm.getConnStatus(), ConnStatus.CLOSED);
    }

    /**
     * Test method for {@link org.apache.niolex.network.client.ClientManager#retryConnect()}.
     */
    @Test(expected = IllegalStateException.class)
    public void testRetryConnectNoAddress() {
        SocketClient sc = new SocketClient();
        ClientManager cm = new ClientManager(sc);
        assertTrue(cm.retryConnect());
    }

    /**
     * Test method for {@link org.apache.niolex.network.client.ClientManager#retryConnect()}.
     */
    @Test
    public void testRetryConnect() {
        SocketClient sc = new SocketClient();
        sc.connectTimeout = 500;
        ClientManager cm = new ClientManager(sc);
        cm.setConnectRetryTimes(1);
        cm.setSleepBetweenRetryTime(1);
        cm.setAddressList(Arrays.asList(new InetSocketAddress("127.0.0.1", 8090)));
        boolean b = cm.retryConnect();
        assertFalse(b);
    }

    /**
     * Test method for {@link org.apache.niolex.network.client.ClientManager#getConnStatus()}.
     */
    @Test(expected = IllegalStateException.class)
    public void testGetConnStatus() {
        SocketClient sc = new SocketClient();
        ClientManager cm = new ClientManager(sc);
        List<InetSocketAddress> addressList = Collections.emptyList();
        cm.setAddressList(addressList);
        boolean b = cm.connect();
        assertTrue(b);
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.apache.niolex.network.client.ClientManager#setSleepBetweenRetryTime(int)}.
     */
    @Test(expected = IllegalStateException.class)
    public void testSetSleepBetweenRetryTime() {
        SocketClient sc = new SocketClient();
        ClientManager cm = new ClientManager(sc);
        boolean b = cm.retryConnect();
        assertTrue(b);
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.apache.niolex.network.client.ClientManager#waitForConnected()}.
     */
    @Test(expected = IllegalStateException.class)
    public void testWaitForConnected() {
        SocketClient sc = new SocketClient();
        ClientManager cm = new ClientManager(sc);
        List<InetSocketAddress> addressList = Collections.emptyList();
        cm.setAddressList(addressList);
        boolean b = cm.retryConnect();
        assertTrue(b);
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.apache.niolex.network.client.ClientManager#close()}.
     */
    @Test
    public void testClose() {
        SocketClient sc = new SocketClient();
        sc.setConnectTimeout(1000);
        ClientManager cm = new ClientManager(sc);
        cm.setAddressList(Arrays.asList(new InetSocketAddress("10.1.2.3", 8090)));
        cm.setConnectRetryTimes(0);
        cm.setSleepBetweenRetryTime(1);
        boolean b = cm.retryConnect();
        assertFalse(b);
        assertEquals(cm.getConnStatus(), ConnStatus.CLOSED);
    }

    /**
     * Test method for {@link org.apache.niolex.network.client.ClientManager#getSleepBetweenRetryTime()}.
     * @throws InterruptedException
     */
    @Test
    public void testGetSleepBetweenRetryTime() throws InterruptedException {
        SocketClient sc = new SocketClient();
        sc.setConnectTimeout(500);
        ClientManager cm = new ClientManager(sc);
        cm.setAddressList(Arrays.asList(new InetSocketAddress("localhost", 8809),
                new InetSocketAddress("127.0.0.1", 8100)));
        boolean b = cm.connect();
        if (b) {
            cm.close();
            cm.setConnectRetryTimes(10);
            b = cm.connect();
        }
        assertFalse(b);
        Thread.sleep(10);
        b = cm.waitForConnected();
        assertTrue(b);
    }

    /**
     * Test method for {@link org.apache.niolex.network.client.ClientManager#setConnectRetryTimes(int)}.
     */
    @Test
    public void testSetConnectRetryTimes() {
        SocketClient sc = new SocketClient();
        sc.setConnectTimeout(500);
        ClientManager cm = new ClientManager(sc);
        cm.setAddressList(Arrays.asList(new InetSocketAddress("localhost", 8809),
                new InetSocketAddress("localhost", 8100), new InetSocketAddress("localhost", 90)));
        boolean b = cm.retryConnect();
        assertTrue(b);
        cm.close();
    }

    /**
     * Test method for {@link org.apache.niolex.network.client.ClientManager#getConnectRetryTimes()}.
     */
    @Test
    public void testGetConnectRetryTimes() {
        SocketClient sc = new SocketClient();
        ClientManager cm = new ClientManager(sc);
        cm.setConnectRetryTimes(100);
        assertEquals(100, cm.getConnectRetryTimes());
    }

    /**
     * Test method for {@link org.apache.niolex.network.client.ClientManager#setAddressList(java.util.List)}.
     */
    @Test
    public void testSetAddressList() {
        SocketClient sc = new SocketClient();
        ClientManager cm = new ClientManager(sc);
        cm.setSleepBetweenRetryTime(1234);
        assertEquals(1234, cm.getSleepBetweenRetryTime());
    }

    /**
     * Test method for {@link org.apache.niolex.network.client.ClientManager#setAddressList(String)}.
     */
    @Test
    public void testSetAddressListStr() {
        SocketClient sc = new SocketClient();
        ClientManager cm = new ClientManager(sc);
        cm.setAddressList("localhost:8809, localhost:8080 localhost:9009");
        System.out.println(cm.getAddressList());
    }

    /**
     * Test method for {@link org.apache.niolex.network.client.ClientManager#getAddressList()}.
     */
    @Test
    public void testGetAddressList() {
        SocketClient sc = new SocketClient();
        ClientManager cm = new ClientManager(sc);
        cm.setAddressList(Arrays.asList(new InetSocketAddress("localhost", 8809),
                new InetSocketAddress("localhost", 1810), new InetSocketAddress("localhost", 2820)));
        System.out.println(cm.getAddressList());
    }

    /**
     * Test method for {@link org.apache.niolex.network.client.ClientManager#setConnectTimeout(int)}.
     */
    @Test
    public void testSetConnectTimeout() {
        SocketClient sc = new SocketClient();
        ClientManager cm = new ClientManager(sc);
        cm.setConnectTimeout(3100);
        assertEquals(3100, sc.getConnectTimeout());
    }

}
