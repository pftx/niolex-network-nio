/**
 * AddressRecordSerializerTest.java
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

import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-26
 */
public class AddressRecordSerializerTest {

	/**
	 * Test method for {@link org.apache.niolex.network.name.bean.AddressRecordSerializer#getCode()}.
	 */
	@Test
	public void testGetCode() {
		AddressRecordSerializer rec = new AddressRecordSerializer((short) 41);
		assertEquals((short) 41, rec.getCode());
	}

	/**
	 * Test method for {@link org.apache.niolex.network.name.bean.AddressRecordSerializer#serObj(org.apache.niolex.network.name.bean.AddressRecord)}.
	 */
	@Test
	public void testSerObjAddressRecord() {
		AddressRecordSerializer rec = new AddressRecordSerializer((short) 41);
		AddressRecord ad = new AddressRecord("network/name", "local/8004");
		byte[] dd = rec.serObj(ad);
		AddressRecord af = rec.deserObj(dd);
		assertEquals(ad, af);
		assertEquals("network/name", af.getAddressKey());
		assertEquals("local/8004", af.getAddressValue());
	}

	/**
	 * Test method for {@link org.apache.niolex.network.name.bean.AddressRecordSerializer#deserObj(byte[])}.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testDeserObjByteArray() {
		AddressRecordSerializer rec = new AddressRecordSerializer((short) 41);
		byte[] dd = "This is bug/*/very bad.".getBytes();
		AddressRecord af = rec.deserObj(dd);
		assertEquals("network/name", af.getAddressKey());
		assertEquals("local/8004", af.getAddressValue());
	}

}
