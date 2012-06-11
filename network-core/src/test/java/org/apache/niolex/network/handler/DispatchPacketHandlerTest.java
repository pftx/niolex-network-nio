/**
 * DispatchPacketHandlerTest.java
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
import static org.mockito.Mockito.mock;
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
public class DispatchPacketHandlerTest {

	DispatchPacketHandler handler;
	@Mock
	IPacketHandler pHandler1;

	@Mock
	IPacketHandler pHandler2;

	@Before
	public void setUp() {
		handler = new DispatchPacketHandler();
		handler.addHandler((short) 3, pHandler1);
		handler.addHandler((short) 5, pHandler2);
	}

	@Test
		public void testHandleClose() {
			PacketData sc = mock(PacketData.class);
			when(sc.getCode()).thenReturn((short)3, (short)3, (short)4);
			IPacketWriter ip = mock(IPacketWriter.class);
			handler.handleRead(sc , ip);
			handler.handleRead(sc , ip);
			handler.handleRead(sc , ip);
			handler.handleRead(sc , ip);
			handler.handleClose(ip);
			handler.handleClose(ip);
			handler.handleClose(ip);
			verify(pHandler1, times(3)).handleClose(ip);
			verify(pHandler2, times(3)).handleClose(ip);
			assertEquals(2, handler.getDispatchSize());
		}

	@Test
	public void testHandleRead() {
		PacketData sc = mock(PacketData.class);
		when(sc.getCode()).thenReturn((short)3, (short)3, (short)4, (short)4, (short)5);
		IPacketWriter ip = mock(IPacketWriter.class);
		handler.handleRead(sc , ip);
		handler.handleRead(sc , ip);
		handler.handleRead(sc , ip);
		handler.handleRead(sc , ip);
		verify(pHandler1, times(2)).handleRead(sc, ip);
		verify(pHandler2, times(1)).handleRead(sc, ip);
		assertEquals(2, handler.getDispatchSize());
	}

}
