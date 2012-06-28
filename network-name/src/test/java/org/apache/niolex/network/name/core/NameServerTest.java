/**
 * NameServerTest.java
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
package org.apache.niolex.network.name.core;

import static org.junit.Assert.fail;

import org.apache.niolex.network.name.bean.RecordStorage;
import org.apache.niolex.network.name.event.ConcurrentDispatcher;
import org.apache.niolex.network.name.event.IDispatcher;
import org.apache.niolex.network.server.NioServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-27
 */
public class NameServerTest {

	private static NioServer s = new NioServer();
	private static NameServer name;

	@BeforeClass
	public static void startServer() {
		s.setPort(8181);
        name = new NameServer(s);
        name.setStorage(new RecordStorage());
        IDispatcher dd = new ConcurrentDispatcher();
        name.setDispatcher(dd);
        name.start();
	}

	@AfterClass
	public static void stopServer() {
		name.stop();
	}

	/**
	 * Test method for {@link org.apache.niolex.network.name.core.NameServer#NameServer(org.apache.niolex.network.IServer)}.
	 */
	@Test
	public void testNameServer() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.apache.niolex.network.name.core.NameServer#start()}.
	 */
	@Test
	public void testStart() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.apache.niolex.network.name.core.NameServer#stop()}.
	 */
	@Test
	public void testStop() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.apache.niolex.network.name.core.NameServer#handleRead(org.apache.niolex.network.PacketData, org.apache.niolex.network.IPacketWriter)}.
	 */
	@Test
	public void testHandleRead() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.apache.niolex.network.name.core.NameServer#handleClose(org.apache.niolex.network.IPacketWriter)}.
	 */
	@Test
	public void testHandleClose() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.apache.niolex.network.name.core.NameServer#setStorage(org.apache.niolex.network.name.bean.RecordStorage)}.
	 */
	@Test
	public void testSetStorage() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.apache.niolex.network.name.core.NameServer#setDispatcher(org.apache.niolex.network.name.event.IDispatcher)}.
	 */
	@Test
	public void testSetDispatcher() {
		fail("Not yet implemented");
	}

}
