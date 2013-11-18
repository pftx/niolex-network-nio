/**
 * CompositeTest.java
 *
 * Copyright 2013 the original author or authors.
 *
 * We licenses this file to you under the Apache License, version 2.0
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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.niolex.commons.test.MockUtil;
import org.apache.niolex.network.CoreRunner;
import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.example.SavePacketHandler;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2013-11-12
 */
@RunWith(MockitoJUnitRunner.class)
public class CompositeTest {
    private static final Logger LOG = LoggerFactory.getLogger(BaseClientTest.class);

    @BeforeClass
    public static void start() throws Exception {
        CoreRunner.createServer();
    }

    @AfterClass
    public static void stop() throws Exception {
        CoreRunner.shutdown();
    }

    @Test
    public void testBlockingClient() throws Exception {
        List<PacketData> clientSavePkList = new ArrayList<PacketData>();
        List<PacketData> clientSendList = new ArrayList<PacketData>();

        BlockingClient c = new BlockingClient(CoreRunner.SERVER_ADDR);
        c.setPacketHandler(new SavePacketHandler(clientSavePkList));
        c.connect();

        for (int i = 0; i < 5000; ++i) {
            PacketData sc = new PacketData();
            sc.setCode((short) 2);
            sc.setVersion((byte) 1);
            byte[] data = MockUtil.randByteArray(60);
            sc.setLength(data.length);
            sc.setData(data);
            c.handleWrite(sc);
            clientSendList.add(sc);
        }
        int k = 100;
        while (k-- > 0) {
            if (clientSavePkList.size() == 5000)
                break;
            Thread.sleep(CoreRunner.CO_SLEEP);
        }
        c.stop();
        for (int i = 0; i < 5000; ++i) {
            assertArrayEquals(clientSavePkList.get(i).getData(), clientSendList.get(i).getData());
        }
    }

    private byte[] generateRandom(int len, Random r) {
        byte[] ret = new byte[len];
        r.nextBytes(ret);
        return ret;
    }

    private void assertArrayEquals(byte[] a, byte[] b) {
        if (a.length == b.length) {
            for (int k = 0; k < a.length; k += a.length / 100 + 1) {
                if (a[k] != b[k]) {
                    assertFalse("Index at " + k, true);
                    return;
                }
            }
        } else {
            assertFalse("Invalid length", true);
        }
    }

    @Test
    public void testPacketClient() throws Exception {
        final PacketData sc0 = new PacketData();
        final PacketData sc1 = new PacketData();
        final PacketData sc2 = new PacketData();
        final PacketData sc3 = new PacketData();
        final PacketData sc4 = new PacketData();
        final PacketData sc5 = new PacketData();

        final IPacketHandler packetHandler = mock(IPacketHandler.class);
        final Set<String> received = Collections.synchronizedSet(new HashSet<String>());

        doAnswer(new Answer<String>() {
            public String answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                PacketData sc = (PacketData) args[0];
                switch (sc.getReserved()) {
                case 1:
                    assertArrayEquals(sc0.getData(), sc.getData());
                    break;
                case 2:
                    assertArrayEquals(sc1.getData(), sc.getData());
                    break;
                case 3:
                    assertArrayEquals(sc2.getData(), sc.getData());
                    break;
                case 4:
                    assertArrayEquals(sc3.getData(), sc.getData());
                    break;
                case 5:
                    assertArrayEquals(sc4.getData(), sc.getData());
                    break;
                case 6:
                    assertArrayEquals(sc5.getData(), sc.getData());
                    break;
                default:
                    assertFalse("!!!Code Not Expected: " + sc.getCode(), true);
                    break;
                }
                IPacketWriter ip = (IPacketWriter)args[1];
                received.add(ip.getRemoteName() + ", res: " + sc.getReserved());
                String s = "called with arguments: " + args.length + ", res: " + sc.getReserved()
                        + ", client: " + ip.getRemoteName();
                LOG.info(s);
                return s;
            }
        }).when(packetHandler).handlePacket(any(PacketData.class),
                any(IPacketWriter.class));

        PacketClient packetClient1 = new PacketClient(CoreRunner.SERVER_ADDR);
        packetClient1.setPacketHandler(packetHandler);
        PacketClient packetClient2 = new PacketClient(CoreRunner.SERVER_ADDR);
        packetClient2.setPacketHandler(packetHandler);
        PacketClient packetClient3 = new PacketClient(CoreRunner.SERVER_ADDR);
        packetClient3.setPacketHandler(packetHandler);
        PacketClient packetClient4 = new PacketClient(CoreRunner.SERVER_ADDR);
        packetClient4.setPacketHandler(packetHandler);

