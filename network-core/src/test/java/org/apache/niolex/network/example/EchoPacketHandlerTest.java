/**
 * EchoPacketHandlerTest.java
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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;
import org.junit.Before;
import org.junit.Test;


public class EchoPacketHandlerTest {
	EchoPacketHandler handler;

	@Before
	public void setUp() throws Exception {
		handler = new EchoPacketHandler();
	}

	@Test
	public void testHandleError() {
		IPacketWriter ip = mock(IPacketWriter.class);
		handler.handleError(ip);
		handler.handleError(ip);
		verify(ip, times(0)).handleWrite(any(PacketData.class));
		verify(ip, times(2)).getRemoteName();
	}

	@Test
	public void testHandleRead() {
		PacketData sc = mock(PacketData.class);
		when(sc.getCode()).thenReturn((short)3, (short)3, (short)4);
		IPacketWriter ip = mock(IPacketWriter.class);
		handler.handleRead(sc , ip);
		handler.handleRead(sc , ip);
		handler.handleRead(sc , ip);
		verify(ip, times(3)).handleWrite(sc);
		verify(ip, times(3)).getRemoteName();
	}

}
