/**
 * BasePacketWriterTest.java
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
package org.apache.niolex.network;

import static org.junit.Assert.assertEquals;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-5-30
 */
public class BasePacketWriterTest {
	BasePacketWriter bpw;

	@Before
	public void setup() {
		bpw = new BasePacketWriter () {

			@Override
			public String getRemoteName() {
				return null;
			}

			@Override
			public Collection<PacketData> getRemainPackets() {
				return null;
			}};
	}

	/**
	 * Test method for {@link org.apache.niolex.network.BasePacketWriter#handleWrite(org.apache.niolex.network.PacketData)}.
	 */
	@Test
	public void testHandleWrite() {
		bpw.handleWrite(null);
	}

	/**
	 * Test method for {@link org.apache.niolex.network.BasePacketWriter#attachData(java.lang.String, java.lang.Object)}.
	 */
	@Test
	public void testAttachData() {
		bpw.attachData("IDIJF", "Not yet implemented");
		assertEquals("Not yet implemented", bpw.getAttached("IDIJF"));
	}

}
