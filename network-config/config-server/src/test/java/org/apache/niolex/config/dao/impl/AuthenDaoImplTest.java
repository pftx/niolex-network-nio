/**
 * AuthenDaoImplTest.java
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
package org.apache.niolex.config.dao.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.niolex.commons.codec.SHAUtil;
import org.apache.niolex.config.bean.UserInfo;
import org.apache.niolex.config.core.Context;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-7-6
 */
public class AuthenDaoImplTest {

	private AuthenDaoImpl dao = Context.CTX.getBean(AuthenDaoImpl.class);

	/**
	 * Test method for {@link org.apache.niolex.config.dao.impl.AuthenDaoImpl#authUser(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testAuthUser() {
		String digest = null;
		try {
			digest = SHAUtil.sha1("4d6e6d7f52798", "sections");
			System.out.println(digest);
		} catch (Exception e) {
		}
		UserInfo info = dao.authUser("su", digest);
		System.out.println("User Info " + info);
		assertEquals(info.getUserId(), 1);
		assertEquals(info.getUserRole(), "ADMIN");

		info = dao.authUser("admin", digest);
		assertNull(info);
	}

	/**
	 * Test method for {@link org.apache.niolex.config.dao.impl.AuthenDaoImpl#addUser(java.lang.String, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testAddUser() {
		String digest = null;
		try {
			digest = SHAUtil.sha1("4d6e6d7f52798", "654321");
			System.out.println(digest);
		} catch (Exception e) {
		}
		boolean b = dao.addUser("admin", digest, "OP");
		assertFalse(b);
	}

	/**
	 * Test method for {@link org.apache.niolex.config.dao.impl.AuthenDaoImpl#updateUser(java.lang.String, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testUpdateUser() {
		boolean b = dao.updateUser("node", "123", "TEST");
		assertTrue(b);
		UserInfo info = dao.authUser("node", "123");
		assertEquals(info.getUserRole(), "TEST");
		b = dao.updateUser("node", "47e109e43e482e50f87504263e8dd0073a810856", "BB");
		info = dao.authUser("node", "47e109e43e482e50f87504263e8dd0073a810856");
		assertEquals(info.getUserRole(), "BB");
		assertTrue(b);
	}

	/**
	 * Test method for {@link org.apache.niolex.config.dao.impl.AuthenDaoImpl#updateUser(java.lang.String, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testUpdateUserNoDig() {
		boolean b = dao.updateUser("node", null, "NODE");
		assertTrue(b);
		UserInfo info = dao.authUser("node", "47e109e43e482e50f87504263e8dd0073a810856");
		assertEquals(info.getUserRole(), "NODE");
		b = dao.updateUser("hahaha", null, "NODE");
		assertFalse(b);
	}

	/**
	 * Test method for {@link org.apache.niolex.config.dao.impl.AuthenDaoImpl#hasReadAuth(long, long)}.
	 */
	@Test
	public void testHasReadAuth() {
		boolean b = dao.hasReadAuth(3, 5);
		assertTrue(b);
		b = dao.hasReadAuth(7, 5);
		assertFalse(b);
	}

	/**
	 * Test method for {@link org.apache.niolex.config.dao.impl.AuthenDaoImpl#addReadAuth(long, long)}.
	 */
	@Test
	public void testAddReadAuth() {
		boolean b = dao.hasReadAuth(100, 5);
		assertFalse(b);
		dao.addReadAuth(100, 5);
		b = dao.hasReadAuth(100, 5);
		assertTrue(b);
		b = dao.delReadAuth(100, 5);
		assertTrue(b);
		b = dao.hasReadAuth(100, 5);
		assertFalse(b);
	}

	/**
	 * Test method for {@link org.apache.niolex.config.dao.impl.AuthenDaoImpl#delReadAuth(long, long)}.
	 */
	@Test
	public void testDelReadAuth() {
		boolean b = dao.delReadAuth(1, 0);
		assertFalse(b);
	}

}
