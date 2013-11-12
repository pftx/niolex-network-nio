/**
 * SocketClientTest.java
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
import java.util.ArrayList;
import java.util.List;

import org.apache.niolex.network.CoreRunner;
import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.example.SavePacketHandler;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-6-13
 */

public class SocketClientTest {

	@BeforeClass
	public static void run() throws Exception {
		CoreRunner.createServer();
	}

	@AfterClass
	public static void down() throws Exception {
		CoreRunner.shutdown();
	}

	/**
	 * Test method for {@link org.apache.niolex.network.client.SocketClient#SocketClient()}.
	 */
	@Test
	public void testSocketClient() {
		SocketClient sc = new SocketClient();
		InetSocketAddress inn = new InetSocketAddress("localhost", CoreRunner.PORT);
		sc.setServerAddress(inn);
		assertFalse(sc.isWorking());
		sc.setConnectTimeout(90821);
		sc.setPacketHandler(null);
		assertEquals(inn, sc.getServerAddress());
		sc.socket = mock(Socket.class);
        when(sc.socket.getLocalPort()).thenReturn(3527);
        try {
            doThrow(new IOException("Moke#Mock#Make")).when(sc.socket).close();
        } catch (IOException e) {
        }
		sc.stop();
	}

	@Test
	public void testConnect() throws IOException {
		SocketClient sc = new SocketClient(CoreRunner.SERVER_ADDR);
		List<PacketData> list = new ArrayList<PacketData>();
		sc.setPacketHandler(new SavePacketHandler(list));
		sc.connect();
		sc.setAutoRead(true);
		sc.handleWrite(new PacketData(6, "Good."));
		assertEquals(list.size(), 1);
		sc.setAutoRead(false);
		sc.handleWrite(new PacketData(2, "Hellow, world.".getBytes()));
		sc.stop();
		assertEquals(list.size(), 1);
		boolean flag = false;
		try {
			sc.handleWrite(new PacketData(6, "Morning."));
		} catch (IllegalStateException e) {
			flag = true;
		}
		assertTrue(flag);
        assertFalse(sc.isWorking());
	}

	@Test
	public void testConnRecon() throws Exception {
	    SocketClient sc = new SocketClient();
	    InetSocketAddress inn = new InetSocketAddress("localhost", CoreRunner.PORT);
	    sc.setServerAddress(inn);
	    IPacketHandler hand = mock(IPacketHandler.class);
	    sc.setPacketHandler(hand);
	    sc.connect();
	    sc.stop();
	    sc.isWorking = true;
	    boolean flag = false;
	    try {
	        sc.handleWrite(new PacketData(6, "Morning."));
	    } catch (IllegalStateException e) {
	        flag = true;
	    }
	    assertTrue(flag);
	    assertFalse(sc.isWorking());
	    Thread.sleep(100);
	    verify(hand).handleClose(sc);
	}

    @Test
    public void testIsAutoRead() throws Exception {
        SocketClient sc = new SocketClient();
        assertTrue(sc.isAutoRead());
        sc.setAutoRead(false);
        assertFalse(sc.isAutoRead());
        sc.stop();
    }

}
