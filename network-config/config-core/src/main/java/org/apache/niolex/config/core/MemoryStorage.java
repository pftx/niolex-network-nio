/**
 * MemoryStorage.java
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
package org.apache.niolex.config.core;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.niolex.config.bean.ConfigItem;
import org.apache.niolex.config.bean.ConfigGroup;


/**
 * Store all the configurations in memory.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-7-3
 */
public class MemoryStorage {

	/**
	 * The total storage.
	 */
	private final ConcurrentHashMap<String, ConfigGroup> mapStorage = new ConcurrentHashMap<String, ConfigGroup>();
	private final ConcurrentHashMap<Integer, String> nameStorage = new ConcurrentHashMap<Integer, String>();

	/**
	 * Store the ConfigGroup into MemoryStorage.
	 * If another ConfigGroup already exist, we will try to replace old config items.
	 *
	 * This method is synchronized.
	 *
	 * @param config
	 * @return the changed item list if this config already exist.
	 */
	public List<ConfigItem> store(ConfigGroup config) {
		ConfigGroup tmp = mapStorage.putIfAbsent(config.getGroupName(), config);
		nameStorage.put(config.getGroupId(), config.getGroupName());
		if (tmp != null) {
			return tmp.replaceConfig(config);
		}
		return null;
	}

	/**
	 * Get group config by the specified group name.
	 *
	 * @param groupName
	 * @return null if group not found.
	 */
	public ConfigGroup get(String groupName) {
		return mapStorage.get(groupName);
	}

	/**
	 * Get all the current stored group config(s).
	 * @return
	 */
	public Collection<ConfigGroup> getAll() {
		return mapStorage.values();
	}

	/**
	 * Get group name by group id.
	 * @param groupId
	 * @return null if group not found.
	 */
	public String findGroupName(int groupId) {
		return nameStorage.get(groupId);
	}

	/**
	 * Update the config item in the specified group.
	 *
	 * @param groupName
	 * @param item
	 * @return true if the specified config item is replaced, false if not.
	 */
	public boolean updateConfigItem(String groupName, ConfigItem item) {
		ConfigGroup tmp = mapStorage.get(groupName);
		if (tmp == null) {
			return false;
		} else {
			return tmp.updateConfigItem(item);
		}
	}
}
