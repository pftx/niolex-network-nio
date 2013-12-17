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
public class VersionUtilTest extends VersionUtil {

    @Test
    public void nothing() {
        assertEquals("93.12.98.334", decodeVersion(931298334));
        assertEquals("0.0.1.12", decodeVersion(1012));
    }

    @Test
    public void testEncodeVersion() throws Exception {
        assertEquals(931298334, encodeVersion("93.12.98.334"));
        assertEquals(11298334, encodeVersion("1.12.98.334"));
        assertEquals(10098334, encodeVersion("1.0.98.334"));
        assertEquals(10006334, encodeVersion("1.0.6.334"));
        assertEquals(10006023, encodeVersion("1.0.6.023"));
        assertEquals(-1, encodeVersion("1.0.6"));
        assertEquals(-1, encodeVersion("1.6"));
        assertEquals(-1, encodeVersion("1.6.0.0.123"));
        assertEquals(-1, encodeVersion("13.12.17-snapshot"));
    }

    @Test
    public void testDecodeVersion() throws Exception {
        boolean f;
        f = false;
        try {
            encodeVersion("-93.12.98.334");
        } catch (Exception e) {
            assertEquals("major version must between 0 and 213", e.getMessage());
            f = true;
        }
        assertTrue(f);
        // --
        f = false;
        try {
            encodeVersion("214.12.98.334");
        } catch (Exception e) {
            assertEquals("major version must between 0 and 213", e.getMessage());
            f = true;
        }
        assertTrue(f);
        // ##_##
        f = false;
        try {
            encodeVersion("13.-12.98.334");
        } catch (Exception e) {
            assertEquals("minor version must between 0 and 99", e.getMessage());
            f = true;
        }
        assertTrue(f);
        // --
        f = false;
        try {
            encodeVersion("13.120.98.334");
        } catch (Exception e) {
            assertEquals("minor version must between 0 and 99", e.getMessage());
            f = true;
        }
        assertTrue(f);
        // ##_##
        f = false;
        try {
            encodeVersion("13.12.-98.334");
        } catch (Exception e) {
            assertEquals("patch version must between 0 and 99", e.getMessage());
            f = true;
        }
        assertTrue(f);
        // --
        f = false;
        try {
            encodeVersion("13.12.108.334");
        } catch (Exception e) {
            assertEquals("patch version must between 0 and 99", e.getMessage());
            f = true;
        }
        assertTrue(f);
        // ##_##
        f = false;
        try {
            encodeVersion("13.12.98.-334");
        } catch (Exception e) {
            assertEquals("build version must between 0 and 999", e.getMessage());
            f = true;
        }
        assertTrue(f);
        // --
        f = false;
        try {
            encodeVersion("13.12.18.1010");
        } catch (Exception e) {
            assertEquals("build version must between 0 and 999", e.getMessage());
            f = true;
        }
        assertTrue(f);
        // --
        f = false;
        try {
            encodeVersion("13.12.17.-snapshot");
        } catch (Exception e) {
            assertEquals("For input string: \"-snapshot\"", e.getMessage());
            f = true;
        }
        assertTrue(f);
    }

    /**
     * Test method for {@link org.apache.niolex.address.util.PathUtil#validateVersion(java.lang.String)}.
     */
    @Test
    public void testValidateVersion() {
        Result res = validateVersion("3-700");
        System.out.println(res);
        assertEquals("{V?true, R?true, [3, 700)}", res.toString());
    }

	/**
	 * Test method for {@link org.apache.niolex.find.util.PathUtil#validateVersion(java.lang.String)}.
	 */
	@Test
	public void testValidateVersion_1() {
		Result r = validateVersion("@12");
		System.out.println(r);
		assertFalse(r.isValid());
	}

	/**
	 * Test method for {@link org.apache.niolex.find.util.PathUtil#validateVersion(java.lang.String)}.
	 */
	@Test
	public void testValidateVersion0() {
		Result r = validateVersion("125+ad");
		System.out.println(r);
		assertFalse(r.isValid());
	}

	/**
	 * Test method for {@link org.apache.niolex.find.util.PathUtil#validateVersion(java.lang.String)}.
	 */
	@Test
	public void testValidateVersion1() {
		Result r = validateVersion("125ad");
		System.out.println(r);
		assertFalse(r.isValid());
	}

	/**
	 * Test method for {@link org.apache.niolex.find.util.PathUtil#validateVersion(java.lang.String)}.
	 */
	@Test
	public void testValidateVersion11() {
		Result r = validateVersion("125+126");
		System.out.println(r);
		assertFalse(r.isValid());
	}

	/**
	 * Test method for {@link org.apache.niolex.find.util.PathUtil#validateVersion(java.lang.String)}.
	 */
	@Test
	public void testValidateVersion12() {
		Result r = validateVersion("125-");
		System.out.println(r);
		assertFalse(r.isValid());
	}

	/**
	 * Test method for {@link org.apache.niolex.find.util.PathUtil#validateVersion(java.lang.String)}.
	 */
	@Test
	public void testValidateVersion2() {
		Result r = validateVersion("423");
		System.out.println(r);
		assertTrue(r.isValid());
		assertFalse(r.isRange());
	}

	/**
	 * Test method for {@link org.apache.niolex.find.util.PathUtil#validateVersion(java.lang.String)}.
	 */
	@Test
	public void testValidateVersion3() {
		Result r = validateVersion("543+");
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
		Result r = validateVersion("543-2343");
		System.out.println(r);
		assertTrue(r.isValid());
		assertTrue(r.isRange());
		assertEquals(543, r.getLow());
		assertEquals(2343, r.getHigh());
	}

}
