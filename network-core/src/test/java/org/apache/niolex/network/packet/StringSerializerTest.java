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
package org.apache.niolex.network.packet;

import static org.junit.Assert.assertEquals;

import org.apache.niolex.network.PacketData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-5-30
 */
public class StringSerializerTest {

	private short code = 3;
	private StringSerializer stringSerializer;

	@Before
	public void createStringSerializer() throws Exception {
		stringSerializer = new StringSerializer(code);
	}

	/**
	 * Test method for {@link org.apache.niolex.network.packet.StringSerializer#getCode()}.
	 */
	@Test
	public void testGetCode() {
		assertEquals(3, stringSerializer.getCode());
		stringSerializer.setCode((short)4);
		assertEquals(4, stringSerializer.getCode());
	}

	/**
	 * Test method for {@link org.apache.niolex.network.packet.StringSerializer#serObj(java.lang.String)}.
	 */
	@Test
	public void testSerObjString() {
		String q = "iolex.network.packet.StringSerializer#serObj(java.l";
		byte[] arr = stringSerializer.serObj(q);
		String s = stringSerializer.deserObj(arr);
		assertEquals(q, s);
	}

	@Test
	public void testSerObj() {
		String q = "iolex.network.packet.StringSerializer#serObj(java.l";
		byte[] arr = stringSerializer.serObj(q);
		PacketData sc = stringSerializer.obj2Data((short)4, arr);
		String s = stringSerializer.data2Obj(sc);
		assertEquals(q, s);
	}
}
