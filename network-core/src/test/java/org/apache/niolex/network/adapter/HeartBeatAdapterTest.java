/**
 * HeartBeatAdapterTest.java
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
package org.apache.niolex.network.adapter;

import static org.junit.Assert.*;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.apache.niolex.network.Config;
import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.TBasePacketWriter;
import org.apache.niolex.network.event.WriteEvent;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-14
 */
public class HeartBeatAdapterTest {

	/**
	 * Test method for {@link org.apache.niolex.network.adapter.HeartBeatAdapter#start()}.
	 * @throws Exception
	 */
	@Test
	public void testStart() throws Exception {
		IPacketHandler other = mock(IPacketHandler.class);
		HeartBeatAdapter ha = new HeartBeatAdapter(other);
		ha.setHeartBeatInterval(200);
		ha.start();
		assertTrue(ha.isWorking());

		// started now.
		IPacketWriter wt = spy(new TBasePacketWriter());
		PacketData sc = new PacketData(Config.CODE_REGR_HBEAT, new byte[0]);

		// Regi heart beat.
		ha.handleRead(sc, wt);

		// Send data.
		PacketData sc2 = new PacketData(3, "This is hearrr".getBytes());
		ha.handleRead(sc2, wt);

		// verify
		verify(other, times(0)).handleRead(sc, wt);
		verify(other).handleRead(sc2, wt);

		// Create another
		IPacketWriter wt2 = spy(new TBasePacketWriter());

		// Regi heart beat.
		ha.handleRead(sc, wt2);

		Thread.sleep(100);

		// wt2 has to send a packet.
		WriteEvent wEvent = new WriteEvent();
		wEvent.setPacketData(sc2);
		wEvent.setPacketWriter(wt2);
		ha.afterSend(wEvent);

		Thread.sleep(150);

		// After then, heart beat should be ready now.
		verify(wt, atLeast(1)).handleWrite(PacketData.getHeartBeatPacket());
		verify(wt2, never()).handleWrite(PacketData.getHeartBeatPacket());

		// Close wt2
		ha.handleClose(wt2);

		// Sleep more than heart beat.
		Thread.sleep(220);

		// After then, heart beat should be ready now.
		verify(wt, atLeast(2)).handleWrite(PacketData.getHeartBeatPacket());
		verify(wt2, never()).handleWrite(PacketData.getHeartBeatPacket());

		// stoped now.
		ha.stop();
		assertFalse(ha.isWorking());
	}

	/**
	 * Test method for {@link org.apache.niolex.network.adapter.HeartBeatAdapter#getHeartBeatInterval()}.
	 */
	@Test
	public void testGetHeartBeatInterval() {
		HeartBeatAdapter ada = new HeartBeatAdapter(null);
		ada.setHeartBeatInterval(8123);
		assertEquals(8123, ada.getHeartBeatInterval());
	}

	@Test
	public void testForceHeartBeat() throws Exception {
		IPacketHandler other = mock(IPacketHandler.class);
		HeartBeatAdapter ada = new HeartBeatAdapter(other);
		ada.setHeartBeatInterval(10);
		ada.setForceHeartBeat(true);
		// started now.
		IPacketWriter wt = spy(new TBasePacketWriter());
		PacketData sc = new PacketData(789, new byte[0]);
		ada.handleRead(sc, wt);
		assertEquals(10, ada.getHeartBeatInterval());
		ada.start();
		Thread.sleep(100);
		verify(wt, atLeast(1)).handleWrite(PacketData.getHeartBeatPacket());
	}
}
