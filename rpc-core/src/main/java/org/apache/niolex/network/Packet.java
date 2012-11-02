/**
 * Packet.java
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


/**
 * The basic data structure of this framework.
 *
 * @author Xie, Jiyun
 *
 */
public class Packet {
	protected short serial;
	protected short code;
	protected int length;
	protected byte[] data;
	private String desc;

	/**
	 * Create an empty Packet, you need to use setter to put values in.
	 *
	 * Constructor
	 */
	public Packet() {
		super();
	}

	/**
	 * Create an valid Packet with length set to 0 and code set to this one.
	 *
	 * Constructor
	 * @param code
	 * @return
	 */
	public Packet(short code) {
		super();
		this.code = code;
		this.data = new byte[0];
	}

	/**
	 * Create a full Packet with this code and data.
	 *
	 * Constructor
	 * @param code
	 * @param d
	 */
	public Packet(short code, byte[] d) {
		super();
		this.code = code;
		this.data = d;
		this.length = d.length;
	}

	/**
	 * Get the hex descriptor of this packet, including serial and code.
	 *
	 * @return the hex string
	 */
	public String descriptor() {
		if (desc == null) {
			desc = Integer.toHexString((serial << 16) + code);
		}
		return desc;
	}

	/**
	 * Make a copy of this Packet.
	 *
	 * @return the copy
	 */
    public final Packet makeCopy() {
    	final Packet other = new Packet(this.code, this.data);
    	other.serial = this.serial;
    	return other;
    }


    /**
     * Write this Packet into the DataOutputStream.
     * This method will block.
     *
     * Only return when finished write or IOException
     * @param out
     * @throws IOException
     */
    public void writeObject(DataOutputStream out) throws IOException {
        out.writeShort(serial);
        out.writeShort(code);
        out.writeInt(length);
        out.write(data);
        out.flush();
    }

    /**
     * Read Packet from this DataInputStream.
     * Only return when parse finished.
     *
     * @param in
     * @throws IOException
     */
    public void readObject(DataInputStream in) throws IOException {
        serial = in.readShort();
        code = in.readShort();
        length = in.readInt();
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

	/**
	 * @return the serial
	 */
	public short getSerial() {
		return serial;
	}

	/**
	 * @param serial
	 *            the serial to set
	 */
	public void setSerial(short serial) {
		this.serial = serial;
	}

	/**
	 * @return the code
	 */
	public short getCode() {
		return code;
	}

	/**
	 * @param code
	 *            the code to set
	 */
	public void setCode(short code) {
		this.code = code;
	}

	/**
	 * @return the length
	 */
	public int getLength() {
		return length;
	}

	/**
	 * @param length
	 *            the length to set
	 */
	public void setLength(int length) {
		this.length = length;
	}

	/**
	 * @return the data
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * @param data
	 *            the data to set
	 */
	public void setData(byte[] data) {
		this.data = data;
	}

}
