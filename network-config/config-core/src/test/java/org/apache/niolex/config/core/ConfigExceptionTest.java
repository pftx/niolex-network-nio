/**
 * ConfigExceptionTest.java
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
package org.apache.niolex.config.core;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-7-11
 */
public class ConfigExceptionTest {

	/**
	 * Test method for {@link org.apache.niolex.config.core.ConfigException#ConfigException(java.lang.String)}.
	 */
	@Test
	public void testConfigExceptionString() {
		ConfigException e = new ConfigException("Not yet implemented");
		assertEquals("Not yet implemented", e.getMessage());
	}

	/**
	 * Test method for {@link org.apache.niolex.config.core.ConfigException#ConfigException(java.lang.String, java.lang.Throwable)}.
	 */
	@Test
	public void testConfigExceptionStringThrowable() {
		Throwable t = new Throwable("Good");
		ConfigException e = new ConfigException("Not yet implemented", t);
		assertEquals("Not yet implemented", e.getMessage());
		assertEquals(t, e.getCause());
	}

}
