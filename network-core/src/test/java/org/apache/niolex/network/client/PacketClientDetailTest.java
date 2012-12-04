/**
 * PacketClientDetailTest.java
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;

import org.apache.niolex.commons.reflect.FieldUtil;
import org.apache.niolex.commons.test.Counter;
import org.apache.niolex.commons.util.Runner;
import org.apache.niolex.commons.util.SystemUtil;
import org.apache.niolex.network.CoreRunner;
import org.apache.niolex.network.client.PacketClient.WriteLoop;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5, $Date: 2012-12-4$
 */
public class PacketClientDetailTest {

    @BeforeClass
    public static void run() throws Exception {
        CoreRunner.createServer();
    }

    @AfterClass
    public static void down() throws Exception {
        CoreRunner.shutdown();
    }

    private PacketClient packetClient;

    @Before
    public void createPacketClient() throws Exception {
        packetClient = new PacketClient(new InetSocketAddress("localhost", CoreRunner.PORT));
    }

    @After
    public void stopPacketClient() throws Exception {
        packetClient.stop();
    }

    @Test
    public void testStop() throws Exception {
        final Counter cnt = new Counter();
        Thread th = new Thread() {
            public void run() {
                while (cnt.cnt() == 0) {
                    SystemUtil.sleep(100);
                }
            }
        };
        th.start();
        Field f = FieldUtil.getField(PacketClient.class, "writeThread");
        FieldUtil.setFieldValue(f, packetClient, th);
        Thread stop = Runner.run(packetClient, "stop");
        Thread.sleep(CoreRunner.CO_SLEEP);
        stop.interrupt();
        cnt.inc();
        stop.join();
    }

    @Test
    public void testWrite() throws Exception {
        OutputStream out = mock(OutputStream.class);
        doThrow(new IOException("This is test testSafeClose.")).when(out)
            .write(any(byte[].class), anyInt(), anyInt());
        packetClient.setConnectTimeout(2);
        packetClient.isWorking = true;
        WriteLoop loop = packetClient.new WriteLoop(out);
        loop.run();
    }

    /**
     * Test method for
     * {@link org.apache.niolex.network.client.PacketClient#getRemoteName()}
     * .
     */
    @Test
    public void testGetRemoteName() throws Exception {
        assertEquals("localhost/127.0.0.1:8809-0000", packetClient.getRemoteName());
        try {
            packetClient.attachData("adsfasdf", "adsfasdf");
            assertTrue("Should not attache.", false);
        } catch (Exception e) {
            ;
        }
        try {
            packetClient.getAttached("adsfasdf");
            assertTrue("Should not attache.", false);
        } catch (Exception e) {
            ;
        }
    }
}
