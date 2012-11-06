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

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.niolex.network.client.PacketClient;
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
		p.parsePacket(in);
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

	@Test
	public void testStr() {
		PacketData pc = new PacketData(34, "setReserved(byte)");
		assertEquals(34, pc.getCode());
		assertEquals(17, pc.getLength());
	}

	@Test(expected=IOException.class)
	public void testparsePacket() throws IOException {
		PacketData pc = new PacketData();
		ByteBuffer ba = ByteBuffer.allocate(12);
		ba.putInt(12345);
		ba.putInt(10485761);
		ba.putInt(10485761);

		pc.parsePacket(new DataInputStream(new ByteArrayInputStream(ba.array())));
		assertEquals(10485760, pc.getLength());
	}

	@Test
	public void testparseHeader() {
		PacketData pc = new PacketData();
		ByteBuffer ba = ByteBuffer.allocate(8);
		ba.putInt(12345);
		ba.putInt(10485760);
		ba.flip();
		pc.parseHeader(ba);
		assertEquals(10485760, pc.getLength());
	}

	@Test(expected=IllegalStateException.class)
	public void testparseHeaderErr() {
		PacketData pc = new PacketData();
		ByteBuffer ba = ByteBuffer.allocate(8);
		ba.putInt(12345);
		ba.putInt(10485761);
		ba.flip();
		pc.parseHeader(ba);
		assertEquals(10485760, pc.getLength());
	}

}