        packetClient1.connect();
        packetClient2.connect();
        packetClient3.connect();
        packetClient4.connect();

        List<PacketData> list = new ArrayList<PacketData>();
        list.add(sc0);
        list.add(sc1);
        list.add(sc2);
        list.add(sc3);
        list.add(sc4);
        list.add(sc5);
        Random r = new Random(System.nanoTime());

        for (int i = 0; i < 6; ++i) {
            PacketData sc = list.get(i);
            sc.setCode((short) 2);
            sc.setVersion((byte) 8);
            sc.setReserved((byte) (i + 1));
            int len = (r.nextInt(1024) + 1) * 1024;
            sc.setLength(len);
            sc.setData(generateRandom(len, r));
            packetClient1.handleWrite(sc);
            packetClient2.handleWrite(sc);
            packetClient3.handleWrite(sc);
            packetClient4.handleWrite(sc);
        }
        int i = 100;
        while (i-- > 0) {
            if (received.size() == 24)
                break;
            Thread.sleep(CoreRunner.CO_SLEEP);
        }
        packetClient1.stop();
        packetClient2.stop();
        packetClient3.stop();
        packetClient4.stop();
        assertEquals(24, received.size());
    }

    private int received = 0;

    @Test
    public void testHandleLarge() throws Exception {
        PacketClient packetClient = new PacketClient(CoreRunner.SERVER_ADDR);
        IPacketHandler packetHandler = mock(IPacketHandler.class);
        received = 0;

        final PacketData sc0 = new PacketData();
        final PacketData sc1 = new PacketData();
        final PacketData sc2 = new PacketData();
        final PacketData sc3 = new PacketData();
        final PacketData sc4 = new PacketData();
        final PacketData sc5 = new PacketData();

        doAnswer(new Answer<String>() {
            public String answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                PacketData sc = (PacketData) args[0];
                switch (sc.getReserved()) {
                case 6:
                    assertArrayEquals(sc0.getData(), sc.getData());
                    break;
                case 1:
                    assertArrayEquals(sc1.getData(), sc.getData());
                    break;
                case 2:
                    assertArrayEquals(sc2.getData(), sc.getData());
                    break;
                case 3:
                    assertArrayEquals(sc3.getData(), sc.getData());
                    break;
                case 4:
                    assertArrayEquals(sc4.getData(), sc.getData());
                    break;
                case 5:
                    assertArrayEquals(sc5.getData(), sc.getData());
                    break;
                default:
                    assertFalse("Invalid Code: " + sc.getReserved(), true);
                    break;
                }
                IPacketWriter ip = (IPacketWriter)args[1];
                String s = "called with arguments: " + args.length + ", code: " + sc.getReserved()
                        + ", client: " + ip.getRemoteName();
                LOG.info(s);
                ++received;
                return s;
            }
        }).when(packetHandler).handlePacket(any(PacketData.class),
                any(IPacketWriter.class));

        packetClient.setPacketHandler(packetHandler);
        packetClient.connect();

        List<PacketData> list = new ArrayList<PacketData>();
        list.add(sc0);
        list.add(sc1);
        list.add(sc2);
        list.add(sc3);
        list.add(sc4);
        list.add(sc5);
        Random r = new Random(System.nanoTime());
        for (int i = 0; i < 6; ++i) {
            PacketData sc = list.get(i);
            if (i == 0) {
                sc.setReserved((byte) 6);
            } else {
                sc.setReserved((byte) i);
            }
            sc.setCode((short) 2);
            sc.setVersion((byte) 8);
            int len = (r.nextInt(99) + 1) * 102400;
            sc.setLength(len);
            sc.setData(generateRandom(len, r));
            packetClient.handleWrite(sc);
        }
        int i = 300;
        while (i-- > 0) {
            if (received == 6)
                break;
            Thread.sleep(CoreRunner.CO_SLEEP);
        }
        packetClient.stop();
        assertEquals(6, received);
    }

    /**
     * Run too many times as main method.
     *
     * @param args
     * @throws Exception
     */
    public static void main(String args[]) throws Exception {
        start();
        CompositeTest hdt = new CompositeTest();
        for (int i = 0; i < 1000; ++i) {
            System.out.println("Test iter .. " + i);
            hdt.testBlockingClient();
        }
        stop();
    }
}
