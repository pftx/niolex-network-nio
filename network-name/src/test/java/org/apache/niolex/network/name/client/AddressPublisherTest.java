/**
 * AddressPublisherTest.java
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
package org.apache.niolex.network.name.client;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.apache.niolex.network.name.demo.DemoAddressEventListener;
import org.apache.niolex.network.name.demo.NameServerDemo;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-6-27
 */
public class AddressPublisherTest {

	@BeforeClass
	public static void startServer() throws Exception {
	    NameServerDemo.main(null);
	}

	@AfterClass
	public static void stopServer() {
	    NameServerDemo.stop();
	}

	/**
	 * Test method for {@link org.apache.niolex.network.name.client.AddressPublisher#AddressPublisher(java.lang.String)}.
	 * @throws IOException
	 */
	@Test
	public void testAddressPublisher() throws IOException {
		AddressPublisher c = new AddressPublisher("localhost:8181");
		c.pushlishService("network/name", "Not yet implemented");
		assertEquals(1, c.size());
		c.stop();
	}

	/**
	 * Test method for {@link org.apache.niolex.network.name.client.AddressPublisher#pushlishService(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testPushlishService() throws Exception {
		AddressPublisher pub = new AddressPublisher("localhost:8181");
		pub.pushlishService("network/name", "needs heart beat.");
		pub.setSleepBetweenRetryTime(20);
		assertEquals(1, pub.size());
		stopServer();
		startServer();
		Thread.sleep(500);
		AddressSubscriber c = new AddressSubscriber("localhost:8181");
    	AddressEventListener listn = new DemoAddressEventListener();
		List<String> ls = c.getServiceAddrList("network/name", listn);
		System.out.println("Address list: " + ls);
		assertEquals(1, ls.size());
		assertEquals("needs heart beat.", ls.get(0));
		pub.stop();
		c.stop();
	}

}
