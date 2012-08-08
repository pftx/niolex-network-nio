/**
 * ConfigEventDispatcherTest.java
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
package org.apache.niolex.config.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.apache.niolex.commons.test.Counter;
import org.apache.niolex.config.bean.ConfigItem;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-7-12
 */
public class ConfigEventDispatcherTest {

	private static final ConfigEventDispatcher dispather = new ConfigEventDispatcher();

	private static final ConfigListener listn = new ConfigListener() {

		@Override
		public void configChanged(String value, long updateTime) {
			System.out.println("Changed noew " + value + ", " + updateTime);
			c.inc();
		}};

	private static final Counter c = new Counter();

	/**
	 * Test method for {@link org.apache.niolex.config.event.ConfigEventDispatcher#addListener(java.lang.String, org.apache.niolex.config.event.ConfigListener)}.
	 */
	@Test
	public void testAddListener() {
		ConfigListener lsit = new ConfigListener() {

			@Override
			public void configChanged(String value, long updateTime) {
				System.out.println("Changed lsit " + value + ", " + updateTime);
				c.inc();c.inc();
			}};
		ConfigListener lsit2 = dispather.addListener("God lick", lsit);
		assertNull(lsit2);
	}

	/**
	 * Test method for {@link org.apache.niolex.config.event.ConfigEventDispatcher#removeListener(java.lang.String, org.apache.niolex.config.event.ConfigListener)}.
	 */
	@Test
	public void testRemoveListener() {
		boolean b = dispather.removeListener("God lick", listn);
		assertFalse(b);
	}

	/**
	 * Test method for {@link org.apache.niolex.config.event.ConfigEventDispatcher#fireEvent(org.apache.niolex.config.bean.ConfigItem)}.
	 */
	@Test
	public void testFireEvent() {
		ConfigItem item = new ConfigItem();
		item.setKey("God lick");
		item.setValue("Very Simple.");
		item.setUpdateTime(123456789);
		dispather.fireEvent(item);
		assertEquals(2, c.cnt());
		ConfigListener lsit2 = dispather.addListener("God lick", listn);
		assertNotNull(lsit2);
		dispather.fireEvent(item);
		assertEquals(3, c.cnt());
	}

}
