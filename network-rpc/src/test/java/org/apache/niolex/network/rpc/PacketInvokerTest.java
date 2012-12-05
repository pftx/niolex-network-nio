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

import java.util.concurrent.CountDownLatch;

import org.apache.niolex.network.IClient;
import org.apache.niolex.network.PacketData;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-6-13
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

	@Test
	public void testInvoke() throws Exception {
		final PacketInvoker in = new PacketInvoker();
		final PacketData rc = new PacketData(56, new byte[76]);
		rc.setVersion((byte) 77);
		rc.setReserved((byte) 127);
		final IClient client = mock(IClient.class);
		final CountDownLatch latch = new CountDownLatch(1);
		final CountDownLatch latch2 = new CountDownLatch(1);
		final PacketData qq = rc.makeCopy();
		rc.setReserved((byte)-128);
		Thread r = new Thread() {
			public void run() {
				latch2.countDown();
				PacketData sc = in.invoke(rc, client);
				assertEquals(sc, qq);
				latch.countDown();
			}
		};
		r.start();
		latch2.await();
		Thread.sleep(50);
		in.handleRead(qq, client);
		latch.await();
	}

	/**
	 * Test method for {@link org.apache.niolex.network.rpc.PacketInvoker#handleClose(org.apache.niolex.network.IPacketWriter)}.
	 * @throws InterruptedException
	 */
	@Test
	public void testInvokeAndHandleClose() throws InterruptedException {
		final PacketInvoker in = new PacketInvoker();
		final PacketData rc = new PacketData(56, new byte[76]);
		final IClient client = mock(IClient.class);
		final CountDownLatch latch = new CountDownLatch(1);
		Thread r = new Thread() {
			public void run() {
				latch.countDown();
				in.invoke(rc, client);
			}
		};
		r.start();
		latch.await();
		assertTrue(!r.isInterrupted());
		r.interrupt();
		Thread.sleep(10);
		in.handleClose(null);
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
