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

import static org.mockito.Mockito.*;

import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;
import org.junit.Test;


public class PrintPacketHandlerTest {

	@Test
	public void testHandlePacket() {
		PacketData sc = mock(PacketData.class);
		when(sc.getData()).thenReturn(")(DF*&@#KLJER".getBytes());
		when(sc.getCode()).thenReturn((short)3, (short)3, (short)4);
		IPacketWriter ip = mock(IPacketWriter.class);

		PrintPacketHandler p = new PrintPacketHandler();
		p.handleClose(ip);
		p.handlePacket(sc, ip);
		verify(sc).getCode();
	}

	@Test
    public void testLastTalkFactory() {
	    PacketData sc = mock(PacketData.class);
        when(sc.getData()).thenReturn(")(DF*&@#KLJER".getBytes());
        when(sc.getCode()).thenReturn((short)3, (short)3, (short)4);

        IPacketWriter ip = mock(IPacketWriter.class);
	    LastTalkFactory l = new LastTalkFactory();
	    IPacketHandler pp = l.createHandler(ip);

	    pp.handleClose(ip);
	    pp.handlePacket(sc, ip);
	    verify(ip).handleWrite(sc);
	    pp.handlePacket(sc, ip);
	    verify(ip, times(2)).handleWrite(sc);
	}

	@Test
	public void testDemoMain() throws Exception {
		new DemoServer();
		new DemoClient();
		DemoServer.main(new String[]{"-x", "1"});
	    Thread.sleep(10);
	    DemoServer.stop();
	}
}
