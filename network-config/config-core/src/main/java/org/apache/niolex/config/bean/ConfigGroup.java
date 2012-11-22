/**
 * ConfigGroup.java
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The config group bean.
 *
 * The data management in config framework is like this:
 *
 * ConfigGroup
 * 		=>	ConfigItem
 * 				=>	RealData
 * We have only two levels management.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-7-3
 */
public class ConfigGroup {

	/**
	 * The group id.
	 */
	private int groupId;

	/**
	 * The group name;
	 */
	private String groupName;

	/**
	 * The update lock.
	 */
	private transient final Lock lock = new ReentrantLock();

	/**
	 * The group config data.
	 */
	private Map<String, ConfigItem> groupData = new HashMap<String, ConfigItem>();

	/**
	 * Update the internal config item.
	 * @param item
	 * @return true if the config item is replaced, false if this parameter is too old.
	 */
	public boolean updateConfigItem(ConfigItem item) {
		lock.lock();
		try {
			ConfigItem tmp = groupData.get(item.getKey());
			if (tmp == null || tmp.getUpdateTime() < item.getUpdateTime()) {
				groupData.put(item.getKey(), item);
				return true;
			}
			return false;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Store the group config into this bean.
	 * @param config
	 * @return the changed item list
	 */
	public List<ConfigItem> replaceConfig(ConfigGroup config) {
		List<ConfigItem> list = new ArrayList<ConfigItem>();
		lock.lock();
		try {
			// We need to update config one by one, to get change list.
			for (ConfigItem item : config.groupData.values()) {
				ConfigItem tmp = groupData.get(item.getKey());
				if (tmp == null || tmp.getUpdateTime() < item.getUpdateTime()) {
					groupData.put(item.getKey(), item);
					list.add(item);
				} else if (!tmp.getValue().equals(item.getValue())
						&& tmp.getUpdateTime() == item.getUpdateTime()) {
					// At the same time, this value updated twice, we update current.
					// This will make all data stay the same with DB.
					groupData.put(item.getKey(), item);
					list.add(item);
				}
			}
		} finally {
			lock.unlock();
		}
		return list;
	}

	//---------------------- GETTER & SETTER ---------------------------------

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public Map<String, ConfigItem> getGroupData() {
		return groupData;
	}

	public void setGroupData(Map<String, ConfigItem> groupData) {
		this.groupData = groupData;
	}

	@Override
	public String toString() {
		return "{" + groupId + ", " + groupName + "}";
	}

}
