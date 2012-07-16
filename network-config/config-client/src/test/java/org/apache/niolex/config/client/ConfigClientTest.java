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


import org.apache.niolex.config.event.ConfigListener;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-7-12
 */
public class ConfigClientTest {

	/**
	 * Test method for {@link org.apache.niolex.config.client.ConfigClient#getConfigGroup(java.lang.String)}.
	 * @throws Throwable
	 */
	@Test
	public void testGetConfigGroup() throws Throwable {
		Configer conf1 = new Configer("testme");
		String s1 = conf1.getString("demo.key");
		System.out.println(s1);
		Configer conf2 = new Configer("configserver.test.demo");
		String s2 = conf2.getString("demo.key");
		System.out.println(s2);

		ConfigListener ls = new ConfigListener() {
			@Override
			public void configChanged(String value, long updateTime) {
				System.out.println("value of key [demo] changed: " + value);

			}};
		conf1.addListener("demo", ls);
		conf2.addListener("demo.str", ls);
		Thread.sleep(300000);
	}

}
