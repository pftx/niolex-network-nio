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
package org.apache.niolex.network.handler;

import static org.mockito.Mockito.*;

import org.apache.niolex.network.Config;
import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketClient;
import org.apache.niolex.network.PacketData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-5-31
 */
public class FaultTolerateSPacketHandlerTest {
	@Mock
	private IHandlerFactory factory;
	@Mock
	private IPacketHandler h;
	private FaultTolerateSPacketHandler faultTolerateSPacketHandler;

	@Before
	public void createFaultTolerateSPacketHandler() throws Exception {
		faultTolerateSPacketHandler = new FaultTolerateSPacketHandler();
		faultTolerateSPacketHandler.setFactory(factory);
		when(factory.createHandler(any(IPacketWriter.class))).thenReturn(h);
	}

	/**
	 * Test method for {@link org.apache.niolex.network.handler.FaultTolerateSPacketHandler#handleError(org.apache.niolex.network.IPacketWriter)}.
	 */
	@Test
	public void testHandleErrorSimple() {
		IPacketWriter wt = spy(new PacketClient());
		faultTolerateSPacketHandler.handleError(wt);
	}

	/**
	 * Test method for {@link org.apache.niolex.network.handler.FaultTolerateSPacketHandler#handleError(org.apache.niolex.network.IPacketWriter)}.
	 */
	@Test
	public void testHandleError() {
		PacketData sc = new PacketData(Config.CODE_SESSN_REGR, "AJFIUEALKD".getBytes());
		IPacketWriter wt0 = spy(new PacketClient());
		doReturn("Mock").when(wt0).getRemoteName();
		PacketData sc2 = new PacketData(3, "AJ231FIUEALKD".getBytes());
		faultTolerateSPacketHandler.handleRead(sc, wt0);
		faultTolerateSPacketHandler.handleRead(sc2, wt0);
		wt0.handleWrite(sc2);
		verify(wt0).attachData(Config.ATTACH_KEY_SESS_SESSID, "AJFIUEALKD");
		faultTolerateSPacketHandler.handleError(wt0);
		// ERROR
		IPacketWriter wt = spy(new PacketClient());
		faultTolerateSPacketHandler.handleRead(sc, wt);
		faultTolerateSPacketHandler.handleError(wt);
		IPacketWriter wt2 = spy(new PacketClient());
		faultTolerateSPacketHandler.handleRead(sc, wt2);
		verify(h, times(1)).handleRead(sc2, wt0);
		verify(wt0, times(1)).handleWrite(sc2);
		verify(wt, times(1)).handleWrite(sc2);
		verify(wt2, times(1)).handleWrite(sc2);
	}

	/**
	 * Test method for {@link org.apache.niolex.network.handler.FaultTolerateSPacketHandler#handleRead(org.apache.niolex.network.PacketData, org.apache.niolex.network.IPacketWriter)}.
	 */
	@Test
	public void testHandleReadNotRegi() {
		PacketData sc = mock(PacketData.class);
		IPacketWriter wt = mock(IPacketWriter.class);
		faultTolerateSPacketHandler.handleRead(sc , wt);
	}

	/**
	 * Test method for {@link org.apache.niolex.network.handler.FaultTolerateSPacketHandler#handleRead(org.apache.niolex.network.PacketData, org.apache.niolex.network.IPacketWriter)}.
	 */
	@Test
	public void testHandleReadRegi() {
		PacketData sc = new PacketData(Config.CODE_SESSN_REGR, "AJFIUEALKD".getBytes());
		IPacketWriter wt = mock(IPacketWriter.class);
		faultTolerateSPacketHandler.handleRead(sc , wt);
		verify(wt).attachData(Config.ATTACH_KEY_SESS_SESSID, "AJFIUEALKD");
	}

}
