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
import static org.mockito.Mockito.*;

import org.apache.niolex.network.Config;
import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.TBasePacketWriter;
import org.apache.niolex.network.event.WriteEvent;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-6-14
 */
public class HeartBeatAdapterTest {

    public static final IPacketHandler other = mock(IPacketHandler.class);
    public static final HeartBeatAdapter ha = new HeartBeatAdapter(other);

    /**
     * Test method for {@link org.apache.niolex.network.adapter.HeartBeatAdapter#start()}.
     * @throws Exception
     */
    @BeforeClass
    public static void setup() {
        ha.setHeartBeatInterval(20);
        ha.start();
    }

    @AfterClass
    public static void stop() {
        ha.stop();
        assertFalse(ha.isWorking());
    }

    @Test
    public void testHandlePacketForce() throws Exception {
        ha.setForceHeartBeat(true);
        // started now.
        IPacketWriter wt = spy(new TBasePacketWriter());
        PacketData sc = new PacketData();
        ha.handlePacket(sc, wt);
        verify(wt, atLeast(1)).attachData(anyString(), anyObject());
        verify(wt).addEventListener(ha);
    }

    @Test
    public void testHandlePacketForceAlready() throws Exception {
        ha.setForceHeartBeat(true);
        // started now.
        IPacketWriter wt = spy(new TBasePacketWriter());
        wt.attachData(Config.ATTACH_KEY_HEART_BEAT, System.currentTimeMillis());
        PacketData sc = new PacketData();
        ha.handlePacket(sc, wt);
        verify(wt, times(1)).attachData(anyString(), anyObject());
        verify(wt, never()).addEventListener(ha);
    }

    @Test
    public void testHandlePacketNotForce() throws Exception {
        ha.setForceHeartBeat(false);
        // started now.
        IPacketWriter wt = spy(new TBasePacketWriter());
        PacketData sc = new PacketData();
        ha.handlePacket(sc, wt);
        verify(wt, times(0)).attachData(anyString(), anyObject());
        verify(wt, never()).addEventListener(ha);
    }

    @Test
    public void testHandlePacketRegi() throws Exception {
        ha.setForceHeartBeat(false);
        // started now.
        IPacketWriter wt = spy(new TBasePacketWriter());
        PacketData sc = new PacketData(Config.CODE_REGR_HBEAT, new byte[0]);
        ha.handlePacket(sc, wt);
        verify(wt).attachData(anyString(), anyObject());
        verify(wt).addEventListener(ha);
        // handle again, will not do anything this time.
        ha.handlePacket(sc, wt);
        verify(wt).attachData(anyString(), anyObject());
        verify(wt).addEventListener(ha);
    }

    @Test
    public void testAfterSendOK() throws Exception {
        IPacketWriter wt = spy(new TBasePacketWriter());
        wt.attachData(Config.ATTACH_KEY_HEART_BEAT, System.currentTimeMillis());
        WriteEvent wEvent = new WriteEvent();
        wEvent.setPacketWriter(wt);
        ha.afterSend(wEvent);
        verify(wt, times(2)).attachData(anyString(), anyObject());
    }

    @Test
    public void testAfterSendNotAttach() throws Exception {
        IPacketWriter wt = spy(new TBasePacketWriter());
        WriteEvent wEvent = new WriteEvent();
        wEvent.setPacketWriter(wt);
        ha.afterSend(wEvent);
        verify(wt, never()).attachData(anyString(), anyObject());
    }

    @Test
    public void testHandleClose() throws Exception {
        IPacketWriter wt = spy(new TBasePacketWriter());
        ha.handleClose(wt);
        verify(wt).attachData(Config.ATTACH_KEY_HEART_BEAT, null);
    }

    @Test
    public void testRun() throws Exception {
        HeartBeatAdapter ada = new HeartBeatAdapter(null);
        ada.run();
        assertFalse(ada.isWorking());
    }

	@Test
	public void testHandleHeartBeat() throws Exception {
		assertTrue(ha.isWorking());
		ha.setForceHeartBeat(false);
		reset(other);

		// started now.
		IPacketWriter wt = spy(new TBasePacketWriter());
		PacketData sc = new PacketData(Config.CODE_REGR_HBEAT, new byte[0]);

		// Regi heart beat.
		ha.handlePacket(sc, wt);

		// Send data.
		PacketData sc2 = new PacketData(3, "This is hearrr".getBytes());
		ha.handlePacket(sc2, wt);

		// verify
		verify(other, times(0)).handlePacket(sc, wt);
		verify(other).handlePacket(sc2, wt);

		// Create another
		IPacketWriter wt2 = spy(new TBasePacketWriter());

		// Regi heart beat.
		ha.handlePacket(sc2, wt2);

		Thread.sleep(100);

		ha.handlePacket(sc, wt2);

		// wt2 has to send a packet.
		WriteEvent wEvent = new WriteEvent();
		wEvent.setPacketData(sc2);
		wEvent.setPacketWriter(wt2);
		ha.afterSend(wEvent);

		wt2.attachData(Config.ATTACH_KEY_HEART_BEAT, null);

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
		final HeartBeatAdapter ada = new HeartBeatAdapter(other);
		ada.setHeartBeatInterval(10);
		ada.setForceHeartBeat(true);
		// started now.
		IPacketWriter wt = spy(new TBasePacketWriter());
		PacketData sc = new PacketData(789, new byte[0]);
		ada.handlePacket(sc, wt);
		assertEquals(10, ada.getHeartBeatInterval());
		ada.start();
		Thread.sleep(100);
		verify(wt, atLeast(1)).handleWrite(PacketData.getHeartBeatPacket());
		ada.stop();
	}

}
