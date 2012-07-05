/**
 * SyncBean.java
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

import java.util.HashMap;
import java.util.Map;

/**
 * Sync groups with server bean.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-7-4
 */
public class SyncBean {

	/**
	 * The group name;
	 */
	private String groupName;

	/**
	 * The group config data.
	 */
	private Map<String, Long> groupData;

	/**
	 * Create an empty sync bean.
	 */
	public SyncBean() {
		super();
	}

	/**
	 * Create an sync bean to sync this group.
	 * @param group
	 */
	public SyncBean(GroupConfig group) {
		super();
		this.groupName = group.getGroupName();
		this.groupData = new HashMap<String, Long>();

		for (ConfigItem item : group.getGroupData().values()) {
			this.groupData.put(item.getKey(), item.getUpdateTime());
		}
	}

	//---------------------- GETTER & SETTER ---------------------------------

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public Map<String, Long> getGroupData() {
		return groupData;
	}

	public void setGroupData(Map<String, Long> groupData) {
		this.groupData = groupData;
	}

}
