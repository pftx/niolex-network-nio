/**
 * StringSerializer.java
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
package org.apache.niolex.network.packet;

import org.apache.niolex.commons.codec.StringUtil;


/**
 * StringSerializer serialize String.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-5-30
 */
public class StringSerializer extends BaseSerializer<String> {
	private short code;

	/**
	 * Create a StringSerializer for this code.
	 * @param code
	 */
	public StringSerializer(short code) {
		super();
		this.code = code;
	}

	public void setCode(short code) {
		this.code = code;
	}

	/* (non-Javadoc)
	 * @see org.apache.niolex.network.packet.ISerializer#getCode()
	 */
	@Override
	public short getCode() {
		return this.code;
	}

	/* (non-Javadoc)
	 * @see org.apache.niolex.network.packet.BaseSerializer#serObj(java.lang.Object)
	 */
	@Override
	public byte[] serObj(String t) {
		return StringUtil.strToUtf8Byte(t);
	}

	/* (non-Javadoc)
	 * @see org.apache.niolex.network.packet.BaseSerializer#deserObj(byte[])
	 */
	@Override
	public String deserObj(byte[] arr) {
		return StringUtil.utf8ByteToStr(arr);
	}

}
