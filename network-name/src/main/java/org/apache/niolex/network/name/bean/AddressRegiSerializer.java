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
import org.apache.niolex.network.packet.BaseSerializer;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-6-27
 */
public class AddressRegiSerializer extends BaseSerializer<AddressRegiBean> {

	/**
	 * The packet code.
	 */
	private short code;

	/**
	 * Create this AddressListSerializer with the given code.
	 * @param code
	 */
	public AddressRegiSerializer(short code) {
		super();
		this.code = code;
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.packet.ISerializer#getCode()
	 */
	@Override
	public short getCode() {
		return code;
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.packet.BaseSerializer#serObj(java.lang.Object)
	 */
	@Override
	public byte[] serObj(AddressRegiBean t) {
		return StringUtil.strToUtf8Byte(t.getAddressKey() + Config.NAME_FIELD_SEP + t.getAddressValue());
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.packet.BaseSerializer#deserObj(byte[])
	 */
	@Override
	public AddressRegiBean deserObj(byte[] arr) {
		String[] arr2 = StringUtil.utf8ByteToStr(arr).split(Config.NAME_FIELD_SEP_REGEX, 2);
		if (arr2.length != 2) {
			throw new IllegalArgumentException("Data is invalid.");
		}
		return new AddressRegiBean(arr2[0], arr2[1]);
	}

}
