/**
 * NameClientTest.java
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

import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.apache.niolex.network.Config;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.name.bean.AddressRecord;
import org.apache.niolex.network.packet.PacketTransformer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-6-29
 */
public class NameClientTest {

	private NameClient nameClient;
	private static PacketTransformer transformer = PacketTransformer.getInstance();

	@BeforeClass
	public static void startServer() {
		NameServerTest.startServer();
	}

	@AfterClass
	public static void stopServer() {
		NameServerTest.stopServer();
	}


	@Before
	public void createNameClient() throws Exception {
		nameClient = new NameClient("localhost:8181");
		nameClient.setConnectRetryTimes(123);
		nameClient.setSleepBetweenRetryTime(100);
	}

	@After
	public void stopNameClient() {
		nameClient.stop();
	}

	/**
	 * Test method for {@link org.apache.niolex.network.name.core.NameClient#handleRead(org.apache.niolex.network.PacketData, org.apache.niolex.network.IPacketWriter)}.
	 */
	@Test
	public void testHandleRead() {
		IPacketWriter wt = mock(IPacketWriter.class);
		// Step 1 publish.
		AddressRecord regi = new AddressRecord("network.name.core.NameServer", "localhost:8181");
		PacketData pb = transformer.getPacketData(Config.CODE_NAME_DIFF, regi);
		nameClient.handleRead(pb, wt);

		// Step 2 subscribe.
		List<String> list = new ArrayList<String>();
		list.add("localhost:8181");
		PacketData pd = transformer.getPacketData(Config.CODE_NAME_DATA, list);
		nameClient.handleRead(pd, wt);

		// Step 3 invalid.
		pb = new PacketData(56);
		nameClient.handleRead(pb, wt);
	}


	/**
	 * Test method for {@link org.apache.niolex.network.name.core.NameClient#handleClose(org.apache.niolex.network.IPacketWriter)}.
	 */
	@Test
	public void testHandleClose() {
		nameClient.handleClose(null);
	}

	/**
	 * Test method for {@link org.apache.niolex.network.name.core.NameClient#handleClose(org.apache.niolex.network.IPacketWriter)}.
	 */
	@Test
	public void testHandleClose2() {
		nameClient.setConnectRetryTimes(0);
		nameClient.handleClose(null);
	}


}
