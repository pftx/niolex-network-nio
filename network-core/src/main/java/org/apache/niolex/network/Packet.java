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
 * @author Xie, Jiyun
 *
 */
public class Packet {

    protected byte version;
    protected byte reserved;
    protected short code;
    protected int length;
    protected byte[] data;
    private String desc;

    /**
     * Create an empty Packet
     */
    public Packet() {
        super();
    }

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
