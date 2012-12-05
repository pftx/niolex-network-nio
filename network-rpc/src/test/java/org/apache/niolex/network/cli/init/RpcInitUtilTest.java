/**
 * RpcInitUtilTest.java
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
package org.apache.niolex.network.cli.init;

import static org.mockito.Mockito.mock;

import org.apache.niolex.network.cli.bui.JsonRpcBuilder;
import org.apache.niolex.network.cli.conf.RpcConfigBean;
import org.apache.niolex.network.cli.init.ServiceHandlerBuilder;
import org.apache.niolex.network.cli.init.ServiceHandlerFactory;
import org.apache.niolex.network.cli.init.RpcInitUtil;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-6-5
 */
public class RpcInitUtilTest {

	/**
	 * Test method for {@link org.apache.niolex.network.cli.init.RpcInitUtil#buildProxy(org.apache.niolex.network.cli.conf.RpcConfigBean, org.apache.niolex.network.cli.init.ServiceHandlerFactory)}.
	 */
	@SuppressWarnings("static-access")
	@Test(expected=IllegalStateException.class)
	public void testBuildProxy() {
		RpcInitUtil u = new RpcInitUtil();
		ServiceHandlerBuilder factory = mock(ServiceHandlerBuilder.class);
		ServiceHandlerFactory.registerBuilder("network/json", factory);
		RpcConfigBean conf = new RpcConfigBean("d");
		conf.serverList = new String[0];
		u.buildProxy(conf);
	}

	/**
	 * Test method for {@link org.apache.niolex.network.cli.init.RpcInitUtil#buildProxy(org.apache.niolex.network.cli.conf.RpcConfigBean, org.apache.niolex.network.cli.init.ServiceHandlerFactory)}.
	 */
	@SuppressWarnings("static-access")
	@Test(expected=IllegalStateException.class)
	public void testBuildProxy2() {
		RpcInitUtil u = new RpcInitUtil();
		ServiceHandlerFactory.registerBuilder("network/json", new JsonRpcBuilder());
		RpcConfigBean conf = new RpcConfigBean("d");
		conf.serverList = new String[1];
		conf.serverList[0] = "locidd:3022df";
		u.buildProxy(conf);
	}

}
