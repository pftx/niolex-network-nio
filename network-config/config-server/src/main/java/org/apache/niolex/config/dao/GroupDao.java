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

import org.apache.niolex.config.bean.ConfigGroup;

/**
 * Communicate with DB, deal with Group and Config.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-7-5
 */
public interface GroupDao {

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
	public List<ConfigGroup> loadAllGroups();

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
	public ConfigGroup loadGroup(String groupName);
}
