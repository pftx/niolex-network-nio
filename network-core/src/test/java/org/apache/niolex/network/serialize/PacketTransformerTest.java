/**
 * PacketTransformerTest.java
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

import static org.junit.Assert.*;

import org.apache.niolex.commons.codec.StringUtil;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.serialize.PacketTransformer;
import org.apache.niolex.network.serialize.StringSerializer;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-5-30
 */
public class PacketTransformerTest {
	PacketTransformer pt;

	/**
	 * Test method for {@link org.apache.niolex.network.serialize.PacketTransformer#getInstance()}.
	 */
	@Before
	public void testGetInstance() {
		pt = PacketTransformer.getInstance();
		pt.addSerializer(new StringSerializer((short)5));
	}

	/**
	 * Test method for {@link org.apache.niolex.network.serialize.PacketTransformer#canHandle(java.lang.Short)}.
	 */
	@Test
	public void testCanHandle() {
		assertFalse(pt.canHandle((short)3));
		assertTrue(pt.canHandle((short)5));
	}

	/**
	 * Test method for {@link org.apache.niolex.network.serialize.PacketTransformer#getDataObject(org.apache.niolex.network.PacketData)}.
	 */
	@Test(expected=IllegalStateException.class)
	public void testGetDataObjectCanNotHandle() {
		PacketData sc = new PacketData(3, new byte[0]);
		pt.getDataObject(sc);
	}

	@Test
	public void testGetDataObject() {
		PacketData sc = new PacketData(5, "mer#getP#价格没有数字".getBytes(StringUtil.UTF_8));
		String c = pt.getDataObject(sc);
		assertEquals("mer#getP#价格没有数字", c);
	}

	/**
     * Test method for {@link org.apache.niolex.network.serialize.PacketTransformer#getPacketData(java.lang.Short, java.lang.Object)}.
     */
    @Test(expected=IllegalStateException.class)
    public void testGetPacketDataCanNotHandle() {
        pt.getPacketData((short)6, "Again, It's more like it.");
    }

	/**
	 * Test method for {@link org.apache.niolex.network.serialize.PacketTransformer#getPacketData(java.lang.Short, java.lang.Object)}.
	 */
	@Test
	public void testGetPacketData() {
	    String s = "原型继承与标识符查找";
		PacketData qc = pt.getPacketData((short)5, s);
		assertEquals(5, qc.getCode());
		assertArrayEquals(s.getBytes(StringUtil.UTF_8), qc.getData());
	}

}
