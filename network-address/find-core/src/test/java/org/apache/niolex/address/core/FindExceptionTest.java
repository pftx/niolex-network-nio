/**
 * FindExceptionTest.java
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
package org.apache.niolex.address.core;

import static org.junit.Assert.*;

import org.apache.niolex.address.core.FindException;
import org.apache.niolex.address.core.FindException.Code;
import org.junit.Test;


/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-25
 */
public class FindExceptionTest {

	/**
	 * Test method for {@link org.apache.niolex.find.core.FindException#FindException(java.lang.String, java.lang.Throwable)}.
	 */
	@Test
	public void testFindExceptionStringThrowable() {
		FindException e = FindException.makeInstance("Not yet implemented", null);
		System.out.println(e.toString());
		assertEquals(e.getCode(), Code.OTHER);
		CoreTest.LOG.error("Error with two msg: {}, {}.", "this is good", "this is bad", e);
	}

	/**
	 * Test method for {@link org.apache.niolex.find.core.FindException#FindException(java.lang.String)}.
	 */
	@Test
	public void testFindExceptionString() {
		FindException e = new FindException("Not yet asdf", Code.DISCONNECTED);
		System.out.println(e.toString());
		assertEquals(e.getCode(), Code.DISCONNECTED);
		assertEquals("DISCONNECTED Not yet asdf", e.getMessage());
	}

}
