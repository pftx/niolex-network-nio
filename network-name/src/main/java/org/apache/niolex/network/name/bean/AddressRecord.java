/**
 * AddressRecord.java
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The class to store the address record.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-21
 */
public class AddressRecord {

	private static final Logger LOG = LoggerFactory.getLogger(AddressRecord.class);

	/**
	 * This record status.
	 */
	private Status status = Status.OK;
	private String addressKey;
	private String addressValue;

	/**
	 * Last status change time.
	 */
	private long lastTime;

	public static enum Status {
		OK, DISCONNECTED, DEL
	}

	public AddressRecord(AddressRegiBean bean) {
		this(bean.getAddressKey(), bean.getAddressValue());
	}

	/**
	 * The full constructor, initialize all fields.
	 *
	 * @param addressKey
	 * @param addressValue
	 */
	public AddressRecord(String addressKey, String addressValue) {
		super();
		this.addressKey = addressKey;
		this.addressValue = addressValue;
		if (addressKey == null || addressValue == null) {
			throw new IllegalArgumentException("addressKey & Value should not be null.");
		}
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
		this.lastTime = System.currentTimeMillis();
		LOG.info(this.toString());
	}

	public String getAddressKey() {
		return addressKey;
	}

	public void setAddressKey(String addressKey) {
		this.addressKey = addressKey;
	}

	public String getAddressValue() {
		return addressValue;
	}

	public void setAddressValue(String addressValue) {
		this.addressValue = addressValue;
	}

	public long getLastTime() {
		return lastTime;
	}

	@Override
	public String toString() {
		return status + ", " + addressKey + ", " + addressValue;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AddressRecord other = (AddressRecord) obj;
		return addressKey.equals(other.addressKey) && addressValue.equals(other.addressValue);
	}

}
