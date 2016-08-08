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
package org.apache.niolex.network.rpc.cli;

import static org.mockito.Mockito.mock;

import org.apache.niolex.commons.reflect.FieldUtil;
import org.apache.niolex.network.IClient;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.client.SocketClient;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-6-13
 */
public class SingleInvokerTest {

	@Test
	public void testHandlePacket() {
        SingleInvoker ss = new SingleInvoker(null);
        ss.setRpcHandleTimeout(1000);
		ss.handlePacket(null, null);
	}

	@Test
    public void testSendPacket() {
        SingleInvoker ss = new SingleInvoker(null);
        final IClient client = mock(SocketClient.class);
        FieldUtil.setValue(ss, "sc", client);
        ss.sendPacket(null);
	}

	@Test
	public void testInvoke() {
        SingleInvoker ss = new SingleInvoker(null);
		final PacketData rc = new PacketData(56, new byte[76]);
        final IClient client = mock(SocketClient.class);
        FieldUtil.setValue(ss, "sc", client);
        ss.invoke(rc);
	}

}
