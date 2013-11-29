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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.apache.niolex.network.Config;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.IServer;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.name.bean.AddressRegiBean;
import org.apache.niolex.network.serialize.PacketTransformer;
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
public class NameServerTest extends Context {

    private static PacketTransformer transformer = getTransformer();
	private static NioServer s = new NioServer();
	private static NameServer name;

	@BeforeClass
	public static void startServer() {
		s.setPort(8181);
        name = new NameServer(s);
        name.setDeleteTime(123);
        assertTrue(name.start());
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
		// Step 1. publish.
		AddressRegiBean regi = new AddressRegiBean("network.name.core.NameServer", "localhost:8181");
		PacketData pb = transformer.getPacketData(Config.CODE_NAME_PUBLISH, regi);
		name.handlePacket(pb, wt);

		// Step 2. subscribe.
		PacketData pd = transformer.getPacketData(Config.CODE_NAME_OBTAIN, "network.name.core.NameServer");
		name.handlePacket(pd, wt);
		ArgumentCaptor<PacketData> cap = ArgumentCaptor.forClass(PacketData.class);
		verify(wt).handleWrite(cap.capture());
		List<String> list = transformer.getDataObject(cap.getValue());
		assertEquals(Config.CODE_NAME_DATA, cap.getValue().getCode());
		assertEquals(2, list.size());
		assertEquals("localhost:8181", list.get(0));
		assertEquals("network.name.core.NameServer", list.get(1));
		reset(wt);

		// Step 3. publish another.
		regi = new AddressRegiBean("network.name.core.NameServer", "localhost:8182");
        pb = transformer.getPacketData(Config.CODE_NAME_PUBLISH, regi);
        name.handlePacket(pb, wt);
        verify(wt).handleWrite(cap.capture());
        assertEquals(Config.CODE_NAME_DIFF, cap.getValue().getCode());

        // Step 4. invalid code.
        name.handlePacket(new PacketData(68), wt);
	}

	/**
     * Test method for {@link org.apache.niolex.network.name.core.NameServer#start()}.
     */
    @Test
    public void testStartFail() {
        IServer server = mock(IServer.class);
        NameServer ns = new NameServer(server);
        when(server.start()).thenReturn(false);
        assertFalse(ns.start());
    }

	/**
	 * Test method for {@link org.apache.niolex.network.name.core.NameServer#start()}.
	 */
	@Test
	public void testNotRecognize() {
		IPacketWriter wt = mock(IPacketWriter.class);
		PacketData pb = new PacketData(56);
		name.handlePacket(pb, wt);
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
		verify(wt, times(2)).getAttached(anyString());
		verify(wt, times(0)).attachData(anyString(), anyObject());
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
		name.handlePacket(pb, wt);
		regi = new AddressRegiBean("network.name.core.NameServer", "localhost:8182");
		pb = transformer.getPacketData(Config.CODE_NAME_PUBLISH, regi);
		name.handlePacket(pb, wt);

		// Step 2 subscribe.
		PacketData pd = transformer.getPacketData(Config.CODE_NAME_OBTAIN, "network.name.core.NameServer");
		name.handlePacket(pd, wt);
		assertFalse(wt.getAttached(Config.ATTACH_KEY_OBTAIN_ADDR) == null);
		assertFalse(wt.getAttached(Config.ATTACH_KEY_REGIST_ADDR) == null);
		name.handleClose(wt);
		assertTrue(wt.getAttached(Config.ATTACH_KEY_OBTAIN_ADDR) == null);
		assertTrue(wt.getAttached(Config.ATTACH_KEY_REGIST_ADDR) == null);
	}


}
