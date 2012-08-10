/**
 * BasePacketWriterTest.java
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
package org.apache.niolex.network.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.TBasePacketWriter;
import org.apache.niolex.network.server.BasePacketWriter;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-5-30
 */
public class BasePacketWriterTest {
	BasePacketWriter bpw;

	@Before
	public void setup() {
		bpw = new TBasePacketWriter ();
	}

	@Test
	public void testClose() {
		BasePacketWriter tmp = new TBasePacketWriter ();
		tmp.handleWrite(PacketData.getHeartBeatPacket());
		tmp.attachData("a", tmp);
		assertNotNull(tmp.getAttached("a"));
		tmp.channelClosed();
		boolean g = false;
		try {
			tmp.handleWrite(PacketData.getHeartBeatPacket());
		} catch (IllegalStateException e) {
			g = true;
		}
		assertTrue(g);
		assertNull(tmp.getAttached("a"));
		tmp.channelClosed();
	}

	/**
	 * Test method for {@link org.apache.niolex.network.server.BasePacketWriter#handleWrite(org.apache.niolex.network.PacketData)}.
	 */
	@Test
	public void testHandleWrite() {
		PacketData sc = new PacketData();
        sc.setCode((short)4);
        sc.setVersion((byte)1);
        sc.setLength(0);
        sc.setData(new byte[0]);
		bpw.handleWrite(sc);
		assertEquals(sc, bpw.handleNext());
		assertTrue(bpw.isEmpty());
	}

	/**
	 * Test method for {@link org.apache.niolex.network.server.BasePacketWriter#attachData(java.lang.String, java.lang.Object)}.
	 */
	@Test
	public void testAttachData() {
		bpw.attachData("IDIJF", "Not yet implemented");
		assertEquals("Not yet implemented", bpw.getAttached("IDIJF"));
	}

	@Test
	public void testHandleWriteM() throws InterruptedException {
		PacketData sc = new PacketData();
        sc.setCode((short)4);
        sc.setVersion((byte)1);
        sc.setLength(0);
        sc.setData(new byte[0]);
		bpw.handleWrite(sc);
		bpw.handleWrite(PacketData.getHeartBeatPacket());
		bpw.handleWrite(sc);
		bpw.handleWrite(PacketData.getHeartBeatPacket());
		bpw.handleWrite(sc);
		bpw.handleWrite(PacketData.getHeartBeatPacket());
		PacketData pc = new PacketData(4, new byte[4]);
		bpw.handleWrite(sc);
		bpw.handleWrite(pc);
		bpw.handleWrite(sc);
		bpw.handleWrite(pc);
		bpw.handleWrite(sc);
		bpw.handleWrite(pc);
		bpw.handleWrite(sc);
		ConcurrentLinkedQueue<PacketData> queue = bpw.getRemainQueue();
		assertEquals(13, queue.size());
		assertEquals(sc, bpw.handleNext());
		assertEquals(PacketData.getHeartBeatPacket(), bpw.handleNext());
		assertEquals(sc, queue.poll());
		assertEquals(10, queue.size());
	}
}
