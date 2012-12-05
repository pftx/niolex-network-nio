/**
 * AddressSubscriberTest.java
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

import static org.junit.Assert.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.niolex.network.name.bean.AddressRecord;
import org.apache.niolex.network.name.bean.AddressRecord.Status;
import org.apache.niolex.network.name.core.NameServerTest;
import org.apache.niolex.network.name.demo.DemoAddressEventListener;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-6-27
 */
public class AddressSubscriberTest {

	@BeforeClass
	public static void startServer() {
		NameServerTest.startServer();
	}

	@AfterClass
	public static void stopServer() {
		NameServerTest.stopServer();
	}


	/**
	 * Test method for {@link org.apache.niolex.network.name.client.AddressSubscriber#handleDiff(org.apache.niolex.network.name.bean.AddressRecord)}.
	 * @throws IOException
	 */
	@Test
	public void testHandleDiff() throws IOException {
		AddressSubscriber sub = new AddressSubscriber("localhost:8181");
    	AddressEventListener listn = spy(new DemoAddressEventListener());
		List<String> ls = sub.getServiceAddrList("network.name.core.NameServer", listn);
		System.out.println("Address list: " + ls);
		assertEquals(0, ls.size());
		AddressRecord bean;

		bean = new AddressRecord("network.name.core.NameServer", "localhost:8181");
		sub.handleDiff(bean);
		verify(listn).addressAdd("localhost:8181");
		bean.setStatus(Status.DEL);
		sub.handleDiff(bean);
		verify(listn).addressRemove("localhost:8181");
		bean.setStatus(Status.DISCONNECTED);
		sub.handleDiff(bean);

		bean.setAddressKey("gujiguji");
		bean.setStatus(Status.DEL);
		sub.handleDiff(bean);
		verify(listn).addressAdd("localhost:8181");
		verify(listn).addressRemove("localhost:8181");
	}

	/**
	 * Test method for {@link org.apache.niolex.network.name.client.AddressSubscriber#fireEvent(org.apache.niolex.network.name.bean.AddressRecord)}.
	 */
	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testHandleRefresh() throws IOException {
		AddressSubscriber sub = new AddressSubscriber("localhost:8181");
    	AddressEventListener listn = spy(new DemoAddressEventListener());
		List<String> ls = sub.getServiceAddrList("network.name.core.NameServer", listn);
		System.out.println("Address list: " + ls);
		assertEquals(0, ls.size());
		List<String> list;

		list = new ArrayList<String>();
		sub.handleRefresh(list);
		list.add("localhost:8456");
		list.add("network.name.core.NameServer");

		sub.handleRefresh(list);
		ArgumentCaptor<List> cap = ArgumentCaptor.forClass(List.class);
		verify(listn).addressRefresh(cap.capture());
		assertEquals(1, cap.getValue().size());
		assertEquals("localhost:8456", cap.getValue().get(0));
	}

	/**
	 * Test method for {@link org.apache.niolex.network.name.client.AddressSubscriber#getServiceAddrList(java.lang.String, org.apache.niolex.commons.event.EventListener)}.
	 * @throws IOException
	 */
	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testGetReconnected() throws Exception {
		AddressSubscriber sub = new AddressSubscriber("localhost:8181");
    	AddressEventListener listn = spy(new DemoAddressEventListener());
		List<String> ls = sub.getServiceAddrList("network.name.core.NameServer", listn);
		System.out.println("Address list: " + ls);
		sub.setSleepBetweenRetryTime(20);
		stopServer();
		startServer();
		Thread.sleep(500);
		ArgumentCaptor<List> cap = ArgumentCaptor.forClass(List.class);
		verify(listn).addressRefresh(cap.capture());
		assertEquals(0, cap.getValue().size());
	}

	/**
	 * Test method for {@link org.apache.niolex.network.name.client.AddressSubscriber#setRpcHandleTimeout(int)}.
	 * @throws IOException
	 */
	@Test
	public void testSetRpcHandleTimeout() throws Exception {
		final AddressSubscriber sub = new AddressSubscriber("localhost:8181");
		final AtomicBoolean bool = new AtomicBoolean(false);
		sub.setRpcHandleTimeout(4230);
		stopServer();
		Thread t = new Thread() {
			public void run() {
				try {
				AddressEventListener listn = spy(new DemoAddressEventListener());
				// Run Chien
				List<String> ls = sub.getServiceAddrList("network.name.core.NameServer", listn);
				System.out.println("Address list:\n\n\n " + ls);
				} catch (NameServiceException e) {
					bool.getAndSet(true);
				}
			}
		};
		t.start();
		Thread.sleep(50);
		t.interrupt();
		Thread.sleep(50);
		assertTrue(bool.get());
	}

}
