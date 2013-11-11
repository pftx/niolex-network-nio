/**
 * FaultTolerateSPacketHandlerTest.java
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
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.niolex.commons.collection.CircularList;
import org.apache.niolex.commons.reflect.FieldUtil;
import org.apache.niolex.network.Config;
import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.TBasePacketWriter;
import org.apache.niolex.network.event.WriteEvent;
import org.apache.niolex.network.server.BasePacketWriter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-5-31
 */
@RunWith(MockitoJUnitRunner.class)
public class FaultTolerateAdapterTest {

	@Mock
	private IPacketHandler h;
	@Mock
    private IPacketWriter wt;
	private BasePacketWriter bpw = new TBasePacketWriter();
	private FaultTolerateAdapter fault;

	@Before
	public void createFaultTolerateSPacketHandler() throws Exception {
		fault = new FaultTolerateAdapter(h);
		bpw = spy(bpw);
	}

	/**
     * Test method for
     * {@link org.apache.niolex.network.adapter.FaultTolerateAdapter#handlePacket(PacketData, IPacketWriter)}.
     */
    @Test
    public void testHandlePacketOther() {
        PacketData sc = new PacketData();
        fault.handlePacket(sc, wt);
        verify(h, times(1)).handlePacket(sc, wt);
    }

    /**
     * Test method for
     * {@link org.apache.niolex.network.adapter.FaultTolerateAdapter#handlePacket(PacketData, IPacketWriter)}.
     */
    @Test
    public void testHandlePacketNoRestore() {
        PacketData sc = new PacketData(Config.CODE_REGR_UUID, "AJFIUEALKD".getBytes());
        fault.handlePacket(sc, wt);
        verify(h, times(0)).handlePacket(sc, wt);
        verify(wt).addEventListener(fault);
        verify(wt, times(2)).attachData(anyString(), anyObject());
    }

	/**
     * Test method for
     * {@link org.apache.niolex.network.adapter.FaultTolerateAdapter#handlePacket(PacketData, IPacketWriter)}.
     */
    @Test
    public void testHandlePacket() {
        PacketData sc = new PacketData(Config.CODE_REGR_UUID, "AJFIUEALKD".getBytes());
        PacketData sc2 = new PacketData(3, "AJ231FIUEALKD".getBytes());
        // Regi
        fault.handlePacket(sc, bpw);
        // Data
        fault.handlePacket(sc2, bpw);
        verify(h, times(0)).handlePacket(sc, bpw);
        verify(h, times(1)).handlePacket(sc2, bpw);

        verify(bpw).attachData(Config.ATTACH_KEY_FAULTTO_UUID, "AJFIUEALKD");

        // Test fault tolerate.
        bpw.handleWrite(sc2);
        fault.handleClose(bpw);
        // after this, we have 1 data for tolerate: sc2 which is not sent

        // after ERROR, client closed. create a new one.
        TBasePacketWriter wt = spy(new TBasePacketWriter());
        // regi again, should handle fault tolerate.
        fault.handlePacket(sc, wt);
        ArgumentCaptor<PacketData> argument = ArgumentCaptor.forClass(PacketData.class);
        verify(wt, times(1)).handleWrite(argument.capture());
        PacketData aa = (PacketData) argument.getValue();
        assertEquals(sc2, aa);

        fault.handleClose(wt);
    }

    @Test
    public void testRestorePacketsNull() throws Exception {
        fault.restorePackets("AJFIUEALKD", bpw);
    }

    @Test
    public void testRestorePackets() throws Exception {
        PacketData sc = new PacketData(Config.CODE_REGR_UUID, "AJFIUEALKD".getBytes());
        PacketData sc2 = new PacketData(39, "AJ231FIUEALKD".getBytes());
        PacketData sc3 = new PacketData(59, "(*&SDFJIODF".getBytes());
        PacketData sc4 = new PacketData(69, "(*)(@NKNF:DSL:M".getBytes());
        fault.handlePacket(sc, bpw);
        WriteEvent wEvent = new WriteEvent();
        wEvent.setPacketData(sc2);
        wEvent.setPacketWriter(bpw);
        fault.afterSend(wEvent);
        bpw.handleWrite(sc3);
        fault.handleClose(bpw);
        // After close, the dataMap have 2 item.
        TBasePacketWriter wt = spy(new TBasePacketWriter());
        wt.handleWrite(sc4);
        fault.handlePacket(sc, wt);
        ConcurrentLinkedQueue<PacketData> data = wt.getRemainQueue();
        assertEquals(sc2, data.poll());
        assertEquals(sc3, data.poll());
        assertEquals(sc4, data.poll());
    }

	/**
	 * Test method for
	 * {@link org.apache.niolex.network.adapter.FaultTolerateAdapter#handleClose(org.apache.niolex.network.IPacketWriter)}
	 * .
	 */
	@Test
	public void testHandleCloseSimple() {
		fault.handleClose(wt);
	}

	/**
	 * Test method for
	 * {@link org.apache.niolex.network.adapter.FaultTolerateAdapter#storePackets(String, BasePacketWriter, CircularList)}
	 * .
	 */
	@Test
	public void testStorePacketsSSidNull() {
	    fault.storePackets(null, bpw, null);
		IPacketWriter wt = new TBasePacketWriter();
		wt.attachData(Config.ATTACH_KEY_FAULTTO_UUID, "haha_me");
		fault.handleClose(wt);
	}

