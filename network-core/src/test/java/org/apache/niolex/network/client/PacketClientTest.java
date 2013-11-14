/**
 * PacketClientTest.java
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
package org.apache.niolex.network.client;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;

import org.apache.niolex.commons.bean.One;
import org.apache.niolex.commons.util.Runner;
import org.apache.niolex.network.Config;
import org.apache.niolex.network.CoreRunner;
import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.client.PacketClient.ReadLoop;
import org.apache.niolex.network.client.PacketClient.WriteLoop;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-5-28
 */
public class PacketClientTest {

	@BeforeClass
    public static void setup() throws Exception {
        CoreRunner.createServer();
    }

    @AfterClass
    public static void stop2() throws Exception {
        CoreRunner.shutdown();
    }

	/**
	 * Test method for
	 * {@link org.apache.niolex.network.client.PacketClient#connect()}.
	 */
	@Test
	public void testConnect() throws Exception {
	    PacketClient packetClient = new PacketClient();
		assertEquals(false, packetClient.isWorking());
		packetClient.setConnectTimeout(1234);
		assertEquals(1234, packetClient.getConnectTimeout());

		final CountDownLatch latch = new CountDownLatch(1);
		final One<PacketData> one = new One<PacketData>();
        IPacketHandler h = new IPacketHandler(){

            @Override
            public void handlePacket(PacketData sc, IPacketWriter wt) {
                if (sc.getCode() == 2) {
                    latch.countDown();
                    one.a = sc;
                }
            }

            @Override
            public void handleClose(IPacketWriter wt) {
            }};;

		packetClient.setPacketHandler(h);
		packetClient.setServerAddress(CoreRunner.SERVER_ADDR);
		packetClient.connect();

		packetClient.handleWrite(new PacketData(3, "Hellow, world.".getBytes()));
		PacketData sc = new PacketData();
		sc.setCode((short) 2);
		sc.setVersion((byte) 8);
		sc.setLength(1024 * 1024 + 6);
		sc.setData(new byte[1024 * 1024 + 6]);
		sc.getData()[9] = (byte) 145;
		sc.getData()[145] = (byte) 63;
		packetClient.handleWrite(sc);
		latch.await();
		packetClient.stop();

		assertEquals((short) 2, one.a.getCode());
		assertEquals((byte) 8, one.a.getVersion());
		assertEquals(1024 * 1024 + 6, one.a.getLength());
		assertEquals(1024 * 1024 + 6, one.a.getData().length);
		assertEquals((byte) 145, one.a.getData()[9]);
		assertEquals((byte) 63, one.a.getData()[145]);

		// Test stop again.
		packetClient.stop();
		assertEquals(0, packetClient.size());
	}

	@Test
    public void testReadLoopFalse() throws IOException {
	    final PacketClient pc = new PacketClient();
        InputStream in = mock(InputStream.class);
        ReadLoop r = pc.new ReadLoop(in);
        r.run();
	}

	@Test
    public void testReadLoop() throws IOException {
        final PacketClient pc = new PacketClient();
        byte[] abc = new byte[10];
        InputStream in = new ByteArrayInputStream(abc);
        ReadLoop r = pc.new ReadLoop(in);
        pc.isWorking = true;

        IPacketHandler h = mock(IPacketHandler.class);
        pc.setPacketHandler(h);

        r.run();
        assertFalse(pc.isWorking);
        verify(h, never()).handlePacket(any(PacketData.class), eq(pc));
        verify(h, times(1)).handleClose(pc);
    }

	/**
	 * Test method for
	 * {@link org.apache.niolex.network.client.PacketClient#handleWrite(PacketData)}
	 * .
	 */
	@Test
	public void testHandleWrite() throws Exception {
	    final PacketClient pc = new PacketClient(CoreRunner.SERVER_ADDR);
	    OutputStream out = new ByteArrayOutputStream() {
	        int cnt = 0;

            /**
             * This is the override of super method.
             * @see java.io.ByteArrayOutputStream#write(int)
             */
            @Override
            public synchronized void write(int b) {
                if (cnt++ > 1)
                    throw new NullPointerException("Fun.Run");
            }

            /**
             * This is the override of super method.
             * @see java.io.ByteArrayOutputStream#write(byte[], int, int)
             */
            @Override
            public synchronized void write(byte[] b, int off, int len) {
                if (cnt++ > 1)
                    throw new NullPointerException("Fun.Run");
            }
	    };
	    WriteLoop wl = pc.new WriteLoop(out);
	    pc.setConnectTimeout(2);
	    pc.isWorking = true;
	    wl.run();
	}

	/**
     * Test method for
     * {@link org.apache.niolex.network.client.PacketClient#handleWrite(PacketData)}
     * .
     */
    @Test
    public void testHandleWriteListFull() throws Exception {
        final PacketClient pc = new PacketClient(CoreRunner.SERVER_ADDR);
        for (int i = 0; i < Config.CLIENT_MAX_QUEUE_SIZE; ++i) {
            pc.handleWrite(PacketData.getHeartBeatPacket());
        }
        Thread t = Runner.run(pc, "handleWrite", PacketData.getHeartBeatPacket());
        Thread.sleep(CoreRunner.CO_SLEEP);
        assertTrue(t.isAlive());
        t.interrupt();
        t.join();
    }

}
