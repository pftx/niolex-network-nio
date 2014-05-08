/**
 * AddressRegiSerializer.java
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

import org.apache.niolex.commons.codec.StringUtil;
import org.apache.niolex.network.Config;
import org.apache.niolex.network.serialize.BaseSerializer;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-6-27
 */
public class AddressRegiSerializer extends BaseSerializer<AddressRegiBean> {

	/**
	 * Create this AddressRegiSerializer with the given code.
	 * @param code
	 */
	public AddressRegiSerializer(short code) {
		super(code);
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.serialize.BaseSerializer#toBytes(Object)
	 */
	@Override
	public byte[] toBytes(AddressRegiBean t) {
		return StringUtil.strToUtf8Byte(t.getAddressKey() + Config.NAME_FIELD_SEP + t.getAddressValue());
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.serialize.BaseSerializer#toObj(byte[])
	 */
	@Override
	public AddressRegiBean toObj(byte[] arr) {
		String[] arr2 = StringUtil.split(StringUtil.utf8ByteToStr(arr), Config.NAME_FIELD_SEP, false);
		if (arr2.length != 2) {
			throw new IllegalArgumentException("Data is invalid.");
		}
		return new AddressRegiBean(arr2[0], arr2[1]);
	}

}
