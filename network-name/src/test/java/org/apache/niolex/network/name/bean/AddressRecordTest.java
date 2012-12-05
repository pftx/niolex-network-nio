/**
 * AddressRecordTest.java
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

import org.apache.niolex.network.name.bean.AddressRecord.Status;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-6-26
 */
public class AddressRecordTest {


	/**
	 * Test method for {@link org.apache.niolex.network.name.bean.AddressRecord#AddressRecord(org.apache.niolex.network.name.bean.AddressRegiBean)}.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testAddressRecord() {
		AddressRecord ad = new AddressRecord("network/name", null);
		assertEquals("network/name", ad.getAddressKey());
		assertEquals("local/8004", ad.getAddressValue());
	}

	/**
	 * Test method for {@link org.apache.niolex.network.name.bean.AddressRecord#AddressRecord(org.apache.niolex.network.name.bean.AddressRegiBean)}.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testAddressRecord2() {
		AddressRecord ad = new AddressRecord(null, null);
		assertEquals("network/name", ad.getAddressKey());
		assertEquals("local/8004", ad.getAddressValue());
	}

	/**
	 * Test method for {@link org.apache.niolex.network.name.bean.AddressRecord#AddressRecord(org.apache.niolex.network.name.bean.AddressRegiBean)}.
	 */
	@Test
	public void testAddressRecordAddressRegiBean() {
		AddressRegiBean r = new AddressRegiBean();
		r.setAddressKey("network/name");
		r.setAddressValue("local/8004");
		AddressRecord ad = new AddressRecord(r);
		assertEquals(Status.OK, ad.getStatus());
		assertEquals(0, ad.getLastTime());
		assertEquals("network/name", ad.getAddressKey());
		assertEquals("local/8004", ad.getAddressValue());
	}

	/**
	 * Test method for {@link org.apache.niolex.network.name.bean.AddressRecord#AddressRecord(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testAddressRecordStringString() {
		AddressRecord ad = new AddressRecord("network/name", "local/8004");
		assertEquals("network/name", ad.getAddressKey());
		assertEquals("local/8004", ad.getAddressValue());
	}

	/**
	 * Test method for {@link org.apache.niolex.network.name.bean.AddressRecord#getStatus()}.
	 */
	@Test
	public void testGetStatus() {
		AddressRecord ad = new AddressRecord("network/name", "local/8004");
		assertEquals("network/name", ad.getAddressKey());
		assertEquals("local/8004", ad.getAddressValue());
		ad.setStatus(Status.DISCONNECTED);
		assertEquals(Status.DISCONNECTED, ad.getStatus());
		assertTrue(System.currentTimeMillis() - ad.getLastTime() < 100);
	}

	/**
	 * Test method for {@link org.apache.niolex.network.name.bean.AddressRecord#setStatus(org.apache.niolex.network.name.bean.AddressRecord.Status)}.
	 */
	@Test
	public void testSetStatus() {
		AddressRecord ad = new AddressRecord("network/name", "local/8004");
		ad.setAddressKey("fake");
		ad.setAddressValue("remote/0932");
		assertEquals("fake", ad.getAddressKey());
		assertEquals("remote/0932", ad.getAddressValue());
	}

	/**
	 * Test method for {@link org.apache.niolex.network.name.bean.AddressRecord#toString()}.
	 */
	@Test
	public void testToString() {
		AddressRecord ad = new AddressRecord("network/name", "local/8004");
		AddressRecord ac = new AddressRecord("network/name", "local/8004");
		assertEquals("OK, network/name, local/8004", ad.toString());
		System.out.println(ac.hashCode());
		assertTrue(ac.equals(ad));
		assertTrue(ac.equals(ac));
		assertFalse(ac.equals(null));
		assertFalse(ac.equals("local/8004"));
		AddressRecord af = new AddressRecord("network/name2", "local/8004");
		assertFalse(ac.equals(af));
		AddressRecord ag = new AddressRecord("network/name", "locale/8004");
		assertFalse(ac.equals(ag));
	}

}
