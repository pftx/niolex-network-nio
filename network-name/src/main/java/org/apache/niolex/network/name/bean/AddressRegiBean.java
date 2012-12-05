/**
 * AddressRegiBean.java
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


/**
 * Use this bean to register address to name server.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-6-21
 */
public class AddressRegiBean {

	private String addressKey;
	private String addressValue;

	/**
	 * This is only for serializer. Register to code Config.CODE_NAME_PUBLISH
	 */
	public AddressRegiBean() {
		// This is only for serializer.
	}

	/**
	 * The full constructor. Initialize all fields.
	 *
	 * @param addressKey
	 * @param addressValue
	 */
	public AddressRegiBean(String addressKey, String addressValue) {
		super();
		this.addressKey = addressKey;
		this.addressValue = addressValue;
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

}
