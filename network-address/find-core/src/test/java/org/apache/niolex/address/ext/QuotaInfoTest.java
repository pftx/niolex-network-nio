/**
 * QuotaInfoTest.java
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
package org.apache.niolex.address.ext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-8-13
 */
public class QuotaInfoTest {

	QuotaInfo info = new QuotaInfo();

	@Before
	public void setup() {
	    info.setMinuteQuota(987423);
	    info.setSecondQuota(542);
	}

	@Test
	public void testQuotaInfo() {
	    QuotaInfo in2fo = new QuotaInfo();
	    assertEquals(0, in2fo.hashCode());
	}

	/**
	 * Test method for {@link org.apache.niolex.address.ext.QuotaInfo#getMinuteQuota()}.
	 */
	@Test
	public void testGetMinuteQuota() {
	    assertEquals(987423, info.getMinuteQuota());
	}

	@Test
	public void testSetMinuteQuota() throws Exception {
	    assertFalse(info.equals(null));
        assertFalse(info.equals("info"));
        assertTrue(info.equals(info));
	}

	@Test
    public void testGetSecondQuota() {
	    assertEquals(542, info.getSecondQuota());
	}

    @Test
    public void testSetSecondQuota() throws Exception {
        QuotaInfo in2fo = QuotaInfo.parse("10,110");
        QuotaInfo in3fo = QuotaInfo.parse("10,100");
        assertFalse(in2fo.equals(in3fo));
        assertFalse(in3fo.hashCode() == in2fo.hashCode());
    }

	/**
	 * Test method for {@link org.apache.niolex.address.ext.QuotaInfo#parse(String)}.
	 */
	@Test
	public void testParse() {
	    QuotaInfo in2fo = QuotaInfo.parse("10,100");
	    QuotaInfo in3fo = QuotaInfo.parse("10,100");
	    assertTrue(in2fo.equals(in3fo));
	    assertFalse(in3fo == in2fo);
	}

	@Test
	public void testHashCode() throws Exception {
	    assertEquals(1004225, info.hashCode());
	}

	@Test
	public void testEquals() {
	    QuotaInfo in2fo = QuotaInfo.parse("293212");
	    assertFalse(in2fo.equals(info));
	    QuotaInfo in3fo = new QuotaInfo();
	    assertTrue(in2fo.equals(in3fo));
	    assertEquals(in2fo.hashCode(), in3fo.hashCode());
	}

	/**
	 * Test method for {@link org.apache.niolex.address.ext.QuotaInfo#toString()}.
	 */
	@Test
	public void testToString() {
		assertEquals("{secQ=542, minQ=987423}", info.toString());
	}

}
