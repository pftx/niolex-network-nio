/**
 * PacketInvokerTest.java
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.apache.niolex.network.IClient;
import org.apache.niolex.network.PacketData;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-13
 */
public class PacketInvokerTest {

	/**
	 * Test method for {@link org.apache.niolex.network.rpc.PacketInvoker#handleClose(org.apache.niolex.network.IPacketWriter)}.
	 */
	@Test
	public void testHandleRead() {
		PacketInvoker in = new PacketInvoker();
		in.handleRead(PacketData.getHeartBeatPacket(), null);
	}

	/**
	 * Test method for {@link org.apache.niolex.network.rpc.PacketInvoker#handleClose(org.apache.niolex.network.IPacketWriter)}.
	 * @throws InterruptedException
	 */
	@Test
	public void testHandleClose() throws InterruptedException {
		final PacketInvoker in = new PacketInvoker();
		final PacketData rc = new PacketData(56, new byte[76]);
		final IClient client = mock(IClient.class);
		Thread r = new Thread() {
			public void run() {
				in.invoke(rc, client);
			}
		};
		r.start();
		assertTrue(!r.isInterrupted());
		Thread.sleep(10);
		in.handleClose(null);
		assertTrue(r.isInterrupted());
	}

	/**
	 * Test method for {@link org.apache.niolex.network.rpc.PacketInvoker#getRpcHandleTimeout()}.
	 */
	@Test
	public void testGetRpcHandleTimeout() {
		PacketInvoker in = new PacketInvoker();
		in.setRpcHandleTimeout(412312);
		assertEquals(412312, in.getRpcHandleTimeout());
	}

}
