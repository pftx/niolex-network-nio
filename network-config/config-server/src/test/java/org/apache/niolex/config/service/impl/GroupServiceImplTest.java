/**
 * GroupServiceImplTest.java
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.apache.niolex.commons.test.OrderedRunner;
import org.apache.niolex.config.bean.ConfigItem;
import org.apache.niolex.config.bean.ConfigGroup;
import org.apache.niolex.config.bean.SyncBean;
import org.apache.niolex.config.bean.UserInfo;
import org.apache.niolex.config.config.AttachKey;
import org.apache.niolex.config.core.Context;
import org.apache.niolex.config.core.MemoryStorage;
import org.apache.niolex.network.server.BasePacketWriter;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-7-9
 */
@RunWith(OrderedRunner.class)
public class GroupServiceImplTest {

	private GroupServiceImpl sevice = Context.CTX.getBean(GroupServiceImpl.class);
	private MemoryStorage storage = Context.CTX.getBean(MemoryStorage.class);
	private BasePacketWriter writer = new BasePacketWriter() {

		@Override
		public String getRemoteName() {
			return "Auto-generated method stub";
		}};

	/**
		 * Test method for {@link org.apache.niolex.config.service.impl.GroupServiceImpl#cliSubscribeGroup(java.lang.String, org.apache.niolex.network.IPacketWriter)}.
		 */
		@Test
		public void testCliSubscribeGroup() {
			sevice.syncAllGroupsWithDB();
			UserInfo info = new UserInfo();
			info.setUserId(2);
			info.setUserRole("NODE");
			writer.attachData(AttachKey.USER_INFO, info);
			boolean b= sevice.cliSubscribeGroup("configserver.test.demo", writer);
			assertTrue(b);
		}

	/**
		 * Test method for {@link org.apache.niolex.config.service.impl.GroupServiceImpl#cliSubscribeGroup(java.lang.String, org.apache.niolex.network.IPacketWriter)}.
		 */
		@Test
		public void testCliSubscribeGroupNeg() {
			UserInfo info = new UserInfo();
			info.setUserId(5);
			info.setUserRole("NODE");
			writer.attachData(AttachKey.USER_INFO, info);
			boolean b= sevice.cliSubscribeGroup("configserver.test.demo", writer);
			assertFalse(b);
		}

	/**
		 * Test method for {@link org.apache.niolex.config.service.impl.GroupServiceImpl#cliSubscribeGroup(java.lang.String, org.apache.niolex.network.IPacketWriter)}.
		 */
		@Test
		public void testCliSubscribeGroupNof() {
			UserInfo info = new UserInfo();
			info.setUserId(2);
			info.setUserRole("NODE");
			writer.attachData(AttachKey.USER_INFO, info);
			boolean b= sevice.cliSubscribeGroup("configserver.test.demo2", writer);
			assertFalse(b);
		}

	/**
		 * Test method for {@link org.apache.niolex.config.service.impl.GroupServiceImpl#cliSyncGroup(org.apache.niolex.config.bean.SyncBean, org.apache.niolex.network.IPacketWriter)}.
		 */
		@Test
		public void testCliSyncGroup() {
			UserInfo info = new UserInfo();
			info.setUserId(2);
			info.setUserRole("NODE");
			SyncBean bean = new SyncBean();
			bean.setGroupData(new HashMap<String, Long>());
			bean.setGroupName("configserver.test.demo");
			writer.attachData(AttachKey.USER_INFO, info);
			sevice.cliSyncGroup(bean, writer);
		}

	/**
	 * Test method for {@link org.apache.niolex.config.service.impl.GroupServiceImpl#syncAllGroupsWithDB()}.
	 */
	@Test
	public void testSyncAllGroupsWithDB() {
		sevice.syncAllGroupsWithDB();
	}

	/**
	 * Test method for {@link org.apache.niolex.config.service.impl.GroupServiceImpl#svrSendDiff(org.apache.niolex.config.bean.ConfigItem)}.
	 */
	@Test
	public void testSvrSendDiff() {
		ConfigItem diff = new ConfigItem();
		diff.setGroupId(2);
		diff.setKey("demo.key");
		diff.setValue("unit stset");
		diff.setUpdateTime(System.currentTimeMillis());
		sevice.svrSendDiff(diff);
		ConfigGroup c = storage.get("configserver.test.demo");
		ConfigItem item = c.getGroupData().get("demo.key");
		assertEquals(item.getValue(), "unit stset");
		item.setUpdateTime(1234L);
	}

	/**
	 * Test method for {@link org.apache.niolex.config.service.impl.GroupServiceImpl#svrSendDiff(org.apache.niolex.config.bean.ConfigItem)}.
	 */
	@Test
	public void testSvrSendDiffNeg2() {
		ConfigItem diff = new ConfigItem();
		diff.setGroupId(17);
		diff.setKey("demo.key");
		diff.setValue("unit stset");
		diff.setUpdateTime(System.currentTimeMillis());
		sevice.svrSendDiff(diff);
		ConfigGroup c = storage.get("configserver.test.demo");
		ConfigItem item = c.getGroupData().get("demo.key");
		assertEquals(item.getValue(), "unit stset");
		item.setUpdateTime(1234L);
	}

	/**
	 * Test method for {@link org.apache.niolex.config.service.impl.GroupServiceImpl#svrSendDiff(org.apache.niolex.config.bean.ConfigItem)}.
	 */
	@Test
	public void testSvrSendDiffNeg() {
		ConfigItem diff = new ConfigItem();
		diff.setGroupId(1);
		diff.setKey("demo.key");
		diff.setValue("unit 352352345");
		sevice.svrSendDiff(diff);
		ConfigGroup c = storage.get("configserver.test.demo");
		assertNotSame(c.getGroupData().get("demo.key").getValue(), "unit 352352345");
	}

	/**
	 * Test method for {@link org.apache.niolex.config.service.impl.GroupServiceImpl#loadGroup(java.lang.String)}.
	 */
	@Test
	public void testLoadGroup() {
		sevice.svrSendGroup("configserver.test.demo");
		ConfigGroup c = storage.get("configserver.test.demo");
		assertEquals(c.getGroupData().get("demo.key").getValue(), "demo.value");
	}

}
