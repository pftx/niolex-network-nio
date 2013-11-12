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
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.niolex.network.CoreRunner;
import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.example.EchoPacketHandler;
import org.apache.niolex.network.server.NioServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
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
    private static final int PORT = 8908;

    @Mock
    private IPacketHandler packetHandler;

    private IPacketHandler packetHandlerServer;

    private NioServer nioServer;
    private Set<String> received = new HashSet<String>();



    @Before
    public void createPacketClient() throws Exception {
        nioServer = new NioServer();
        packetHandlerServer = spy(new EchoPacketHandler());
        nioServer.setPacketHandler(packetHandlerServer);
        nioServer.setPort(PORT);
        nioServer.start();
    }

    @After
    public void stopNioServer() throws Exception {
        nioServer.stop();
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

    /**
     * Test method for
     * {@link org.apache.niolex.network.client.PacketClient#handleWrite(org.apache.niolex.network.PacketData)}
     * .
     */
    @Test
    public void testHandleWrite() throws Exception {
        final PacketData sc0 = new PacketData();
        final PacketData sc1 = new PacketData();
        final PacketData sc2 = new PacketData();
        final PacketData sc3 = new PacketData();
        final PacketData sc4 = new PacketData();
        final PacketData sc5 = new PacketData();

        sc5.setReserved((byte)5);
        assertEquals(sc5.getReserved(), 5);

        doAnswer(new Answer<String>() {
            public String answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                PacketData sc = (PacketData) args[0];
                switch (sc.getCode()) {
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
                    System.out.println("!!!Code Not Expected: " + sc.getCode());
                    break;
                }
                IPacketWriter ip = (IPacketWriter)args[1];
                received.add(ip.getRemoteName() + ", code: " + sc.getCode());
                String s = "called with arguments: " + args.length + ", code: " + sc.getCode()
                        + ", client: " + ip.getRemoteName();
                LOG.info(s);
                return s;
            }
        }).when(packetHandler).handlePacket(any(PacketData.class),
                any(IPacketWriter.class));

        PacketClient packetClient1 = new PacketClient(new InetSocketAddress("localhost", PORT));
        packetClient1.setPacketHandler(packetHandler);
        PacketClient packetClient2 = new PacketClient(new InetSocketAddress("localhost", PORT));
        packetClient2.setPacketHandler(packetHandler);
        PacketClient packetClient3 = new PacketClient(new InetSocketAddress("localhost", PORT));
        packetClient3.setPacketHandler(packetHandler);
        PacketClient packetClient4 = new PacketClient(new InetSocketAddress("localhost", PORT));
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
            sc.setCode((short) (i + 1));
            sc.setVersion((byte) 8);
            int len = (r.nextInt(1024) + 1) * 1024;
            sc.setLength(len);
            sc.setData(generateRandom(len, r));
            packetClient1.handleWrite(sc);
            packetClient2.handleWrite(sc);
            packetClient3.handleWrite(sc);
            packetClient4.handleWrite(sc);
        }
        int i = 30;
        while (i-- > 0) {
            if (received.size() == 24)
                break;
            Thread.sleep(10 * CoreRunner.CO_SLEEP);
        }
        packetClient1.stop();
        packetClient2.stop();
        packetClient3.stop();
        packetClient4.stop();
        assertEquals(24, received.size());
    }
}
