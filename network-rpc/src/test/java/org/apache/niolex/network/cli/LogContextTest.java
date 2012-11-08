/**
 * LogContextTest.java
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
package org.apache.niolex.network.cli;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import junit.framework.Assert;

import org.apache.niolex.network.cli.LogContext;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-5-27
 */
public class LogContextTest {
	@Mock
	private LogContext instance;

	@BeforeClass
	public static void test() {
		Assert.assertEquals("LOGID", LogContext.prefix());
		LogContext.serviceUrl("hdhdhdj");
	}

	@Before
	public void createLogContext() throws Exception {
		LogContext.setInstance(instance);
		when(instance.getLogPrefix()).thenReturn("cdiejl coolmoon");
	}

	/**
	 * Test method for {@link org.apache.niolex.network.cli.LogContext#getLogPrefix()}.
	 */
	@Test
	public final void testGetLogPrefix() {
		assertEquals("cdiejl coolmoon", LogContext.prefix());
	}

	/**
	 * Test method for {@link org.apache.niolex.network.cli.LogContext#setServerUrl(java.lang.String)}.
	 */
	@Test
	public final void testSetServerUrl() {
		LogContext.serviceUrl("http://www.niolex.com.org");
		verify(instance).setServerUrl("http://www.niolex.com.org");
	}

}
