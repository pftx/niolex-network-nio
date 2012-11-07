/**
 * FastCoreTest.java
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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import org.apache.niolex.network.CoreRunner;
import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.PacketData;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-11-7
 */
public class FastCoreTest {

	private SocketChannel client;

	@Mock
	private IPacketHandler packetHandler;

	private Selector selector;

	@Mock
	private SelectorHolder selectorH;

	private FastCore fastCore;

	@BeforeClass
	public static void start() throws Exception {
		CoreRunner.createServer();
	}

	@AfterClass
	public static void stop() throws Exception {
		CoreRunner.shutdown();
	}

	@Before
	public void init() throws IOException {
		client = SocketChannel.open(new InetSocketAddress("localhost", CoreRunner.PORT));
		client.configureBlocking(false);
		selector = Selector.open();
		when(selectorH.getSelector()).thenReturn(selector);
		fastCore = spy(new FastCore(packetHandler, selectorH, client));
	}

	@After
	public void destroy() throws IOException {
		client.close();
	}

	@Test
	public void testCannotStart() {
		MultiNioServer nioServer  = new MultiNioServer(3);
		nioServer.setPort(-1);
		nioServer.setAcceptTimeOut(10);
		nioServer.start();
		nioServer.stop();
		nioServer.stop();
	}

	/**
	 * Test method for {@link org.apache.niolex.network.server.FastCore#handleWrite(org.apache.niolex.network.PacketData)}.
	 */
	@Test
	public void testHandleWritePacketData() {
		PacketData sc = new PacketData(5);
		fastCore.handleWrite(sc);
		fastCore.handleWrite(sc);
		verify(selectorH).changeInterestOps(any(SelectionKey.class));
	}

	/**
	 * Test method for {@link org.apache.niolex.network.server.FastCore#getRemoteName()}.
	 */
	@Test
	public void testGetRemoteName() {
		System.out.println(fastCore.getRemoteName());
	}

	/**
	 * Test method for {@link org.apache.niolex.network.server.FastCore#handleRead()}.
	 * @throws IOException
	 */
	@Test
	public void testHandleRead() throws IOException {
		client.close();
		fastCore.handleRead();
		fastCore.handleRead();
		verify(packetHandler, times(2)).handleClose(fastCore);
	}

	/**
	 * Test method for {@link org.apache.niolex.network.server.FastCore#handleWrite()}.
	 * @throws IOException
	 */
	@Test
	public void testHandleWrite() throws IOException {
		client.close();
		fastCore.handleWrite();
		verify(packetHandler, times(1)).handleClose(fastCore);
	}

}
