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
package org.apache.niolex.network.serialize;

import org.apache.niolex.commons.codec.StringUtil;


/**
 * StringSerializer serialize String into byte array and vice-verser using UTF-8.
 * This is just a demo for user to reference.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-5-30
 */
public class StringSerializer extends BaseSerializer<String> {

	/**
	 * Create a StringSerializer for this code.
	 *
	 * @param code
	 */
	public StringSerializer(short code) {
		super(code);
	}

    /**
     * This is the override of super method.
     * @see org.apache.niolex.network.serialize.BaseSerializer#toObj(byte[])
     */
    @Override
    public String toObj(byte[] array) {
        return StringUtil.utf8ByteToStr(array);
    }

    /**
     * This is the override of super method.
     * @see org.apache.niolex.network.serialize.BaseSerializer#toBytes(Object)
     */
    @Override
    public byte[] toBytes(String t) {
        return StringUtil.strToUtf8Byte(t);
    }

}
