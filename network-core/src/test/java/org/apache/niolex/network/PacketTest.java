/**
 * PacketTest.java
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

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-6-4
 */
public class PacketTest {

	@Test
	public void testDescriptor() {
		PacketData p = PacketData.getHeartBeatPacket();
		PacketData o = p.clone();
		assertEquals(0, o.getCode());
		assertEquals("00000100", p.descriptor());
	}


    /**
     * Test method for {@link org.apache.niolex.network.Packet#getVersion()}.
     */
	@Test
    public void testGetVersion() {
        PacketData p = PacketData.getHeartBeatPacket();
        assertEquals(1, p.getVersion());
	}


	/**
	 * Test method for {@link org.apache.niolex.network.Packet#setVersion(byte)}.
	 */
	@Test
	public void testSetVersion() {
	    PacketData p = new PacketData(56);
	    assertEquals(56, p.getCode());
	    p.setVersion((byte) 64);
	    assertEquals(64, p.getVersion());
	}

	/**
	 * Test method for {@link org.apache.niolex.network.Packet#getReserved()}.
	 */
	@Test
	public void testGetReserved() {
	    PacketData p = new PacketData();
	    p.setReserved((byte) 33);
	    assertEquals(33, p.getReserved());
	}

	@Test
	public void testSetReserved() {
	    PacketData pc = new PacketData(34);
	    assertEquals(34, pc.getCode());
	    assertEquals("00220100", pc.descriptor());
	    pc.setCode((short) 66);
	    assertEquals("00220100", pc.descriptor());
	}

	@Test
	public void testGetCode() {
	    PacketData pc = new PacketData(34, "setReserved(byte)");
	    assertEquals(34, pc.getCode());
	    assertEquals(17, pc.getLength());
	}


    @Test
    public void testSetLength() throws Exception {
        PacketData pc = new PacketData();
        pc.setLength(563);
        assertEquals(563, pc.getLength());
    }

    @Test
    public void testGetData() throws Exception {
        byte[] arr = "not yet implemented".getBytes();
        PacketData pc = new PacketData((short) 56, new byte[0]);
        pc.setData(arr);
        assertArrayEquals(arr, pc.getData());
    }

}
