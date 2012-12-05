/**
 * RecordStorageTest.java
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
package org.apache.niolex.network.name.bean;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.apache.niolex.network.name.bean.AddressRecord.Status;
import org.apache.niolex.network.name.event.IDispatcher;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-6-26
 */
public class RecordStorageTest {

	/**
	 * Test method for {@link org.apache.niolex.network.name.bean.RecordStorage#store(org.apache.niolex.network.name.bean.AddressRegiBean)}.
	 */
	@Test
	public void testStore() {
		RecordStorage rs = new RecordStorage();
		rs.setDeleteTime(1233);
		AddressRegiBean ben = new AddressRegiBean("network/name", "local/8004");
		rs.store(ben);
		assertEquals(1233, rs.getDeleteTime());
		List<String> ls = rs.getAddress("network/name");
		System.out.println(ls);
		assertEquals(1, ls.size());
		assertEquals("local/8004", ls.get(0));
	}

	/**
	 * Test method for {@link org.apache.niolex.network.name.bean.RecordStorage#deleteGarbage()}.
	 * @throws Throwable
	 */
	@Test
	public void testDeleteGarbage() throws Throwable {
		RecordStorage rs = new RecordStorage();
		IDispatcher dd = mock(IDispatcher.class);
		rs.setDispatcher(dd);
		rs.setDeleteTime(1);
		AddressRegiBean ben = new AddressRegiBean("network/name", "local/8004");
		AddressRecord rec = rs.store(ben);
		assertEquals(1, rs.getDeleteTime());
		rec.setStatus(Status.DISCONNECTED);
		Thread.sleep(20);
		ben = new AddressRegiBean("network/name", "remote/8004");
		rs.store(ben);
		List<String> ls = rs.getAddress("network/name");
		System.out.println(ls);
		assertEquals(2, ls.size());
		rs.deleteGarbage();
		ls = rs.getAddress("network/name");
		System.out.println(ls);
		assertEquals(1, ls.size());
		assertEquals("remote/8004", ls.get(0));
		verify(dd).fireEvent(rec);
		assertEquals(dd, rs.getDispatcher());
	}

	/**
	 * Test method for {@link org.apache.niolex.network.name.bean.RecordStorage#getAddress(java.lang.String)}.
	 */
	@Test
	public void testGetAddress() throws Throwable {
		RecordStorage rs = new RecordStorage();
		rs.setDeleteTime(1);
		AddressRegiBean ben = new AddressRegiBean("network/name", "local/8004");
		AddressRecord rec = rs.store(ben);
		assertEquals(1, rs.getDeleteTime());
		rec.setStatus(Status.DEL);
		Thread.sleep(10);
		ben = new AddressRegiBean("network/name", "remote/8004");
		rs.store(ben);
		rs.store(ben);
		List<String> ls = rs.getAddress("network/name");
		System.out.println(ls);
		assertEquals(1, ls.size());
		rs.deleteGarbage();
		ls = rs.getAddress("network/name");
		System.out.println(ls);
		assertEquals(1, ls.size());
		assertEquals("remote/8004", ls.get(0));
	}

	/**
	 * Test method for {@link org.apache.niolex.network.name.bean.RecordStorage#getDeleteTime()}.
	 */
	@Test
	public void testGetDeleteTime() throws Throwable {
		RecordStorage rs = new RecordStorage();
		rs.setDeleteTime(1);
		AddressRegiBean ben = new AddressRegiBean("network/name", "local/8004");
		AddressRecord rec = rs.store(ben);
		assertEquals(1, rs.getDeleteTime());
		rec.setStatus(Status.DEL);
		ben = new AddressRegiBean("network/name2", "remote/8004");
		rs.store(ben);
		rec = rs.store(ben);
		rec.setStatus(Status.DEL);

		Thread.sleep(20);

		List<String> ls = rs.getAddress("network/name3");
		System.out.println(ls);

		ls = rs.getAddress("network/name");
		System.out.println(ls);
		assertEquals(0, ls.size());

		rs.deleteGarbage();

		ls = rs.getAddress("network/name");
		System.out.println(ls);
		assertEquals(0, ls.size());

		rs.deleteGarbage();
		ls = rs.getAddress("network/name");
		System.out.println(ls);
	}

}
