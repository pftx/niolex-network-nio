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

import static org.junit.Assert.*;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.TBasePacketWriter;
import org.apache.niolex.network.adapter.HeartBeatAdapter;
import org.apache.niolex.network.event.WriteEventListener;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-5-30
 */
public class BasePacketWriterTest {
	BasePacketWriter bpw;

	@Before
	public void setup() {
		bpw = new TBasePacketWriter();
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
	    assertFalse(bpw.isEmpty());
	    assertEquals(sc, bpw.handleNext());
	    assertTrue(bpw.isEmpty());
	}

	/**
     * Test method for {@link org.apache.niolex.network.server.BasePacketWriter#handleWrite(org.apache.niolex.network.PacketData)}.
     */
    @Test(expected=IllegalStateException.class)
    public void testHandleWriteWhenClosed() {
        bpw.channelClosed();
        bpw.handleWrite(PacketData.getHeartBeatPacket());
    }

	@Test
	public void testChannelClosed() {
	    bpw.handleWrite(PacketData.getHeartBeatPacket());
	    bpw.attachData("a", "b");
		assertEquals("b", bpw.getAttached("a"));
		bpw.channelClosed();
		boolean g = false;
		try {
		    bpw.handleWrite(PacketData.getHeartBeatPacket());
		} catch (IllegalStateException e) {
			g = true;
		}
		assertTrue(g);
		assertNull(bpw.getAttached("a"));
		// close again.
		bpw.channelClosed();
	}

    @Test
    public void testAddEventListener() throws Exception {
        WriteEventListener listener = new HeartBeatAdapter(null);
        bpw.addEventListener(listener);
        bpw.fireSendEvent(PacketData.getHeartBeatPacket());
    }

    @Test(expected=NullPointerException.class)
    public void testAddEventListenerAfterClosed() throws Exception {
        WriteEventListener listener = new HeartBeatAdapter(null);
        bpw.addEventListener(listener);
        bpw.channelClosed();
        bpw.addEventListener(listener);
    }

	/**
	 * Test method for {@link org.apache.niolex.network.server.BasePacketWriter#attachData(java.lang.String, java.lang.Object)}.
	 */
	@Test
	public void testAttachData() {
		bpw.attachData("IDIJF", "Not yet implemented");
		assertEquals("Not yet implemented", bpw.getAttached("IDIJF"));
		PacketData sc = new PacketData();
		bpw.attachData("IDIJF", sc);
		assertEquals(sc, bpw.getAttached("IDIJF"));
	}

	@Test
	public void testGetRemainQueue() throws InterruptedException {
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
