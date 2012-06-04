/**
 * PrintPacketHandlerTest.java
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
package org.apache.niolex.network.demo;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;

import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;
import org.junit.Test;


public class PrintPacketHandlerTest {

	@Test
	public void testHandleRead() {
		PacketData sc = mock(PacketData.class);
		when(sc.getData()).thenReturn("NullPointerException".getBytes());
		when(sc.getCode()).thenReturn((short)3, (short)3, (short)4);
		IPacketWriter ip = mock(IPacketWriter.class);
		PrintPacketHandler p = new PrintPacketHandler();
		p.handleError(ip);
		p.handleRead(sc, ip);
		LastTalkFactory l = new LastTalkFactory();
		IPacketHandler pp = l.createHandler(ip);
		pp.handleError(ip);
		pp.handleRead(sc, ip);
		verify(sc).getCode();
	}

	@SuppressWarnings("unused")
	@Test
	public void testCoverMain() throws Exception {
		DemoServer a = new DemoServer();
		DemoClient b = new DemoClient();
		DemoServer.main(null);
	    Thread.sleep(10);
	    String cons = "4\nNice to meet you!\n4\nNice to meet you!\n-1\n";
	    DemoClient.setIn(new ByteArrayInputStream(cons.getBytes()));
	    DemoClient.main(null);
	    DemoServer.stop();
	}
}
