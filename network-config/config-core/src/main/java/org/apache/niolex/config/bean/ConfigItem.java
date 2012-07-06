/**
 * ConfigItem.java
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
package org.apache.niolex.config.bean;

/**
 * The configuration item.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-7-3
 */
public class ConfigItem {

	/**
	 * The group id this config belong to.
	 */
	private int groupId;

	/**
	 * This config key.
	 */
	private String key;

	/**
	 * The config value.
	 */
	private String value;

	/**
	 * The creator of this key.
	 */
	private int cUid;

	/**
	 * Last updater of this key.
	 */
	private int uUid;

	/**
	 * Last update time.
	 */
	private long updateTime;

	//---------------------- GETTER & SETTER ---------------------------------

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public int getcUid() {
		return cUid;
	}

	public void setcUid(int cUid) {
		this.cUid = cUid;
	}

	public int getuUid() {
		return uUid;
	}

	public void setuUid(int uUid) {
		this.uUid = uUid;
	}

	public long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[groupId=").append(groupId).append(", key=").append(key).append(", value=")
				.append(value).append(", cUid=").append(cUid).append(", uUid=").append(uUid).append(", updateTime=")
				.append(updateTime).append("]");
		return builder.toString();
	}

}
