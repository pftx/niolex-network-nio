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
 * @author Xie, Jiyun
 */
public class PacketData extends Packet {
    // The HEART_BEAT Packet is for testing server and client being alive
    private static final PacketData HEART_BEAT = new PacketData((short)0, new byte[0]);
    private static final int MAX_SIZE = Config.SERVER_MAX_PACKET_SIZE;
    // The current handled data position in packet.
    private int dataPos;

    /**
     * Get the default heart beat packet.
     * @return
     */
    public static final PacketData getHeartBeatPacket() {
        return HEART_BEAT;
    }

    /**
     * Default constructor
     */
    public PacketData() {
        super();
        this.version = 1;
    }

    /**
     * Create a packet with only packet code.
     * @param code
     */
    public PacketData(int code) {
    	this(code, new byte[0]);
    }

    /**
     * Create a packet with packet code and String data.
     * String will be encoded as UTF-8
     * @param code
     * @param data
     */
    public PacketData(int code, String data) {
    	this(code, StringUtil.strToUtf8Byte(data));
    }

    /**
     * Create packet by code and data
     * @param code
     * @param data
     */
    public PacketData(int code, byte[] data) {
    	this((short)code, data);
    }

    /**
     * Create packet by code and data
     * @param code
     * @param data
     */
    public PacketData(short code, byte[] data) {
        super();
        this.version = 1;
        this.code = code;
        this.data = data;
        this.length = data.length;
        this.dataPos = 0;
    }

    public PacketData makeCopy() {
    	PacketData other = new PacketData(this.code, this.data);
    	other.reserved = this.reserved;
    	other.version = this.version;
    	return other;
    }


    /**
     * Generate Data from this Packet into the ByteBuffer.
     * @param bb
     * @return true if the generation is finished.
     */
    public boolean generateData(ByteBuffer bb) {
        if (dataPos == 0) {
            bb.put(version);
            bb.put(reserved);
            bb.putShort(code);
            bb.putInt(length);
        }
        int len = bb.remaining();
        boolean r = (len + dataPos) >= length;
        if (r) {
            len = length - dataPos;
        }
        bb.put(data, dataPos, len);
        dataPos += len;
        return r;
    }

    /**
     * Write this Packet into the DataOutputStream.
     * Only return when finished write or IOException
     * @param out
     * @throws IOException
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
     * Parse Packet header from ByteBuffer
     * Parse body when there is any data left in the ByteBuffer.
     * @param bb
     * @return true only if this packet is ready to send.
     */
    public boolean parseHeader(ByteBuffer bb) {
        version = bb.get();
        reserved = bb.get();
        code = bb.getShort();
        length = bb.getInt();

        if (length > MAX_SIZE) {
        	throw new IllegalStateException("The packet length is larger than the max size: " + length);
        }

        data = new byte[length];

        dataPos = 0;
        return parseBody(bb);
    }

    /**
     * Parse Packet from this DataInputStream.
     * Only return when finish parse
     * @param in
     * @throws IOException
     */
    public void parseHeader(DataInputStream in) throws IOException {
        version = in.readByte();
        reserved = in.readByte();
        code = in.readShort();
        length = in.readInt();

        if (length > MAX_SIZE) {
        	throw new IOException("The packet length is larger than the max size: " + length);
        }

        data = new byte[length];

        dataPos = 0;
        parseBody(in);
    }

    /**
     * Parse body from this ByteBuffer
     * @param bb
     * @return true when finished
     */
    public boolean parseBody(ByteBuffer bb) {
        int len = bb.limit() - bb.position();
        boolean r = (len + dataPos) >= length;
        if (r) {
            len = length - dataPos;
        }
        bb.get(data, dataPos, len);
        if (r) {
            dataPos = 0;
        } else {
            dataPos += len;
        }
        return r;
    }

    /**
     * Parse body from this DataInputStream
     * return when finished, or IOException
     * @param in
     * @throws IOException
     */
    public void parseBody(DataInputStream in) throws IOException {
        int count = 0;
        while ((count = in.read(data, dataPos, length - dataPos)) >= 0) {
            if (count + dataPos != length) {
                dataPos += count;
            } else {
                break;
            }
        }
        if (count + dataPos != length) {
            throw new IOException("End of stream found, but packet was not finished.");
        }
    }

	public void setDataPos(int dataPos) {
		this.dataPos = dataPos;
	}

}
