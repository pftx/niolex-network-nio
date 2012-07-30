/**
 * ItemDaoImplTest.java
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

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.apache.niolex.commons.util.DateTimeUtil;
import org.apache.niolex.config.bean.ConfigItem;
import org.apache.niolex.config.core.Context;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-7-6
 */
public class ItemDaoImplTest {

	private ItemDaoImpl dao = Context.CTX.getBean(ItemDaoImpl.class);

	/**
	 * Test method for {@link org.apache.niolex.config.dao.impl.ItemDaoImpl#loadAllConfigItems(long)}.
	 * @throws ParseException
	 */
	@Test
	public void testLoadAllConfigItems() throws ParseException {
		List<ConfigItem> list = dao.loadAllConfigItems(0);
		int cgid = 1;
		System.out.println("All item size " + list.size());
		for (ConfigItem gc : list) {
			assertTrue(cgid <= gc.getGroupId());
			cgid = gc.getGroupId();
		}
		Date q = DateTimeUtil.parseDateFromDateTimeStr("2012-07-06 11:55:40");
		list = dao.loadAllConfigItems(q.getTime());
		System.out.println("Half item size " + list.size());
		cgid = 1;
		for (ConfigItem gc : list) {
			assertTrue(cgid <= gc.getGroupId());
			cgid = gc.getGroupId();
		}
	}

	/**
	 * Test method for {@link org.apache.niolex.config.dao.impl.ItemDaoImpl#loadGroupItems(int)}.
	 */
	@Test
	public void testLoadGroupItems() {
		List<ConfigItem> list = dao.loadGroupItems(1);
		System.out.println("Item " + list);
		assertEquals(1, list.size());
	}

	/**
	 * Test method for {@link org.apache.niolex.config.dao.impl.ItemDaoImpl#updateConfig(org.apache.niolex.config.bean.ConfigItem)}.
	 * @throws ParseException
	 */
	@Test
	public void testUpdateConfig() throws ParseException {
		ConfigItem item = dao.loadGroupItems(1).get(0);
		item.setValue("This is the value from unit test. " + System.currentTimeMillis());
		boolean b = dao.updateConfig(item);
		assertTrue(b);
		item.setGroupId(2);
		b = dao.updateConfig(item);
		assertFalse(b);
	}

	/**
	 * Test method for {@link org.apache.niolex.config.dao.impl.ItemDaoImpl#addConfig(org.apache.niolex.config.bean.ConfigItem)}.
	 */
	@Test
	public void testAddConfig() {
		ConfigItem item = new ConfigItem();
		item.setGroupId(7);
		item.setcUid(100);
		item.setuUid(100);
		String key = "unittest." + System.currentTimeMillis();
		item.setKey(key);
		item.setValue("This is the value from unit test.");
		boolean b = dao.addConfig(item);
		assertTrue(b);
		b = dao.addConfig(item);
		assertFalse(b);
		item = dao.getConfig(7, key);
		System.out.println("Insert time: " + item.getUpdateTime());
	}


	/**
	 * Test method for {@link org.apache.niolex.config.dao.impl.ItemDaoImpl#addConfig(org.apache.niolex.config.bean.ConfigItem)}.
	 */
	@Test
	public void testGetConfig() {
		ConfigItem item = dao.getConfig(7, "demo");
		assertNull(item);
		item = dao.getConfig(2, "demo.key");
		assertEquals("demo.value", item.getValue());
	}

}
