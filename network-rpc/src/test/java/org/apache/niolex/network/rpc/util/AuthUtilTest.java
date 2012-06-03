/**
 * AuthUtilTest.java
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
package org.apache.niolex.network.rpc.util;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-3
 */
public class AuthUtilTest {

	/**
	 * Test method for {@link org.apache.niolex.network.rpc.util.AuthUtil#authHeader(java.lang.String, java.lang.String)}.
	 */
	@Test
	public final void testAuthHeader() {
		String s = AuthUtil.authHeader("webadmin", "IJDieio3980");
		System.out.println(s);
		assertEquals("Basic d2ViYWRtaW46SUpEaWVpbzM5ODA=", s);
	}

	/**
	 * Test method for {@link org.apache.niolex.network.rpc.util.AuthUtil#genSessionId(int)}.
	 */
	@Test
	public final void testGenSessionId() {
		String s = AuthUtil.genSessionId(45);
		String q = AuthUtil.genSessionId(45);
		assertEquals(45, s.length());
		assertEquals(45, q.length());
		assertNotSame(s, q);
	}

	/**
	 * Test method for {@link org.apache.niolex.network.rpc.util.AuthUtil#checkServerStatus(java.lang.String, int, int)}.
	 */
	@Test
	public final void testCheckServerStatus() {
		boolean b = AuthUtil.checkServerStatus("http://www.baidu.com", 4000, 4000);
		System.out.println(b);
		assertTrue(b);
		boolean c = AuthUtil.checkServerStatus("http://cy.baidu.com/find.php", 4000, 4000);
		System.out.println(c);
		assertFalse(c);
		boolean d = AuthUtil.checkServerStatus("http://cycqc.baidu.com/find.php", 4000, 4000);
		System.out.println(d);
		assertFalse(d);
		boolean e = AuthUtil.checkServerStatus("http://www.cs.zju.edu.cn/org/codes/404.html", 4000, 4000);
		System.out.println(e);
		assertFalse(e);

	}

}
