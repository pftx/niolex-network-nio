/**
 * AddressListSerializerTest.java
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

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-6-26
 */
public class AddressListSerializerTest {

	/**
	 * Test method for {@link org.apache.niolex.network.name.bean.AddressListSerializer#getCode()}.
	 */
	@Test
	public void testGetCode() {
		AddressListSerializer ser = new AddressListSerializer((short) 23);
		assertEquals(ser.getCode(), (short) 23);
	}

	/**
	 * Test method for {@link org.apache.niolex.network.name.bean.AddressListSerializer#serObj(java.util.List)}.
	 */
	@Test
	public void testSerObjListOfString() {
		AddressListSerializer ser = new AddressListSerializer((short) 23);
		List<String> ss = Arrays.asList("implemented", "yet", "Not");
		byte[] bbt = ser.serObj(ss);
		List<String> ls = ser.deserObj(bbt);
		System.out.println(ls);
		assertEquals(ss, ls);
	}

	/**
	 * Test method for {@link org.apache.niolex.network.name.bean.AddressListSerializer#deserObj(byte[])}.
	 */
	@Test
	public void testDeserObjByteArray() {
		AddressListSerializer ser = new AddressListSerializer((short) 23);
		List<String> ss = Arrays.asList("implemented");
		byte[] bbt = ser.serObj(ss);
		List<String> ls = ser.deserObj(bbt);
		System.out.println(ls);
		assertEquals(ss, ls);
	}

	/**
	 * Test method for {@link org.apache.niolex.network.name.bean.AddressListSerializer#deserObj(byte[])}.
	 */
	@Test
	public void testDeserObjByte() {
		AddressListSerializer ser = new AddressListSerializer((short) 23);
		List<String> ss = Arrays.asList();
		byte[] bbt = ser.serObj(ss);
		List<String> ls = ser.deserObj(bbt);
		System.out.println(ls);
		assertEquals(ss.size(), ls.size());
	}

}
