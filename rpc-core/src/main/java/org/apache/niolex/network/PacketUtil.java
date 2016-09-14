/**
 * PacketUtil.java
 *
 * Copyright 2011 Niolex, Inc.
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

import java.nio.ByteBuffer;

import org.apache.niolex.commons.codec.StringUtil;

/**
 * The helper class of Packet, handle reads and writes of Packet.
 *
 * @author Xie, Jiyun
 */
public class PacketUtil {
	private static final int MAX_SIZE = Config.SERVER_MAX_PACKET_SIZE;

	/**
     * Invoke the default constructor to create a new packet.
     */
	public static final Packet newInstance() {
		return new Packet();
	}

	/**
     * Create a packet with only packet code.
     *
     * @param code the packet code
     */
	public static final Packet newInstance(int code) {
		return new Packet((short) code);
	}

	/**
     * Create a packet with packet code and String data. String will be encoded with UTF-8.
     *
     * @param code the packet code
     * @param data the packet data, encoded with UTF8
     */
	public static final Packet newInstance(int code, String data) {
		return new Packet((short) code, StringUtil.strToUtf8Byte(data));
	}

	/**
     * Create packet by code and data, data must not be null.
     *
     * @param code the packet code
     * @param data the packet data
     */
	public static final Packet newInstance(int code, byte[] data) {
		return new Packet((short) code, data);
	}

	/**
     * Generate Header Data from this Packet into the ByteBuffer.
     * We will only put header information into buffer and will not
     * reset buffer in here. So please make sure the buffer is not full.
     * The header is 8 bytes.
     *
     * @param pc the packet used to generate header
     * @param bb the byte buffer used to put data
     */
	public static void putHeader(Packet pc, ByteBuffer bb) {
		bb.putShort(pc.getSerial());
		bb.putShort(pc.getCode());
		bb.putInt(pc.getLength());
	}

	/**
     * Parse Packet header from ByteBuffer.
     * We will create a new Packet with the header information parsed from
     * the buffer. We will create a byte array and set it into the created
     * Packet for put data. So you can just use it to read packet body.
     *
     * @param bb the byte buffer used to read data
     * @throws IllegalStateException if packet is larger than 10MB
     * @return the packet created
     */
	public static Packet parseHeader(ByteBuffer bb) {
		Packet pc = new Packet();
		pc.setSerial(bb.getShort());
		pc.setCode(bb.getShort());
		int length = bb.getInt();

		if (length > MAX_SIZE) {
			throw new IllegalStateException("The packet length is larger than the max size: " + length);
		}
		pc.setData(new byte[length]);

		return pc;
	}

}
