/**
 * GroupService.java
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
package org.apache.niolex.config.service;

import java.util.List;

import org.apache.niolex.config.bean.ConfigItem;
import org.apache.niolex.config.bean.GroupConfig;
import org.apache.niolex.config.bean.SyncBean;
import org.apache.niolex.network.IPacketWriter;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-7-5
 */
public interface GroupService {

	/**
	 * Try to subscribe this group.
	 *
	 * @param groupName
	 * @param wt
	 * @return
	 */
	public boolean subscribeGroup(String groupName, IPacketWriter wt);

	/**
	 * Try to synchronize with server central storage.
	 *
	 * @param bean
	 * @param wt
	 */
	public void syncGroup(SyncBean bean, IPacketWriter wt);

	/**
	 * Load all config groups from DB.
	 * @return
	 */
	public List<GroupConfig> loadAllGroups();

	/**
	 * Update this diff packet into memory storage.
	 * @param diff
	 */
	public void handleDiff(ConfigItem diff);

	/**
	 * Load group with this group name into memory storage.
	 * @param groupName
	 */
	public void loadGroup(String groupName);

}
