/**
 * AddressListSerializer.java
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
package org.apache.niolex.network.name.bean;

import java.util.Arrays;
import java.util.List;

import org.apache.niolex.commons.codec.StringUtil;
import org.apache.niolex.network.Config;
import org.apache.niolex.network.serialize.BaseSerializer;

/**
 * The class to serialize address list.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-6-21
 */
public class AddressListSerializer extends BaseSerializer<List<String>> {

	/**
	 * Create this AddressListSerializer with the given code.
	 * @param code
	 */
	public AddressListSerializer(short code) {
		super(code);
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.serialize.BaseSerializer#toBytes(Object)
	 */
	@Override
	public byte[] toBytes(List<String> t) {
		String s = StringUtil.join(t, Config.NAME_FIELD_SEP);
		return StringUtil.strToUtf8Byte(s);
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.serialize.BaseSerializer#toObj(byte[])
	 */
	@Override
	public List<String> toObj(byte[] arr) {
		if (arr.length == 0) {
			return Arrays.asList();
		}
		String s = StringUtil.utf8ByteToStr(arr);
		return Arrays.asList(s.split(Config.NAME_FIELD_SEP_REGEX));
	}

}
