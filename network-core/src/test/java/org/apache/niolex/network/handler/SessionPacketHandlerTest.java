/**
 * PacketDataTest.java
 *
 * Copyright 2011 Niolex, Inc.
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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class SessionPacketHandlerTest {
	@Mock
	private IHandlerFactory factory;
	private SessionPacketHandler sessionPacketHandler;
	@Mock
	IPacketHandler pHandler1;

	@Before
	public void createSessionPacketHandler() throws Exception {
		sessionPacketHandler = new SessionPacketHandler(factory);

		when(factory.createHandler(any(IPacketWriter.class))).thenReturn(pHandler1);
	}

	@Test
	public void testHandleError() {
		PacketData sc = mock(PacketData.class);
		when(sc.getCode()).thenReturn((short)3, (short)3, (short)4);
		IPacketWriter ip = mock(IPacketWriter.class);
		IPacketWriter wt = mock(IPacketWriter.class);
		IPacketWriter qt = mock(IPacketWriter.class);
		sessionPacketHandler.handleRead(sc, wt);
		sessionPacketHandler.handleRead(sc, ip);
		sessionPacketHandler.handleRead(sc, wt);
		sessionPacketHandler.handleError(wt);
		sessionPacketHandler.handleError(qt);
		verify(pHandler1, times(1)).handleError(wt);
		verify(pHandler1, never()).handleError(qt);
		verify(pHandler1, never()).handleError(ip);
	}

	@Test
	public void testHandleRead() {
		PacketData sc = mock(PacketData.class);
		when(sc.getCode()).thenReturn((short)3, (short)3, (short)4);
		IPacketWriter ip = mock(IPacketWriter.class);
		IPacketWriter wt = mock(IPacketWriter.class);
		IPacketWriter qt = mock(IPacketWriter.class);
		sessionPacketHandler.handleRead(sc, wt);
		sessionPacketHandler.handleRead(sc, qt);
		sessionPacketHandler.handleRead(sc, ip);
		sessionPacketHandler.handleRead(sc, wt);
		sessionPacketHandler.handleRead(sc, qt);
		sessionPacketHandler.handleRead(sc, ip);
		sessionPacketHandler.handleRead(sc, wt);
		sessionPacketHandler.handleRead(sc, qt);
		sessionPacketHandler.handleRead(sc, ip);
		PacketData qc = mock(PacketData.class);
		sessionPacketHandler.handleRead(qc, qt);
		sessionPacketHandler.handleRead(qc, ip);
		verify(factory, times(3)).createHandler(any(IPacketWriter.class));
		verify(pHandler1, times(9)).handleRead(eq(sc), any(IPacketWriter.class));
		verify(pHandler1, times(2)).handleRead(eq(qc), any(IPacketWriter.class));
		verify(pHandler1, times(4)).handleRead(any(PacketData.class), eq(ip));
		verify(pHandler1, times(3)).handleRead(any(PacketData.class), eq(wt));
	}

	@Test
	public void testGetFactory() {
	    sessionPacketHandler = new SessionPacketHandler();
		sessionPacketHandler.setFactory(factory);
		assertEquals(factory, sessionPacketHandler.getFactory());
	}

}
