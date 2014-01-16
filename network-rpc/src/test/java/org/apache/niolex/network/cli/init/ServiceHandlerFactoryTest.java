/**
 * RpcClientFactoryTest.java
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

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import org.apache.niolex.network.cli.init.ServiceHandlerBuilder;
import org.apache.niolex.network.cli.init.ServiceHandlerFactory;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-6-20
 */
public class ServiceHandlerFactoryTest extends ServiceHandlerFactory {

	/**
	 * Test method for {@link org.apache.niolex.network.cli.init.ServiceHandlerFactory#getBuilder(java.lang.String)}.
	 */
	@Test
	public void testGetBuilder() {
		ServiceHandlerBuilder factory = mock(ServiceHandlerBuilder.class);
		ServiceHandlerFactory.registerBuilder("network/mock", factory);
		assertEquals(factory, ServiceHandlerFactory.getBuilder("network/mock"));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testGetBuilder2() {
		assertEquals(null, ServiceHandlerFactory.getBuilder("network/mock23"));
	}

}
