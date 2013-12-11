/**
 * UtilTest.java
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
package org.apache.niolex.address.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-25
 */
public class UtilTest extends PathUtil {

    @Test
    public void nothing() {
    }

	/**
	 * Test method for {@link org.apache.niolex.find.util.PathUtil#validateVersion(java.lang.String)}.
	 */
	@Test
	public void testValidateVersion_1() {
		Result r = PathUtil.validateVersion("@12");
		System.out.println(r);
		assertFalse(r.isValid());
	}

	/**
	 * Test method for {@link org.apache.niolex.find.util.PathUtil#validateVersion(java.lang.String)}.
	 */
	@Test
	public void testValidateVersion0() {
		Result r = PathUtil.validateVersion("125+ad");
		System.out.println(r);
		assertFalse(r.isValid());
	}

	/**
	 * Test method for {@link org.apache.niolex.find.util.PathUtil#validateVersion(java.lang.String)}.
	 */
	@Test
	public void testValidateVersion1() {
		Result r = PathUtil.validateVersion("125ad");
		System.out.println(r);
		assertFalse(r.isValid());
	}

	/**
	 * Test method for {@link org.apache.niolex.find.util.PathUtil#validateVersion(java.lang.String)}.
	 */
	@Test
	public void testValidateVersion11() {
		Result r = PathUtil.validateVersion("125+126");
		System.out.println(r);
		assertFalse(r.isValid());
	}

	/**
	 * Test method for {@link org.apache.niolex.find.util.PathUtil#validateVersion(java.lang.String)}.
	 */
	@Test
	public void testValidateVersion12() {
		Result r = PathUtil.validateVersion("125-");
		System.out.println(r);
		assertFalse(r.isValid());
	}

	/**
	 * Test method for {@link org.apache.niolex.find.util.PathUtil#validateVersion(java.lang.String)}.
	 */
	@Test
	public void testValidateVersion2() {
		Result r = PathUtil.validateVersion("423");
		System.out.println(r);
		assertTrue(r.isValid());
		assertFalse(r.isRange());
	}

	/**
	 * Test method for {@link org.apache.niolex.find.util.PathUtil#validateVersion(java.lang.String)}.
	 */
	@Test
	public void testValidateVersion3() {
		Result r = PathUtil.validateVersion("543+");
		System.out.println(r);
		assertTrue(r.isValid());
		assertTrue(r.isRange());
		assertEquals(543, r.getLow());
		assertEquals(0x7fffffff, r.getHigh());
	}

	/**
	 * Test method for {@link org.apache.niolex.find.util.PathUtil#validateVersion(java.lang.String)}.
	 */
	@Test
	public void testValidateVersion4() {
		Result r = PathUtil.validateVersion("543-2343");
		System.out.println(r);
		assertTrue(r.isValid());
		assertTrue(r.isRange());
		assertEquals(543, r.getLow());
		assertEquals(2343, r.getHigh());
	}

}
