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

import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-8-13
 */
public class QuotaInfoTest {

	static QuotaInfo info = new QuotaInfo();

	@Test
	public void testEqu() {
	    QuotaInfo in2fo = new QuotaInfo();
	    assertFalse(in2fo.equals("info"));
	    assertTrue(in2fo.equals(info));
	    QuotaInfo in3fo = new QuotaInfo();
	    assertTrue(in2fo.equals(in3fo));
	    assertEquals(in2fo.hashCode(), in3fo.hashCode());
	}

	/**
	 * Test method for {@link org.apache.niolex.find.bean.QuotaInfo#getTotalQuota()}.
	 */
	@Test
	public void testGetMinuteQuota() {
		info.setMinuteQuota(987423);
		assertEquals(info.getMinuteQuota(), 987423);
	}

	/**
	 * Test method for {@link org.apache.niolex.find.bean.QuotaInfo#getSingleQuota()}.
	 */
	@Test
	public void testGetSingleQuota() {
		QuotaInfo in2fo = QuotaInfo.parse("10,100");
        QuotaInfo in3fo = QuotaInfo.parse("10,100");
        assertTrue(in2fo.equals(in3fo));
	}

	/**
	 * Test method for {@link org.apache.niolex.find.bean.QuotaInfo#toString()}.
	 */
	@Test
	public void testToString() {
		System.out.println(info);
		assertTrue(info.equals(info));
	}

}
