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

import static org.junit.Assert.*;

import java.util.Map;

import org.apache.niolex.address.ext.QuotaInfo;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-8-13
 */
public class QuotaInfoTest {

	static QuotaInfo info = new QuotaInfo();

	/**
	 * Test method for {@link org.apache.niolex.find.bean.QuotaInfo#getClientId()}.
	 */
	@Test
	public void testGetClientId() {
		info.setClientName("fasd");
		assertEquals(info.getClientName(), "fasd");
		Map<String, QuotaInfo> map = QuotaInfo.parse("find,300,10000;xoa,10,5000;abc-10-20");
		System.out.println(map);
		assertEquals("{xoa={cN=xoa, tQ=10, sQ=5000}, find={cN=find, tQ=300, sQ=10000}}", map.toString());
	}

	@Test
	public void testEqu() {
	    QuotaInfo in2fo = new QuotaInfo(null);
	    assertFalse(in2fo.equals("info"));
	    assertFalse(in2fo.equals(info));
	    QuotaInfo in3fo = new QuotaInfo(null);
	    assertFalse(in2fo.equals(in3fo));
	    assertEquals(in2fo.hashCode(), in3fo.hashCode());
	}

	/**
	 * Test method for {@link org.apache.niolex.find.bean.QuotaInfo#getTotalQuota()}.
	 */
	@Test
	public void testGetTotalQuota() {
		info.setTotalQuota(987423);
		assertEquals(info.getTotalQuota(), 987423);
	}

	/**
	 * Test method for {@link org.apache.niolex.find.bean.QuotaInfo#getSingleQuota()}.
	 */
	@Test
	public void testGetSingleQuota() {
		QuotaInfo in2fo = new QuotaInfo("test,10,100");
        QuotaInfo in3fo = new QuotaInfo("test,10,100");
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
