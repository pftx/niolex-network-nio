/**
 * GroupDaoImplTest.java
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

import static org.junit.Assert.*;

import java.util.List;

import org.apache.niolex.config.bean.GroupConfig;
import org.apache.niolex.config.core.Context;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-7-6
 */
public class GroupDaoImplTest {

	private GroupDaoImpl dao = Context.CTX.getBean(GroupDaoImpl.class);

	/**
	 * Test method for {@link org.apache.niolex.config.dao.impl.GroupDaoImpl#addGroup(java.lang.String)}.
	 */
	@Test
	public void testAddGroup() {
		String name = "unittest." + System.currentTimeMillis();
		boolean b = dao.addGroup(name);
		assertTrue(b);
		b = dao.addGroup(name);
		assertFalse(b);
	}

	/**
	 * Test method for {@link org.apache.niolex.config.dao.impl.GroupDaoImpl#loadAllGroups()}.
	 */
	@Test
	public void testLoadAllGroups() {
		List<GroupConfig> list = dao.loadAllGroups();
		int cgid = 1;
		System.out.println("Group size " + list.size());
		for (GroupConfig gc : list) {
			assertTrue(cgid <= gc.getGroupId());
			cgid = gc.getGroupId();
		}
	}

	/**
	 * Test method for {@link org.apache.niolex.config.dao.impl.GroupDaoImpl#loadDBTime()}.
	 */
	@Test
	public void testLoadDBTime() {
		long a = dao.loadDBTime();
		System.out.println(a);
		long b = System.currentTimeMillis();
		System.out.println(b);
		assertTrue(a < b);
	}

	/**
	 * Test method for {@link org.apache.niolex.config.dao.impl.GroupDaoImpl#loadGroup(java.lang.String)}.
	 */
	@Test
	public void testLoadGroup() {
		GroupConfig gc = dao.loadGroup("configserver.test.demo");
		assertEquals(gc.getGroupId(), 1);
		assertEquals(gc.getGroupName(), "configserver.test.demo");
		gc = dao.loadGroup("configserver.test.donotinsert");
		assertNull(gc);
	}

}
