/**
 * SingleInvokerTest.java
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

import static org.mockito.Mockito.mock;

import org.apache.niolex.network.IClient;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.rpc.SingleInvoker;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-13
 */
public class SingleInvokerTest {

	/**
	 * Test method for {@link org.apache.niolex.network.rpc.SingleInvoker#handleRead(org.apache.niolex.network.PacketData, org.apache.niolex.network.IPacketWriter)}.
	 */
	@Test
	public void testHandleRead() {
		SingleInvoker ss = new SingleInvoker();
		ss.handleRead(null, null);
	}

	/**
	 * Test method for {@link org.apache.niolex.network.rpc.SingleInvoker#handleClose(org.apache.niolex.network.IPacketWriter)}.
	 */
	@Test
	public void testHandleClose() {
		SingleInvoker ss = new SingleInvoker();
		ss.handleClose(null);
	}

	/**
	 * Test method for {@link org.apache.niolex.network.rpc.SingleInvoker#invoke(org.apache.niolex.network.PacketData, org.apache.niolex.network.IClient)}.
	 */
	@Test
	public void testInvoke() {
		SingleInvoker ss = new SingleInvoker();
		final PacketData rc = new PacketData(56, new byte[76]);
		final IClient client = mock(IClient.class);
		ss.invoke(rc, client);
	}

}
