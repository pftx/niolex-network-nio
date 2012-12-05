/**
 * MemoryStorageTest.java
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
package org.apache.niolex.config.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;

import org.apache.niolex.commons.test.OrderedRunner;
import org.apache.niolex.config.bean.ConfigGroup;
import org.apache.niolex.config.bean.ConfigItem;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-7-11
 */
@RunWith(OrderedRunner.class)
public class MemoryStorageTest {
	private static final MemoryStorage storage = new MemoryStorage();

	/**
	 * Test method for {@link org.apache.niolex.config.core.MemoryStorage#store(org.apache.niolex.config.bean.ConfigGroup)}.
	 */
	@Test
	public void testAStore() {
		ConfigGroup grp = new ConfigGroup();
		grp.setGroupId(123);
		grp.setGroupName("Like Name");
		ConfigItem item = new ConfigItem();
		item.setGroupId(123);
		item.setKey("ckey");
		item.setValue("good.");
		grp.getGroupData().put("ckey", item);
		List<ConfigItem> list = storage.store(grp);
		assertNull(list);
		list = storage.store(grp);
		assertEquals(0, list.size());
	}

	@Test
	public void testBStore2() {
		ConfigGroup grp = new ConfigGroup();
		grp.setGroupId(123);
		grp.setGroupName("Like Name");
		ConfigItem item = new ConfigItem();
		item.setGroupId(123);
		item.setKey("ckey");
		item.setValue("good.");
		grp.getGroupData().put("ckey", item);
		List<ConfigItem> list = storage.store(grp);
		if (list != null)
			assertEquals(0, list.size());
		item.setUpdateTime(1234);
		list = storage.store(grp);
		assertEquals(1, list.size());
	}

	/**
	 * Test method for {@link org.apache.niolex.config.core.MemoryStorage#get(java.lang.String)}.
	 */
	@Test
	public void testGet() {
		ConfigGroup grp = storage.get("Like Name");
		assertEquals(123, grp.getGroupId());
	}

	/**
	 * Test method for {@link org.apache.niolex.config.core.MemoryStorage#getAll()}.
	 */
	@Test
	public void testGetAll() {
		Collection<ConfigGroup> cols = storage.getAll();
		assertEquals(1, cols.size());
	}

	/**
	 * Test method for {@link org.apache.niolex.config.core.MemoryStorage#findGroupName(int)}.
	 */
	@Test
	public void testFindGroupName() {
		assertEquals("Like Name", storage.findGroupName(123));
		assertEquals(null, storage.findGroupName(1234));
	}

	/**
	 * Test method for {@link org.apache.niolex.config.core.MemoryStorage#updateConfigItem(java.lang.String, org.apache.niolex.config.bean.ConfigItem)}.
	 */
	@Test
	public void testUpdateConfigItem() {
		ConfigItem item = new ConfigItem();
		item.setGroupId(123);
		item.setKey("ckey");
		item.setValue("good.");
		item.setUpdateTime(4);
		boolean b = storage.updateConfigItem("Like Name", item);
		assertFalse(b);
		item.setUpdateTime(432112);
		b = storage.updateConfigItem("Like Name", item);
		assertTrue(b);
		item.setUpdateTime(4321122);
		b = storage.updateConfigItem("Like 12313", item);
		assertFalse(b);
	}

}
