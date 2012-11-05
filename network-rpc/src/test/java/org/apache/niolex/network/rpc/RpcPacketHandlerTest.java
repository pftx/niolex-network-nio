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

import java.lang.reflect.Type;

import org.apache.niolex.network.CoreRunner;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.demo.rpc.RpcService;
import org.apache.niolex.network.demo.rpc.RpcServiceImpl;
import org.apache.niolex.network.rpc.json.JsonRpcPacketHandler;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-4
 */
public class RpcPacketHandlerTest {

	/**
	 * Test method for
	 * {@link org.apache.niolex.network.rpc.RpcPacketHandler#handleRead(org.apache.niolex.network.PacketData, org.apache.niolex.network.IPacketWriter)}
	 * .
	 */
	@Test
	public void testHandleRead() {
		RpcPacketHandler rr = new JsonRpcPacketHandler();
		IPacketWriter tt = mock(IPacketWriter.class);
		rr.handleRead(new PacketData(5, new byte[5]), tt);
		verify(tt).handleWrite(any(PacketData.class));
	}

	/**
	 * Test method for
	 * {@link org.apache.niolex.network.rpc.RpcPacketHandler#handleClose(org.apache.niolex.network.IPacketWriter)}.
	 */
	@Test
	public void setConfigs() {
		RpcPacketHandler rr = new JsonRpcPacketHandler(20);
		RpcConfig[] confs = new RpcConfig[2];
		RpcConfig c = new RpcConfig();
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
			RpcPacketHandler rr = new JsonRpcPacketHandler();
			rr.handleClose(null);
		}

	@Test
	public void testHandleHB() {
		RpcPacketHandler rr = new JsonRpcPacketHandler();
		rr.handleRead(PacketData.getHeartBeatPacket(), null);
	}

	@Test
	public void testHandleNe() {
		RpcPacketHandler rr = new JsonRpcPacketHandler();
		PacketData p = new PacketData(9, new byte[9]);
		rr.handleRead(p, null);
	}

	@Test
	public void testHandleNeg() throws Exception {
		RpcPacketHandler rr = new RpcPacketHandler() {

			@Override
			protected Object[] prepareParams(byte[] data, Type[] generic) throws Exception {
				throw new Exception("Test");
			}

			@Override
			protected byte[] serializeReturn(Object ret) throws Exception {
				return new byte[9];
			}
		};
		RpcConfig[] confs = new RpcConfig[1];
		RpcConfig c = new RpcConfig();
		c.setInterface(RpcService.class);
		c.setTarget(new RpcServiceImpl());
		confs[0] = c;
		rr.setRpcConfigs(confs);
		PacketData p = new PacketData(14, new byte[9]);
		IPacketWriter wt = mock(IPacketWriter.class);
		rr.handleRead(p, wt);
		Thread.sleep(CoreRunner.CO_SLEEP);
		ArgumentCaptor<PacketData> au = ArgumentCaptor.forClass(PacketData.class);
		verify(wt).handleWrite(au.capture());
		assertEquals(14, au.getValue().getCode());
		assertEquals(9, au.getValue().getLength());
	}

}
