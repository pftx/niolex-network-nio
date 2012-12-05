/**
 * AddressRegiSerializerTest.java
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

import org.apache.niolex.network.Config;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-6-27
 */
public class AddressRegiSerializerTest {

	/**
	 * Test method for {@link org.apache.niolex.network.name.bean.AddressRegiBean#serObj(org.apache.niolex.network.name.bean.AddressRegiBean)}.
	 */
	@Test
	public void testSerObjAddressRegiBean() {
		AddressRegiSerializer r = new AddressRegiSerializer(Config.CODE_NAME_PUBLISH);
		assertEquals(Config.CODE_NAME_PUBLISH, r.getCode());
		AddressRegiBean q = new AddressRegiBean("network/name", "local/8004");
		byte[] t = r.serObj(q);
		AddressRegiBean s = r.deserObj(t);
		assertEquals("network/name", s.getAddressKey());
		assertEquals("local/8004", s.getAddressValue());
	}


	/**
	 * Test method for {@link org.apache.niolex.network.name.bean.AddressRegiBean#serObj(org.apache.niolex.network.name.bean.AddressRegiBean)}.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testSerObjAddress() {
		AddressRegiSerializer r = new AddressRegiSerializer(Config.CODE_NAME_PUBLISH);
		assertEquals(Config.CODE_NAME_PUBLISH, r.getCode());
		AddressRegiBean s = r.deserObj("This is bad".getBytes());
		assertEquals("network/name", s.getAddressKey());
	}
	/**
	 * Test method for {@link org.apache.niolex.network.name.bean.AddressRegiSerializer#getCode()}.
	 */
	@Test
	public void testGetCode() {
		AddressRegiSerializer r = new AddressRegiSerializer(Config.CODE_NAME_PUBLISH);
		assertEquals(Config.CODE_NAME_PUBLISH, r.getCode());
	}

}
