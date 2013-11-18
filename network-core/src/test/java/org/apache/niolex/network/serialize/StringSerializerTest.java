/**
 * StringSerializerTest.java
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
package org.apache.niolex.network.serialize;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-5-30
 */
@RunWith(MockitoJUnitRunner.class)
public class StringSerializerTest {

	private short code = 3;
	private StringSerializer stringSerializer;

	@Before
	public void createStringSerializer() throws Exception {
		stringSerializer = new StringSerializer(code);
	}

	/**
	 * Test method for {@link org.apache.niolex.network.serialize.StringSerializer#getCode()}.
	 */
	@Test
	public void testGetCode() {
		assertEquals(3, stringSerializer.getCode());
		stringSerializer.setCode((short)4);
		assertEquals(4, stringSerializer.getCode());
	}

	/**
	 * Test method for {@link org.apache.niolex.network.serialize.StringSerializer#toBytes(String)}.
	 */
	@Test
	public void testSerObjString() {
		String q = "iolex.network.packet.StringSerializer#serObj(java.l";
		byte[] arr = stringSerializer.obj2Bytes(q);
		String s = (String) stringSerializer.bytes2Obj(arr);
		assertEquals(q, s);
	}

}
