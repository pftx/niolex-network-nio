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
	 * Invoke the Default constructor
	 */
	public static final Packet newInstance() {
		return new Packet();
	}

	/**
	 * Create a packet with only packet code.
	 *
	 * @param code
	 */
	public static final Packet newInstance(int code) {
		return new Packet((short) code);
	}

	/**
	 * Create a packet with packet code and String data. String will be encoded as UTF-8
	 *
	 * @param code
	 * @param data
	 */
	public static final Packet newInstance(int code, String data) {
		return new Packet((short) code, StringUtil.strToUtf8Byte(data));
	}

	/**
	 * Create packet by code and data
	 *
	 * @param code
	 * @param data
	 */
	public static final Packet newInstance(int code, byte[] data) {
		return new Packet((short) code, data);
	}

	/**
	 * Generate Data from this Packet into the ByteBuffer.
	 *
	 * @param pc
	 * @param bb
	 * @return true if the generation is finished.
	 */
	public static void putHeader(Packet pc, ByteBuffer bb) {
		bb.putShort(pc.getSerial());
		bb.putShort(pc.getCode());
		bb.putInt(pc.getLength());
	}

	/**
	 * Parse Packet header from ByteBuffer
	 *
	 * @param bb
	 * @return true only if this packet is ready to send.
	 */
	public static Packet parseHeader(ByteBuffer bb) {
		Packet pc = new Packet();
		pc.setSerial(bb.getShort());
		pc.setCode(bb.getShort());
		int length = bb.getInt();

		pc.setLength(length);
		if (length > MAX_SIZE) {
			throw new IllegalStateException("The packet length is larger than the max size: " + length);
		}
		pc.setData(new byte[length]);

		return pc;
	}

}
