/**
 * GroupDao.java
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
package org.apache.niolex.config.dao;

import java.util.List;

import org.apache.niolex.config.bean.ConfigItem;
import org.apache.niolex.config.bean.GroupConfig;

/**
 * Communicate with DB, deal with Group and Config.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-7-5
 */
public interface GroupDao {

	/**
	 * Update this config.
	 * We only update when groupId, key and updateTime are all the same.
	 *
	 * @param item
	 * @return true if update success.
	 */
	public boolean updateConfig(ConfigItem item);

	/**
	 * Add this config.
	 * We only add this item when the groupId and key pair not exist.
	 *
	 * @param item
	 * @return true if added into DB.
	 */
	public boolean addConfig(ConfigItem item);

	/**
	 * Add this group into DB.
	 *
	 * @param groupName
	 * @return true if added into DB.
	 */
	public boolean addGroup(String groupName);

	/**
	 * Get all the config groups from DB.
	 * Notice! Just groups, no config item.
	 * The list must order by groupId.
	 * @return
	 */
	public List<GroupConfig> loadAllGroups();

	/**
	 * Load the current DB time for mark laster update time.
	 * @return
	 */
	public long loadDBTime();

	/**
	 * Load the group config with this group name.
	 * @param groupName
	 * @return null if group not found.
	 */
	public GroupConfig loadGroup(String groupName);
}
