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

import org.apache.niolex.commons.event.Event;
import org.apache.niolex.commons.event.Listener;
import org.apache.niolex.commons.test.Counter;
import org.apache.niolex.config.bean.ConfigItem;
import org.apache.niolex.config.client.ConfigClient;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-7-12
 */
public class ConfigEventDispatcherTest extends ConfigClient {

	private static final Listener<ConfigItem> listn = new Listener<ConfigItem>() {

        @Override
        public void eventHappened(Event<ConfigItem> e) {
            ConfigItem i = e.getEventValue();
            System.out.println("Changed value new " + i.getValue() + ", " + i.getUpdateTime());
            c.inc();
        }};

	private static final Counter c = new Counter();

	/**
	 * Test method for {@link org.apache.niolex.config.event.ConfigEventDispatcher#fireEvent(org.apache.niolex.config.bean.ConfigItem)}.
	 */
	@Test
	public void testFireEvent() {
	    registerEventHandler("abc", "God lick", listn);
		ConfigItem item = new ConfigItem();
		item.setKey("God lick");
		item.setValue("Very Simple.");
		item.setUpdateTime(123456789);
		fireEvent("abc", item);
		assertEquals(1, c.cnt());
		registerEventHandler("abc", "God lick", listn);
		fireEvent("abc", item);
		assertEquals(3, c.cnt());
	}

}
