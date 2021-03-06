/**
 * AddressRegiBeanTest.java
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
 * @since 2012-6-26
 */
public class AddressRegiBeanTest {

	/**
	 * Test method for {@link org.apache.niolex.network.name.bean.AddressRegiBean#AddressRegiBean()}.
	 */
	@Test
	public void testAddressRegiBean() {
		AddressRegiBean r = new AddressRegiBean();
		r.setAddressKey("network/name");
		r.setAddressValue("local/8004");
		assertEquals("network/name", r.getAddressKey());
		assertEquals("local/8004", r.getAddressValue());
	}

	/**
	 * Test method for {@link org.apache.niolex.network.name.bean.AddressRegiBean#AddressRegiBean(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testAddressRegiBeanStringString() {
		AddressRegiBean r = new AddressRegiBean("network/name", "local/8004");
		assertEquals("network/name", r.getAddressKey());
		assertEquals("local/8004", r.getAddressValue());
	}

}
