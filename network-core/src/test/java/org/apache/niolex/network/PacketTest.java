/**
 * PacketTest.java
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
package org.apache.niolex.network;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.niolex.network.client.PacketClient;
import org.apache.niolex.network.demo.PrintPacketHandler;
import org.apache.niolex.network.server.NioServer;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-4
 */
public class PacketTest {

	/**
	 * Test method for {@link org.apache.niolex.network.Packet#getVersion()}.
	 */
	@Test
	public void testGetVersion() {
		PacketData p = PacketData.getHeartBeatPacket();
		PacketData o = p.makeCopy();
		assertEquals(0, o.getCode());

		assertEquals("00000100", p.descriptor());
	}

	/**
	 * Test method for {@link org.apache.niolex.network.Packet#setVersion(byte)}.
	 */
	@Test(expected=IOException.class)
	public void testParseStream() throws IOException {
		byte[] b = new byte[9];
		for (int i = 0; i < 7; ++i) {
			b[i] = 0;
		}
		b[7] = b[8] = 10;
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(b));
		PacketData p = PacketData.getHeartBeatPacket();
		p.parseHeader(in);
	}

	/**
	 * Test method for {@link org.apache.niolex.network.Packet#getReserved()}.
	 */
	@Test
	public void testGetReserved() {
		PacketClient p = new PacketClient();
		p.stop();
	}

	@Test
	public void testGConstr() {
		PacketData pc = new PacketData(34);
		assertEquals(34, pc.getCode());
	}

	/**
	 * Test method for {@link org.apache.niolex.network.Packet#setReserved(byte)}.
	 * @throws IOException
	 */
	@Test
	public void testSetReserved() throws IOException {
		NioServer nioServer = new NioServer();
		nioServer.setPort(8808);
		nioServer.start();
		PacketClient c = new PacketClient(new InetSocketAddress("localhost", 8808));
		c.setPacketHandler(new PrintPacketHandler());
		c.connect();
		nioServer.stop();
	}

}
