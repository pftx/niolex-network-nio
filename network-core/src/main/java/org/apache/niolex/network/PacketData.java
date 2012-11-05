/**
 * PacketClient.java
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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.niolex.commons.codec.StringUtil;

/**
 * The helper class of Packet, handle reads and writes of Packet.
 * User can use this class directly.
 *
 * @author Xie, Jiyun
 */
public class PacketData extends Packet {
    // The HEART_BEAT Packet is for test the connectivity between server and client
    private static final PacketData HEART_BEAT = new PacketData((short)0, new byte[0]);
    private static final int MAX_SIZE = Config.SERVER_MAX_PACKET_SIZE;

    /**
     * Get the default heart beat packet.
     * @return
     */
    public static final PacketData getHeartBeatPacket() {
        return HEART_BEAT;
    }

    /**
     * Default constructor
     * version is set to 1.
     * All other fields are not set, set them before use the packet.
     */
    public PacketData() {
        super();
        this.version = 1;
    }

    /**
     * Create a packet with only packet code.
     * packet data will be set to an array of 0 length.
     *
     * @param code
     */
    public PacketData(int code) {
    	this(code, new byte[0]);
    }

    /**
     * Create a packet with packet code and String data.
     * String will be encoded as UTF-8.
     *
     * @param code
     * @param data
     */
    public PacketData(int code, String data) {
    	this(code, StringUtil.strToUtf8Byte(data));
    }

    /**
     * Create packet by code and data
     *
     * @param code
     * @param data
     */
    public PacketData(int code, byte[] data) {
    	this((short)code, data);
    }

    /**
     * Create packet by code and data
     *
     * @param code
     * @param data
     */
    public PacketData(short code, byte[] data) {
        super();
        this.version = 1;
        this.code = code;
        this.data = data;
        this.length = data.length;
    }

    /**
     * Make a copy of this packet. All fields will be copied.
     *
     * @return the copy.
     */
    public PacketData makeCopy() {
    	PacketData other = new PacketData(this.code, this.data);
    	other.reserved = this.reserved;
    	other.version = this.version;
    	return other;
    }


    /**
     * Generate Data from this Packet into the ByteBuffer.
     * Please make sure there are at least 8 bytes left in the buffer.
     *
     * @param bb
     */
    public void putHeader(ByteBuffer bb) {
        bb.put(version);
        bb.put(reserved);
        bb.putShort(code);
        bb.putInt(length);
    }

    /**
     * Write this Packet into the DataOutputStream.
     * Only return when finished write or IOException
     *
     * @param out
     * @throws IOException For any I/O error occurs.
     */
    public void generateData(DataOutputStream out) throws IOException {
        out.writeByte(version);
        out.writeByte(reserved);
        out.writeShort(code);
        out.writeInt(length);
        out.write(data);
        out.flush();
    }

    /**
     * Parse Packet header from ByteBuffer.
     * We will create the data array for you to put in the packet content.
     *
     * @param bb
     * @throws IllegalStateException If packet is too large
     */
    public void parseHeader(ByteBuffer bb) {
        version = bb.get();
        reserved = bb.get();
        code = bb.getShort();
        length = bb.getInt();

        if (length > MAX_SIZE) {
        	throw new IllegalStateException("The packet length is larger than the max size: " + length);
        }

        data = new byte[length];
    }

    /**
     * Parse Packet from this DataInputStream.
     * Only return when finish parse
     *
     * @param in
     * @throws IOException For any I/O error occurs.
     * @throws IllegalStateException If packet is too large
     */
    public void parsePacket(DataInputStream in) throws IOException {
        version = in.readByte();
        reserved = in.readByte();
        code = in.readShort();
        length = in.readInt();

        if (length > MAX_SIZE) {
        	throw new IOException("The packet length is larger than the max size: " + length);
        }

        data = new byte[length];

        int dataPos = 0;
        int count = 0;
        while ((count = in.read(data, dataPos, length - dataPos)) >= 0) {
        	dataPos += count;
            if (dataPos == length) {
                break;
            }
        }
        if (dataPos != length) {
            throw new IOException("End of stream found, but packet was not finished.");
        }
    }

}
