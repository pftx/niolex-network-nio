/**
 * RpcConfigBeanTest.java
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
package org.apache.niolex.network.cli.conf;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-11-9
 */
public class RpcConfigBeanTest {

	private RpcConfigBean conf = new RpcConfigBean("implemented");

	/**
	 * Test method for {@link org.apache.niolex.network.cli.conf.RpcConfigBean#setConfig(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testSetConfigComma() {
		conf.setConfig("serverList", "abc,d ,ef ,  g");
		assertEquals(4, conf.serverList.length);
		assertEquals("ef", conf.serverList[2]);
	}

	@Test
	public void testSetConfigSemicolon() {
		conf.setConfig("serverList", "abc;d ,ef ;  ghk  ");
		assertEquals(4, conf.serverList.length);
		assertEquals("ef", conf.serverList[2]);
	}

	@Test
	public void testSetConfigOther() {
		conf.setConfig("rpcSleepBetweenRetry", "1280");
		conf.setConfig("connectRetryTimes", "6");
		assertEquals(1280, conf.rpcSleepBetweenRetry);
		assertEquals(6, conf.connectRetryTimes);
	}

	/**
	 * Test method for {@link org.apache.niolex.network.cli.conf.RpcConfigBean#setSuper(org.apache.niolex.network.cli.conf.BaseConfigBean)}.
	 */
	@Test
	public void testSetSuperRpcConfigBean() {
		RpcConfigBean bean = new RpcConfigBean("Super");
		bean.serviceType = "json/rpc";
		conf.setSuper(bean);
		assertEquals("json/rpc", conf.serviceType);
	}

	@Test
	public void testSetSuperBaseConfigBean() {
		BaseConfigBean bean = new BaseConfigBean("Super");
		bean.header.put("a", "aka");
		conf.setSuper(bean);
		assertEquals("aka", conf.header.get("a"));
	}

	/**
	 * Test method for {@link org.apache.niolex.network.cli.conf.RpcConfigBean#toString()}.
	 */
	@Test
	public void testToString() {
		conf.header.put("a", "aka");
		conf.prop.put("prop", "propme");
		conf.prop.put("cook", "good");
		System.out.println(conf);
	}

}
