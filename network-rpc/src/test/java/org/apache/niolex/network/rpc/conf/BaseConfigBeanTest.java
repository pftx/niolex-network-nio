/**
 * BaseConfigBeanTest.java
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
package org.apache.niolex.network.rpc.conf;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-3
 */
public class BaseConfigBeanTest {

	class Head extends BaseConfigBean {
		private long kill;

		/**
		 * Constructor
		 * @param groupName
		 */
		public Head(String groupName) {
			super(groupName);
		}

		public long getKill() {
			return kill;
		}

	}

	/**
	 * Test method for {@link org.apache.niolex.network.rpc.conf.BaseConfigBean#getProp(java.lang.String)}.
	 */
	@Test
	public final void testGetPropString() {
		BaseConfigBean c = new BaseConfigBean("FFF");
		c.setConfig("yet", "true");
		c.setConfig("method", "network");
		assertEquals("true", c.getProp("yet"));
		assertEquals("network", c.getProp("method"));
		assertEquals("FFF", c.getGroupName());
	}

	/**
	 * Test method for {@link org.apache.niolex.network.rpc.conf.BaseConfigBean#getHeader(java.lang.String)}.
	 */
	@Test
	public final void testGetHeaderString() {
		BaseConfigBean c = new BaseConfigBean("FFF");
		c.setConfig("hasHeader", "true");
		c.setConfig("header.cool", "Nice");
		assertEquals("Nice", c.getHeader("cool"));
		assertTrue(c.isHasHeader());
	}

	@Test
	public final void testGetHeaderStringn() {
		BaseConfigBean c = new BaseConfigBean("FFF");
		c.setConfig("header.cool", "Nice");
		assertEquals(null, c.getHeader("cool"));
	}

	/**
	 * Test method for {@link org.apache.niolex.network.rpc.conf.BaseConfigBean#getHeader()}.
	 */
	@Test
	public final void testGetHeader() {
		BaseConfigBean c = new BaseConfigBean("FFF");
		c.setConfig("hasHeader", "true");
		c.setConfig("header.cool", "Nice");
		c.setConfig("header.link", "BaseConfigBean");
		assertEquals(2, c.getHeader().size());
	}

	/**
	 * Test method for {@link org.apache.niolex.network.rpc.conf.BaseConfigBean#getGroupName()}.
	 */
	@Test
	public final void testGetGroupName() {
		BaseConfigBean c = new BaseConfigBean("FFF");
		c.setConfig("yet", "true");
		c.setConfig("method", "network");
		assertEquals(2, c.getProp().size());
	}

	@Test
	public void testSetField()
	 throws Exception {
		Head h = new Head("ghgh");
		h.setConfig("kill", "912039298102988");
		assertEquals(912039298102988l, h.getKill());
	}

}
