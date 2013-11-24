/**
 * RpcPacketHandlerTest.java
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
package org.apache.niolex.network.rpc;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.apache.niolex.network.CoreRunner;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.demo.json.RpcService;
import org.apache.niolex.network.demo.json.RpcServiceImpl;
import org.apache.niolex.network.rpc.conv.JsonConverter;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-6-4
 */
public class RpcPacketHandlerTest {

	/**
	 * Test method for
	 * {@link org.apache.niolex.network.rpc.RpcPacketHandler#handlePacket(org.apache.niolex.network.PacketData, org.apache.niolex.network.IPacketWriter)}
	 * .
	 */
	@Test
	public void testHandlePacket() {
		RpcPacketHandler rr = new RpcPacketHandler(3);
		rr.setConverter(new JsonConverter());
		IPacketWriter tt = mock(IPacketWriter.class);
		rr.handlePacket(new PacketData(5, new byte[5]), tt);
		verify(tt).handleWrite(any(PacketData.class));
	}

	@Test
	public void testHandleHB() {
		RpcPacketHandler rr = new RpcPacketHandler();
		IPacketWriter tt = mock(IPacketWriter.class);
		rr.handlePacket(PacketData.getHeartBeatPacket(), tt);
		verify(tt, never()).handleWrite(any(PacketData.class));
	}

	/**
	 * Test method for
	 * {@link org.apache.niolex.network.rpc.RpcPacketHandler#handleClose(org.apache.niolex.network.IPacketWriter)}.
	 */
	@Test
	public void setConfigs() {
		RpcPacketHandler rr = new RpcPacketHandler(3, new JsonConverter());
		ConfigItem[] confs = new ConfigItem[2];
		ConfigItem c = new ConfigItem();
		c.setInterface(RpcService.class);
		c.setTarget(new RpcServiceImpl());
		confs[0] = c;
		confs[1] = c;
		rr.setRpcConfigs(confs);
	}

	/**
	 * Test method for
	 * {@link org.apache.niolex.network.rpc.RpcPacketHandler#handleClose(org.apache.niolex.network.IPacketWriter)}.
	 */
	@Test
	public void testHandleClose() {
		RpcPacketHandler rr = new RpcPacketHandler(1);
		rr.handleClose(null);
	}

	@Test
	public void testHandleNe() {
		RpcPacketHandler rr = new RpcPacketHandler(2);
		rr.setConverter(new JsonConverter());
		IPacketWriter wt = mock(IPacketWriter.class);
		PacketData p = new PacketData(9, new byte[9]);
		rr.handlePacket(p, wt);
		assertEquals(0, rr.getQueueSize());
	}

	@Test
	public void testHandleNeg() throws Exception {
		RpcPacketHandler rr = new RpcPacketHandler(3);
		rr.setConverter(new JsonConverter());
		ConfigItem[] confs = new ConfigItem[1];
		ConfigItem c = new ConfigItem();
		c.setInterface(RpcService.class);
		c.setTarget(new RpcServiceImpl());
		confs[0] = c;
		rr.setRpcConfigs(confs);
		PacketData p = new PacketData(14, new byte[9]);
		p.setVersion((byte) 79);
		p.setReserved((byte) 99);
		IPacketWriter wt = mock(IPacketWriter.class);
		rr.handlePacket(p, wt);
		Thread.sleep(3 * CoreRunner.CO_SLEEP);
		ArgumentCaptor<PacketData> au = ArgumentCaptor.forClass(PacketData.class);
		verify(wt).handleWrite(au.capture());
		assertEquals(14, au.getValue().getCode());
		assertEquals(79, au.getValue().getVersion());
		assertEquals(100, au.getValue().getReserved());
	}

}
