/**
 * SavePacketHandlerTest.java
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
package org.apache.niolex.network.example;

import static org.mockito.Mockito.*;

import java.util.List;

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
 * @since 2012-6-1
 */
public class SavePacketHandlerTest {

	@Mock
	private List<PacketData> list;
	private SavePacketHandler savePacketHandler;

	@Before
	public void createSavePacketHandler() throws Exception {
		savePacketHandler = new SavePacketHandler(list);
	}

	/**
    	 * Test method for {@link org.apache.niolex.network.example.SavePacketHandler#handlePacket(org.apache.niolex.network.PacketData, org.apache.niolex.network.IPacketWriter)}.
    	 */
    	@Test
    	public void testHandlePacket() {
    		PacketData sc = new PacketData(3, new byte[0]);
    		savePacketHandler.handlePacket(sc, null);
    		verify(list).add(sc);
    	}

	/**
		 * Test method for {@link org.apache.niolex.network.example.SavePacketHandler#handleClose(org.apache.niolex.network.IPacketWriter)}.
		 */
		@Test
		public void testHandleClose() {
			savePacketHandler.handleClose(null);
		}

}