	/**
	 * Test method for
	 * {@link org.apache.niolex.network.adapter.FaultTolerateAdapter#storePackets(String, BasePacketWriter, CircularList)}
	 * .
	 * @throws Exception
	 * @throws SecurityException
	 */
	@Test
	public void testStorePacketsListNull() throws SecurityException, Exception {
	    TBasePacketWriter wt = new TBasePacketWriter();
	    PacketData sc2 = new PacketData(39, "AJ231FIUEALKD".getBytes());
	    wt.handleWrite(sc2);
	    fault.storePackets("haha_me", wt, null);
	    Field f = FieldUtil.getField(FaultTolerateAdapter.class, "dataMap");
	    Map<String, ConcurrentLinkedQueue<PacketData>> dataMap = FieldUtil.getFieldValue(f, fault);
	    ConcurrentLinkedQueue<PacketData> data = dataMap.get("haha_me");
	    assertEquals(sc2, data.poll());
	    fault.handleClose(wt);
	}

	/**
	 * Test method for
	 * {@link org.apache.niolex.network.adapter.FaultTolerateAdapter#storePackets(String, BasePacketWriter, CircularList)}
	 * .
	 * @throws Exception
	 * @throws SecurityException
	 */
	@Test
	public void testStorePackets() throws SecurityException, Exception {
	    TBasePacketWriter wt = new TBasePacketWriter();
        PacketData sc2 = new PacketData(39, "AJ231FIUEALKD".getBytes());
        PacketData sc3 = new PacketData(59, "(*&SDFJIODF".getBytes());
        PacketData sc4 = new PacketData(69, "(*)(@NKNF:DSL:M".getBytes());
        wt.handleWrite(sc2);
        CircularList<PacketData> list = new CircularList<PacketData>(2);
        list.add(sc3);
        list.add(sc4);
        fault.storePackets("haha_me", wt, list);
        Field f = FieldUtil.getField(FaultTolerateAdapter.class, "dataMap");
        Map<String, ConcurrentLinkedQueue<PacketData>> dataMap = FieldUtil.getFieldValue(f, fault);
        ConcurrentLinkedQueue<PacketData> data = dataMap.get("haha_me");
        assertEquals(sc3, data.poll());
        assertEquals(sc4, data.poll());
        assertEquals(sc2, data.poll());
        fault.handleClose(wt);
	}

	/**
	 * Test method for
	 * {@link org.apache.niolex.network.adapter.FaultTolerateAdapter#handlePacket(PacketData, IPacketWriter)}
	 * .
	 */
	@Test
	public void testHandlePacketNotRegi() {
		PacketData sc = mock(PacketData.class);
		IPacketWriter wt = mock(IPacketWriter.class);
		fault.handlePacket(sc, wt);
		verify(h, times(1)).handlePacket(sc, wt);
	}

	/**
	 * Test method for
	 * {@link org.apache.niolex.network.adapter.FaultTolerateAdapter#handlePacket(PacketData, IPacketWriter)}
	 * .
	 */
	@Test
	public void testHandlePacketRegi() {
		PacketData sc = new PacketData(Config.CODE_REGR_UUID, "AJFIUEALKD".getBytes());
		IPacketWriter wt = mock(IPacketWriter.class);
		fault.handlePacket(sc, wt);
		verify(wt).attachData(Config.ATTACH_KEY_FAULTTO_UUID, "AJFIUEALKD");
	}

	/**
	 * Test method for
	 * {@link org.apache.niolex.network.adapter.FaultTolerateAdapter#handlePacket(PacketData, IPacketWriter)}
	 * .
	 */
	@Test
	public void testHandleRR() {
		WriteEvent w = new WriteEvent();
		PacketData sc2 = new PacketData(3, "AJ231FIUEALKD".getBytes());
		IPacketWriter wt = mock(IPacketWriter.class);
		w.setPacketData(sc2);
		w.setPacketWriter(wt);
		fault.afterSend(w);
	}

	/**
	 * Test method for
	 * {@link org.apache.niolex.network.adapter.FaultTolerateAdapter#handlePacket(PacketData, IPacketWriter)}
	 * .
	 */
	@Test
	public void testHandleRROk() {
		PacketData sc = new PacketData(Config.CODE_REGR_UUID, "AJFIUEALKD".getBytes());
		IPacketWriter wt = new TBasePacketWriter();
		fault.handlePacket(sc, wt);
		WriteEvent w = new WriteEvent();
		PacketData sc2 = new PacketData(3, "AJ231FIU3212312EALKD".getBytes());
		w.setPacketData(sc2);
		w.setPacketWriter(wt);
		fault.afterSend(w);
		fault.handleClose(wt);

		// after error.
		IPacketWriter wt2 = mock(IPacketWriter.class);
		// regi again
		fault.handlePacket(sc, wt2);
		verify(wt2).attachData(Config.ATTACH_KEY_FAULTTO_UUID, "AJFIUEALKD");

		TBasePacketWriter wt3 = spy(new TBasePacketWriter());
		fault.handlePacket(sc, wt3);
		verify(wt3).attachData(Config.ATTACH_KEY_FAULTTO_UUID, "AJFIUEALKD");

		ArgumentCaptor<PacketData> argument = ArgumentCaptor.forClass(PacketData.class);
		verify(wt3, times(1)).handleWrite((PacketData) argument.capture());
		assertEquals(sc2, argument.getValue());
	}

}
