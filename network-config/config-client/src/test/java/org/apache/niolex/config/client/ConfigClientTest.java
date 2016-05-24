/**
 * ConfigClientTest.java
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
package org.apache.niolex.config.client;

import org.apache.niolex.commons.event.Event;
import org.apache.niolex.commons.event.Listener;
import org.apache.niolex.config.bean.ConfigItem;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-7-12
 */
public class ConfigClientTest {

	/**
	 * Test method for {@link org.apache.niolex.config.client.ConfigClient#getConfigGroup(java.lang.String)}.
	 *
	 * @throws Throwable
	 */
	@Test
	public void testGetConfigGroup() throws Throwable {
		// 获取配置组
		Configer conf1 = new Configer("testme");
		// 获取配置
		String s1 = conf1.getString("demo.key");
		System.out.println(s1);
	}

	public static void main(String[] args) throws Throwable {
	    Listener<ConfigItem> ls = new Listener<ConfigItem>() {
	        @Override
	        public void eventHappened(Event<ConfigItem> e) {
	            ConfigItem i = e.getEventValue();
	            System.out.println("value of key [" + i.getKey() + "] changed: " + i.getValue() + ", " + i.getUpdateTime());
	        }
	    };
		// 获取配置组
		Configer conf1 = new Configer("testme");
		// 添加监听器
		conf1.addListener("demo", ls);

		Configer conf2 = new Configer("configserver.test.demo");
		conf2.addListener("demo.key", ls);
		
		Thread.sleep(1000000);
	}

}
