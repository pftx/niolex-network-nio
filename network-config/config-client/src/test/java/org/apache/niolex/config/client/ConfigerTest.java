/**
 * ConfigerTest.java
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

import static org.junit.Assert.*;

import org.apache.niolex.config.event.ConfigListener;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-7-4
 */
public class ConfigerTest {

	/**
	 * Test method for {@link org.apache.niolex.config.client.Configer#Configer(java.lang.String)}.
	 */
	@Test
	public void testConfiger() {
		Configer conf = new Configer("configserver.test.demo");
		String s = conf.getString("demo.key");
		System.out.println(s);
	}

	/**
	 * Test method for {@link org.apache.niolex.config.client.Configer#addListener(java.lang.String, org.apache.niolex.config.event.ConfigListener)}.
	 * @throws InterruptedException
	 */
	@Test
	public void testAddListener() throws InterruptedException {
		Configer conf = new Configer("testme");
		ConfigListener lister = new ConfigListener() {

			@Override
			public void configChanged(String value, long updateTime) {
				System.out.println("value of key [demo] changed: " + value);

			}};
		conf.addListener("demo", lister);
		Thread.sleep(300);
	}

	/**
	 * Test method for {@link org.apache.niolex.config.client.Configer#getProperty(java.lang.String)}.
	 */
	@Test
	public void testGetPropertyString() {
		Configer conf = new Configer("configserver.test.demo");
		String s = conf.getString("demo.str");
		System.out.println(s);
		assertEquals("我们最美丽的祖国啊", s);
	}

	/**
	 * Test method for {@link org.apache.niolex.config.client.Configer#getProperty(java.lang.String)}.
	 */
	@Test
	public void testGetPropertyLong() {
		Configer conf = new Configer("configserver.test.demo");
		long s = conf.getLong("demo.long");
		System.out.println(s);
		assertEquals(123213123123l, s);
	}

	/**
	 * Test method for {@link org.apache.niolex.config.client.Configer#getProperty(java.lang.String)}.
	 */
	@Test
	public void testGetPropertyInt() {
		Configer conf = new Configer("configserver.test.demo");
		int s = conf.getInteger("demo.int");
		System.out.println(s);
		assertEquals(12345, s);
	}

	/**
	 * Test method for {@link org.apache.niolex.config.client.Configer#getProperty(java.lang.String)}.
	 */
	@Test
	public void testGetPropertyBoolean() {
		Configer conf = new Configer("configserver.test.demo");
		boolean s = conf.getBoolean("demo.boolean");
		System.out.println(s);
		assertEquals(true, s);
	}

}
