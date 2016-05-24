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


import org.apache.niolex.config.bean.ConfigItem;
import org.apache.niolex.config.bean.SyncBean;
import org.apache.niolex.network.IPacketWriter;

/**
 * Group Config Operation Service.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-7-5
 */
public interface GroupService {

	/**
	 * Try to subscribe this group.
	 *
	 * @param groupName the group name
	 * @param wt the packet writer
	 * @return true if success, false otherwise. Detailed information will be send to
	 * <code>wt</code> directly.
	 */
	public boolean cliSubscribeGroup(String groupName, IPacketWriter wt);

	/**
	 * Try to synchronize client status with server central storage.
	 *
	 * @param bean the sync bean
	 * @param wt the packet writer
	 */
	public void cliSyncGroup(SyncBean bean, IPacketWriter wt);

	/**
	 * Load all config groups from DB and sync them with this server memory.
	 */
	public void syncAllGroupsWithDB();

	/**
	 * Update this diff packet from other server into memory storage.
	 *
	 * @param diff the diff config item
	 */
	public void svrSendDiff(ConfigItem diff);

	/**
	 * Handle the group add request from other server.
	 *
	 * @param groupName the group name
	 */
	public void svrSendGroup(String groupName);

	/**
	 * Refresh this group data with DB, send changes to other server as well.
	 *
	 * @param groupName the group name
	 * @return the detailed string about the status of refresh group
	 */
	public String adminRefreshGroup(String groupName);

	/**
	 * Add a new config group with this name.
	 *
	 * @param groupName the group name
	 * @param wt the packet writer
	 * @return the detailed string about the status of add group
	 */
	public String addGroup(String groupName, IPacketWriter wt);

	/**
	 * Add a new config item.
	 *
	 * @param item the config item
	 * @param wt the packet writer
	 * @return the detailed string about the status of add config item
	 */
	public String addItem(ConfigItem item, IPacketWriter wt);

	/**
	 * Update an existing config item.
	 *
	 * @param item the config item
	 * @param wt the packet writer
	 * @return the detailed string about the status of update config item
	 */
	public String updateItem(ConfigItem item, IPacketWriter wt);

}
