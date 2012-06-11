/**
 * ClientHandlerTest.java
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
package org.apache.niolex.network.server;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import org.apache.niolex.network.CoreRunner;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-11
 */
public class ClientHandlerTest {

	/**
	 * Test method for {@link org.apache.niolex.network.server.ClientHandler#getHeartBeatInterval()}.
	 * @throws IOException
	 */
	@Test
	public final void testGetHeartBeatInterval() throws Exception {
		CoreRunner.createServer();
		ClientHandler c = new ClientHandler(null, null, SocketChannel.open(new InetSocketAddress("localhost", 8809)));
		c.setHeartBeatInterval(123321);
		assertEquals(123321, c.getHeartBeatInterval());
	}

}
