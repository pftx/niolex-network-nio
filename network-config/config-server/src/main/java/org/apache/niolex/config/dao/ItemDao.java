/**
 * ItemDao.java
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

/**
 * Communicate with DB, deal with config item operations.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-7-5
 */
public interface ItemDao {


	/**
	 * Get all the config items from DB.
	 * Notice! Only load items older than the specified time.
	 * The list must order by groupId.
	 *
	 * @return the updated config items after the startTime
	 */
	public List<ConfigItem> loadAllConfigItems(long startTime);

	/**
	 * Load all the config items for this group.
	 *
	 * @param groupId
	 * @return all the config items for this group
	 */
	public List<ConfigItem> loadGroupItems(int groupId);

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
	 * Get the config item with this groupId and key.
	 *
	 * @param groupId
	 * @param key
	 * @return null if not found.
	 */
	public ConfigItem getConfig(int groupId, String key);
}
