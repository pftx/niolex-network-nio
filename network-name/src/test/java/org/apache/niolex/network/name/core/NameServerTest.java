/**
 * NameServerTest.java
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
package org.apache.niolex.network.name.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.apache.niolex.network.Config;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.name.bean.AddressRegiBean;
import org.apache.niolex.network.name.bean.RecordStorage;
import org.apache.niolex.network.name.event.ConcurrentDispatcher;
import org.apache.niolex.network.name.event.IDispatcher;
import org.apache.niolex.network.packet.PacketTransformer;
import org.apache.niolex.network.server.BasePacketWriter;
import org.apache.niolex.network.server.NioServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-6-27
 */
public class NameServerTest {

	private static NioServer s = new NioServer();
	private static NameServer name;
	private static PacketTransformer transformer;

	@BeforeClass
	public static void startServer() {
		s.setPort(8181);
        name = new NameServer(s);
        RecordStorage st = new RecordStorage();
        name.setStorage(st);
        IDispatcher dd = new ConcurrentDispatcher();
        name.setDispatcher(dd);
        st.setDispatcher(dd);
        st.setDeleteTime(123);
        name.start();
        transformer = PacketTransformer.getInstance();
	}

	@AfterClass
	public static void stopServer() {
		name.stop();
	}

	/**
	 * Test method for {@link org.apache.niolex.network.name.core.NameServer#NameServer(org.apache.niolex.network.IServer)}.
	 */
	@Test
	public void testNameServer() {
		IPacketWriter wt = mock(IPacketWriter.class);
		// Step 1 publish.
		AddressRegiBean regi = new AddressRegiBean("network.name.core.NameServer", "localhost:8181");
		PacketData pb = transformer.getPacketData(Config.CODE_NAME_PUBLISH, regi);
		name.handleRead(pb, wt);

		// Step 2 subscribe.
		PacketData pd = transformer.getPacketData(Config.CODE_NAME_OBTAIN, "network.name.core.NameServer");
		name.handleRead(pd, wt);
		ArgumentCaptor<PacketData> cap = ArgumentCaptor.forClass(PacketData.class);
		verify(wt).handleWrite(cap.capture());
		List<String> list = transformer.getDataObject(cap.getValue());
		assertEquals(Config.CODE_NAME_DATA, cap.getValue().getCode());
		assertEquals(2, list.size());
		assertEquals("localhost:8181", list.get(0));
	}

	/**
	 * Test method for {@link org.apache.niolex.network.name.core.NameServer#start()}.
	 */
	@Test
	public void testStart() {
		IPacketWriter wt = mock(IPacketWriter.class);
		// Step 1 publish.
		PacketData pb = new PacketData(56);
		name.handleRead(pb, wt);
		ArgumentCaptor<PacketData> cap = ArgumentCaptor.forClass(PacketData.class);
		verify(wt).handleWrite(cap.capture());
		assertEquals(Config.CODE_NOT_RECOGNIZED, cap.getValue().getCode());
	}

	/**
	 * Test method for {@link org.apache.niolex.network.name.core.NameServer#handleClose(org.apache.niolex.network.IPacketWriter)}.
	 */
	@Test
	public void testHandleClose() {
		IPacketWriter wt = mock(IPacketWriter.class);
		name.handleClose(wt);
	}

	/**
	 * Test method for {@link org.apache.niolex.network.name.core.NameServer#setStorage(org.apache.niolex.network.name.bean.RecordStorage)}.
	 */
	@Test
	public void testSetStorage() {
		IPacketWriter wt = new BasePacketWriter() {

			@Override
			public String getRemoteName() {
				return "@link org.apache.niolex";
			}};

		// Step 1 publish.
		AddressRegiBean regi = new AddressRegiBean("network.name.core.NameServer", "localhost:8181");
		PacketData pb = transformer.getPacketData(Config.CODE_NAME_PUBLISH, regi);
		name.handleRead(pb, wt);
		regi = new AddressRegiBean("network.name.core.NameServer", "localhost:8182");
		pb = transformer.getPacketData(Config.CODE_NAME_PUBLISH, regi);
		name.handleRead(pb, wt);

		// Step 2 subscribe.
		PacketData pd = transformer.getPacketData(Config.CODE_NAME_OBTAIN, "network.name.core.NameServer");
		name.handleRead(pd, wt);
		assertFalse(wt.getAttached(Config.ATTACH_KEY_OBTAIN_ADDR) == null);
		assertFalse(wt.getAttached(Config.ATTACH_KEY_REGIST_ADDR) == null);
		name.handleClose(wt);
		assertTrue(wt.getAttached(Config.ATTACH_KEY_OBTAIN_ADDR) == null);
		assertTrue(wt.getAttached(Config.ATTACH_KEY_REGIST_ADDR) == null);
	}


}
