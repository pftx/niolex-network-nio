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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.niolex.commons.collection.CircularList;
import org.apache.niolex.network.Config;
import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.TBasePacketWriter;
import org.apache.niolex.network.event.WriteEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-5-31
 */
public class FaultTolerateAdapterTest {
	@Mock
	private IPacketHandler h;
	private FaultTolerateAdapter faultTolerateSPacketHandler;

	@Before
	public void createFaultTolerateSPacketHandler() throws Exception {
		faultTolerateSPacketHandler = new FaultTolerateAdapter(h);
	}

	/**
	 * Test method for
	 * {@link org.apache.niolex.network.handler.FaultTolerateAdapter#handleClose(org.apache.niolex.network.IPacketWriter)}
	 * .
	 */
	@Test
	public void testHandleCloseSimple() {
		IPacketWriter wt = spy(new TBasePacketWriter());
		faultTolerateSPacketHandler.handleClose(wt);
	}

	/**
	 * Test method for
	 * {@link org.apache.niolex.network.handler.FaultTolerateAdapter#handleClose(org.apache.niolex.network.IPacketWriter)}
	 * .
	 */
	@Test
	public void testHandleCloseSSid_list_null() {
		IPacketWriter wt = new TBasePacketWriter();
		wt.attachData(Config.ATTACH_KEY_FAULTTO_UUID, "haha_me");
		faultTolerateSPacketHandler.handleClose(wt);
	}

	/**
	 * Test method for
	 * {@link org.apache.niolex.network.handler.FaultTolerateAdapter#handleClose(org.apache.niolex.network.IPacketWriter)}
	 * .
	 */
	@Test
	public void testHandleCloseSSid_list_ok() {
		IPacketWriter wt = new TBasePacketWriter();
		wt.attachData(Config.ATTACH_KEY_FAULTTO_UUID, "haha_me");
		CircularList<PacketData> list = new CircularList<PacketData>(2);
		wt.attachData(Config.ATTACH_KEY_FAULT_RRLIST, list);
		list.add(PacketData.getHeartBeatPacket());

		faultTolerateSPacketHandler.handleClose(wt);
	}

	/**
	 * Test method for
	 * {@link org.apache.niolex.network.handler.FaultTolerateAdapter#handleClose(org.apache.niolex.network.IPacketWriter)}
	 * .
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testHandleRead() {
		PacketData sc = new PacketData(Config.CODE_REGR_UUID, "AJFIUEALKD".getBytes());

		TBasePacketWriter wt0 = spy(new TBasePacketWriter());
		doReturn("Mock").when(wt0).getRemoteName();

		PacketData sc2 = new PacketData(3, "AJ231FIUEALKD".getBytes());
		// Regi
		faultTolerateSPacketHandler.handleRead(sc, wt0);
		// Data
		faultTolerateSPacketHandler.handleRead(sc2, wt0);
		verify(h, times(0)).handleRead(sc, wt0);
		verify(h, times(1)).handleRead(sc2, wt0);

		wt0.handleWrite(sc2);
		verify(wt0).attachData(Config.ATTACH_KEY_FAULTTO_UUID, "AJFIUEALKD");
		faultTolerateSPacketHandler.handleClose(wt0);
		// after this, we have 1 data for tolerate: sc2 which is not sent

		// after ERROR, client closed. create a new one.
		TBasePacketWriter wt = spy(new TBasePacketWriter());
		// regi again, should handle fault tolerate.
		faultTolerateSPacketHandler.handleRead(sc, wt);
		ArgumentCaptor<Object> argument = ArgumentCaptor.forClass(Object.class);
		verify(wt, times(1)).replaceQueue((ConcurrentLinkedQueue<PacketData>) argument.capture());
		ConcurrentLinkedQueue<PacketData> aa = (ConcurrentLinkedQueue<PacketData>) argument.getValue();
		assertEquals(sc2, aa.peek());

		faultTolerateSPacketHandler.handleClose(wt);
	}

	/**
	 * Test method for
	 * {@link org.apache.niolex.network.handler.FaultTolerateAdapter#handleRead(org.apache.niolex.network.PacketData, org.apache.niolex.network.IPacketWriter)}
	 * .
	 */
	@Test
	public void testHandleReadNotRegi() {
		PacketData sc = mock(PacketData.class);
		IPacketWriter wt = mock(IPacketWriter.class);
		faultTolerateSPacketHandler.handleRead(sc, wt);
		verify(h, times(1)).handleRead(sc, wt);
	}

	/**
	 * Test method for
	 * {@link org.apache.niolex.network.handler.FaultTolerateAdapter#handleRead(org.apache.niolex.network.PacketData, org.apache.niolex.network.IPacketWriter)}
	 * .
	 */
	@Test
	public void testHandleReadRegi() {
		PacketData sc = new PacketData(Config.CODE_REGR_UUID, "AJFIUEALKD".getBytes());
		IPacketWriter wt = mock(IPacketWriter.class);
		faultTolerateSPacketHandler.handleRead(sc, wt);
		verify(wt).attachData(Config.ATTACH_KEY_FAULTTO_UUID, "AJFIUEALKD");
	}

	/**
	 * Test method for
	 * {@link org.apache.niolex.network.handler.FaultTolerateAdapter#handleRead(org.apache.niolex.network.PacketData, org.apache.niolex.network.IPacketWriter)}
	 * .
	 */
	@Test
	public void testHandleRR() {
		WriteEvent w = new WriteEvent();
		PacketData sc2 = new PacketData(3, "AJ231FIUEALKD".getBytes());
		IPacketWriter wt = mock(IPacketWriter.class);
		w.setPacketData(sc2);
		w.setPacketWriter(wt);
		faultTolerateSPacketHandler.afterSend(w);
	}

	/**
	 * Test method for
	 * {@link org.apache.niolex.network.handler.FaultTolerateAdapter#handleRead(org.apache.niolex.network.PacketData, org.apache.niolex.network.IPacketWriter)}
	 * .
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testHandleRROk() {
		PacketData sc = new PacketData(Config.CODE_REGR_UUID, "AJFIUEALKD".getBytes());
		IPacketWriter wt = new TBasePacketWriter();
		faultTolerateSPacketHandler.handleRead(sc, wt);
		WriteEvent w = new WriteEvent();
		PacketData sc2 = new PacketData(3, "AJ231FIU3212312EALKD".getBytes());
		w.setPacketData(sc2);
		w.setPacketWriter(wt);
		faultTolerateSPacketHandler.afterSend(w);
		faultTolerateSPacketHandler.handleClose(wt);

		// after error.
		IPacketWriter wt2 = mock(IPacketWriter.class);
		// regi again
		faultTolerateSPacketHandler.handleRead(sc, wt2);
		verify(wt2).attachData(Config.ATTACH_KEY_FAULTTO_UUID, "AJFIUEALKD");

		TBasePacketWriter wt3 = spy(new TBasePacketWriter());
		faultTolerateSPacketHandler.handleRead(sc, wt3);
		verify(wt3).attachData(Config.ATTACH_KEY_FAULTTO_UUID, "AJFIUEALKD");

		ArgumentCaptor<Object> argument = ArgumentCaptor.forClass(Object.class);
		verify(wt3, times(1)).replaceQueue((ConcurrentLinkedQueue<PacketData>) argument.capture());
		ConcurrentLinkedQueue<PacketData> aa = (ConcurrentLinkedQueue<PacketData>) argument.getValue();
		assertEquals(sc2, aa.peek());
	}
}
