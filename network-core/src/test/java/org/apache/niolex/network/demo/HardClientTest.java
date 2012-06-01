/**
 * HardClientTest.java
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
package org.apache.niolex.network.demo;

import static org.junit.Assert.assertArrayEquals;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.niolex.commons.test.MockUtil;
import org.apache.niolex.network.CoreRunner;
import org.apache.niolex.network.PacketClient;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.example.SavePacketHandler;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-5-31
 */
public class HardClientTest {
	PacketClient c = new PacketClient(new InetSocketAddress("localhost", 8809));
	List<PacketData> list = new ArrayList<PacketData>();
	List<PacketData> list2 = new ArrayList<PacketData>();

	@BeforeClass
	public static void run() throws Exception {
		CoreRunner.createServer();
	}

	@AfterClass
	public static void down2() throws Exception {
		CoreRunner.shutdown();
	}

	@Before
	public void setup() throws Exception {
		c.setPacketHandler(new SavePacketHandler(list));
		c.connect();
	}

	@After
	public void down() throws Exception {
		c.stop();
	}

	@Test
	public void test() throws Exception {
		list.clear();
		list2.clear();
		for (int i = 0; i < 5000; ++i) {
			PacketData sc = new PacketData();
			sc.setCode((short) 2);
			sc.setVersion((byte) 1);
			byte[] data = MockUtil.randByteArray(60);
			sc.setLength(data.length);
			sc.setData(data);
			c.handleWrite(sc);
			list2.add(sc);
		}
		Thread.sleep(3000);
		for (int i = 0; i < 5000; ++i) {
			assertArrayEquals(list.get(i).getData(), list2.get(i).getData());
		}
	}

	public static void main(String args[]) throws Exception {
		HardClientTest hdt = new HardClientTest();
		hdt.setup();
		for (int i = 0; i < 1000; ++i) {
			hdt.test();
			System.out.println("Test iter.");
		}
		hdt.down();
	}
}
