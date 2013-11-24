/**
 * AddressRecordSerializer.java
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
import org.apache.niolex.network.name.bean.AddressRecord.Status;
import org.apache.niolex.network.serialize.BaseSerializer;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-6-26
 */
public class AddressRecordSerializer extends BaseSerializer<AddressRecord> {

	/**
	 * Create this AddressListSerializer with the given code.
	 * @param code
	 */
	public AddressRecordSerializer(short code) {
		super(code);
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.serialize.BaseSerializer#toBytes(Object)
	 */
	@Override
	public byte[] toBytes(AddressRecord t) {
		String[] arr = new String[] {t.getStatus().name(), t.getAddressKey(), t.getAddressValue()};
		return StringUtil.strToUtf8Byte(StringUtil.join(arr , Config.NAME_FIELD_SEP));
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.serialize.BaseSerializer#toObj(byte[])
	 */
	@Override
	public AddressRecord toObj(byte[] arr) {
		String[] arr2 = StringUtil.utf8ByteToStr(arr).split(Config.NAME_FIELD_SEP_REGEX, 4);
		if (arr2.length != 3) {
			throw new IllegalArgumentException("Data is invalid.");
		}
		AddressRecord rec = new AddressRecord(arr2[1], arr2[2]);
		rec.setStatus(Status.valueOf(arr2[0]));
		return rec;
	}

}
