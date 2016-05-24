/**
 * AuthenServiceImplTest.java
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
package org.apache.niolex.config.service.impl;

import static org.junit.Assert.*;

import org.apache.niolex.config.bean.ConfigGroup;
import org.apache.niolex.config.bean.SubscribeBean;
import org.apache.niolex.config.bean.UserInfo;
import org.apache.niolex.config.config.AttachKey;
import org.apache.niolex.config.core.Context;
import org.apache.niolex.network.server.BasePacketWriter;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-7-9
 */
public class AuthenServiceImplTest {

	private AuthenServiceImpl sevice = Context.CTX.getBean(AuthenServiceImpl.class);
	private BasePacketWriter writer = new BasePacketWriter() {

		@Override
		public String getRemoteName() {
			return "Auto-generated method stub";
		}};

	/**
	 * Test method for {@link org.apache.niolex.config.service.impl.AuthenServiceImpl#authUser(org.apache.niolex.config.bean.SubscribeBean, org.apache.niolex.network.IPacketWriter)}.
	 */
	@Test
	public void testAuthUser() {
		SubscribeBean bean = new SubscribeBean();
		bean.setUserName("node");
		bean.setPassword("nodepasswd");
		boolean b = sevice.authUser(bean , writer);
		assertTrue(b);
	}

	/**
	 * Test method for {@link org.apache.niolex.config.service.impl.AuthenServiceImpl#authUser(org.apache.niolex.config.bean.SubscribeBean, org.apache.niolex.network.IPacketWriter)}.
	 */
	@Test
	public void testAuthUserNeg() {
		SubscribeBean bean = new SubscribeBean();
		bean.setUserName("node-unlike");
		bean.setPassword("nodepasswd");
		boolean b = sevice.authUser(bean , writer);
		assertFalse(b);
	}

	/**
	 * Test method for {@link org.apache.niolex.config.service.impl.AuthenServiceImpl#hasReadAuth(org.apache.niolex.config.bean.ConfigGroup, org.apache.niolex.network.IPacketWriter)}.
	 */
	@Test
	public void testHasReadAuth() {
		UserInfo info = new UserInfo();
		info.setUserId(2);
		info.setUserRole("gg");
		writer.attachData(AttachKey.USER_INFO, info);
		ConfigGroup group = new ConfigGroup();
		group.setGroupId(1);
		boolean b = sevice.hasReadAuth(group, writer);
		assertTrue(b);
	}
	
	/**
	 * Test method for {@link org.apache.niolex.config.service.impl.AuthenServiceImpl#hasReadAuth(org.apache.niolex.config.bean.ConfigGroup, org.apache.niolex.network.IPacketWriter)}.
	 */
	@Test
	public void testHasReadAuthAdmin() {
	    UserInfo info = new UserInfo();
	    info.setUserId(6);
	    info.setUserRole("ADMIN");
	    writer.attachData(AttachKey.USER_INFO, info);
	    ConfigGroup group = new ConfigGroup();
	    group.setGroupId(1);
	    boolean b = sevice.hasReadAuth(group, writer);
	    assertTrue(b);
	}

	/**
	 * Test method for {@link org.apache.niolex.config.service.impl.AuthenServiceImpl#hasReadAuth(org.apache.niolex.config.bean.ConfigGroup, org.apache.niolex.network.IPacketWriter)}.
	 */
	@Test
	public void testHasReadAuthNeg() {
		UserInfo info = new UserInfo();
		info.setUserId(3);
		info.setUserRole("gg");
		ConfigGroup group = new ConfigGroup();
		group.setGroupId(100);
		boolean b = sevice.hasReadAuth(group, writer);
		assertFalse(b);
	}


	/**
	 * Test method for {@link org.apache.niolex.config.service.impl.AuthenServiceImpl#hasReadAuth(org.apache.niolex.config.bean.ConfigGroup, org.apache.niolex.network.IPacketWriter)}.
	 */
	@Test
	public void testGetUserId() {
		UserInfo info = new UserInfo();
		info.setUserId(3);
		writer.attachData(AttachKey.USER_INFO, info);
		int id = sevice.getUserId(writer);
		assertEquals(3, id);
	}

	/**
	 * Test method for {@link org.apache.niolex.config.service.impl.AuthenServiceImpl#hasReadAuth(org.apache.niolex.config.bean.ConfigGroup, org.apache.niolex.network.IPacketWriter)}.
	 */
	@Test
	public void testGetUserIdNeg() {
		int id = sevice.getUserId(writer);
		assertEquals(-1, id);
	}


	/**
	 * Test method for {@link org.apache.niolex.config.service.impl.AuthenServiceImpl#hasReadAuth(org.apache.niolex.config.bean.ConfigGroup, org.apache.niolex.network.IPacketWriter)}.
	 */
	@Test
	public void testGetUserRol() {
		UserInfo info = new UserInfo();
		info.setUserId(3);
		info.setUserRole("Good");
		writer.attachData(AttachKey.USER_INFO, info);
		String role = sevice.getUserRole(writer);
		assertEquals("Good", role);
	}

	/**
	 * Test method for {@link org.apache.niolex.config.service.impl.AuthenServiceImpl#hasReadAuth(org.apache.niolex.config.bean.ConfigGroup, org.apache.niolex.network.IPacketWriter)}.
	 */
	@Test
	public void testGetUserRoleNeg() {
		String role = sevice.getUserRole(writer);
		assertEquals(null, role);
	}
}
