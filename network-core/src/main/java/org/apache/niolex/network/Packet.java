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

import org.apache.niolex.commons.codec.Base16Util;

/**
 * The basic data structure of this framework.
 * Use {@link PacketData} instead in user application.
 *
 * @author Xie, Jiyun
 *
 */
public class Packet {

	/**
	 * The current version number of this packet, can be used for serial number
	 * or magic code or something like that.
	 */
    protected byte version;

    /**
     * Not used for now. Can be used for serial number
	 * or magic code or something like that.
     */
    protected byte reserved;

    /**
     * The packet code, server use this to judge the content of this packet.
     */
    protected short code;

    /**
     * The packet data length.
     */
    protected int length;

    /**
     * The real packet content.
     */
    protected byte[] data;

    /**
     * The string format packet description, encoded by code, version and reserved.
     * This field make the packet magic thing human readable.
     * @see #descriptor()
     */
    private String desc;

    /**
     * Create an empty Packet.
     * All fields are not set, set them before use the packet.
     */
    public Packet() {
        super();
    }

    /**
     * Get The string format packet description, encoded by code, version and reserved.
     * Format: code[2B] version[1B] reserved[1B] to base16 integer format.
     * i.e.		3A456B12
     * This method will cache the result. So if you change the header after call this
     * method, the string will not change accordingly.
     *
     * @return the descriptor
     */
    public String descriptor() {
    	if (desc == null) {
	    	byte[] bytes = new byte[4];
	    	bytes[0] = (byte)(code >> 8);
	    	bytes[1] = (byte)code;
	    	bytes[2] = version;
	    	bytes[3] = reserved;
	    	desc = Base16Util.byteToBase16(bytes);
    	}
    	return desc;
    }

    /**
     * @return the version
     */
    public byte getVersion() {
        return version;
    }

    /**
     * @param version
     *            the version to set
     */
    public void setVersion(byte version) {
        this.version = version;
    }

    /**
     * @return the reserved
     */
    public byte getReserved() {
        return reserved;
    }

    /**
     * @param reserved
     *            the reserved to set
     */
    public void setReserved(byte reserved) {
        this.reserved = reserved;
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
