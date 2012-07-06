/**
 * GroupServiceImpl.java
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
package org.apache.niolex.config.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.niolex.config.bean.ConfigItem;
import org.apache.niolex.config.bean.GroupConfig;
import org.apache.niolex.config.bean.SyncBean;
import org.apache.niolex.config.config.AttachKey;
import org.apache.niolex.config.core.CodeMap;
import org.apache.niolex.config.core.MemoryStorage;
import org.apache.niolex.config.core.PacketTranslater;
import org.apache.niolex.config.dao.GroupDao;
import org.apache.niolex.config.dao.ItemDao;
import org.apache.niolex.config.event.ConfigEventDispatcher;
import org.apache.niolex.config.service.AuthenService;
import org.apache.niolex.config.service.GroupService;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-7-5
 */
@Service
public class GroupServiceImpl implements GroupService {

	/**
	 * Use this field to sync with DB.
	 */
	private long lastSyncTime = 0;

	/**
	 * Dispatch event to clients.
	 */
	@Autowired
	private ConfigEventDispatcher dispatcher;

	/**
	 * Store all the configurations.
	 */
	@Autowired
	private MemoryStorage storage;

	/**
	 * Do all the authentication works.
	 */
	@Autowired
	private AuthenService service;

	/**
	 * The Dao managing config groups.
	 */
	@Autowired
	private GroupDao groupDao;

	/**
	 * The Dao managing config items.
	 */
	@Autowired
	private ItemDao itemDao;

	/**
	 * Override super method
	 *
	 * @see org.apache.niolex.config.service.GroupService#subscribeGroup(java.lang.String,
	 *      org.apache.niolex.network.IPacketWriter)
	 */
	@Override
	public boolean subscribeGroup(String groupName, IPacketWriter wt) {
		GroupConfig group = storage.get(groupName);
		if (group != null) {
			// Authenticate Group Read.
			if (service.hasReadAuth(group, wt)) {
				wt.handleWrite(PacketTranslater.translate(group));
				dispatcher.addListener(groupName, wt);
				// Add this group into set.
				Set<String> set = getCachedGroupSet(wt);
				set.add(groupName);
				return true;
			} else {
				// Has no right to read this group.
				wt.handleWrite(new PacketData(CodeMap.GROUP_NOA, groupName));
			}
		} else {
			// Group not found.
			wt.handleWrite(new PacketData(CodeMap.GROUP_NOF, groupName));
		}
		return false;
	}

	/**
	 * Get the cached config group set from writer.
	 * @param wt
	 * @return
	 */
	private Set<String> getCachedGroupSet(IPacketWriter wt) {
		// This client may already has some groups, so we merge them.
		Set<String> set = wt.getAttached(AttachKey.GROUP_SET);
		if (set == null) {
			set = new HashSet<String>();
			// Attach group list. For close this client and remove listener.
			wt.attachData(AttachKey.GROUP_SET, set);
		}
		return set;
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.config.service.GroupService#syncGroup(org.apache.niolex.config.bean.SyncBean, org.apache.niolex.network.IPacketWriter)
	 */
	@Override
	public void syncGroup(SyncBean bean, IPacketWriter wt) {
		String groupName = bean.getGroupName();
		GroupConfig group = storage.get(groupName);
		if (group != null) {
			// Authenticate Group Read.
			if (service.hasReadAuth(group, wt)) {
				Map<String, Long> groupData = bean.getGroupData();
				for (ConfigItem item : group.getGroupData().values()) {
					Long time = groupData.get(item.getKey());
					if (time == null || time.longValue() < item.getUpdateTime()) {
						// This item is new at server side, so dispatch it.
						wt.handleWrite(PacketTranslater.translate(item));
					}
				}
			} else {
				// Has no right to read this group.
				wt.handleWrite(new PacketData(CodeMap.GROUP_NOA, groupName));
			}
		} else {
			// Group not found.
			wt.handleWrite(new PacketData(CodeMap.GROUP_NOF, groupName));
		}
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.config.service.GroupService#syncAllGroupsWithDB()
	 */
	@Override
	public void syncAllGroupsWithDB() {
		// Load all the group names.
		List<GroupConfig> allGroups = groupDao.loadAllGroups();
		// Store the current DB time.
		long tmpTime = groupDao.loadDBTime();
		List<ConfigItem> allItems = itemDao.loadAllConfigItems(lastSyncTime);
		// Renew last sync time.
		lastSyncTime = tmpTime;
		allGroups = assembleGroups(allGroups, allItems);

		// Store new groups into memory storage.
		for (GroupConfig conf : allGroups) {
			storeGroup(conf);
		}
	}

	/**
	 * Assemble config items into group config.
	 * @param allGroups
	 * @param allItems
	 * @return
	 */
	private List<GroupConfig> assembleGroups(List<GroupConfig> allGroups, List<ConfigItem> allItems) {
		int j = 0, groupId = 0;
		GroupConfig gConf = null;
		Map<String, ConfigItem> groupData = null;
		// item list must order by groupId
		for (ConfigItem item : allItems) {
			// group list must order by groupId
			if (item.getGroupId() != groupId) {
				// Find the correct group.
				while (j < allGroups.size()) {
					gConf = allGroups.get(j);
					// In case we can not found that group, that Id will be jumped.
					if (gConf.getGroupId() >= item.getGroupId()) {
						break;
					}
					++j;
				}
				// Check whether we find it.
				if (gConf.getGroupId() == item.getGroupId()) {
					groupId = gConf.getGroupId();
					groupData = gConf.getGroupData();
				} else {
					// We can not find the group for this item at all.
					continue;
				}
			}
			groupData.put(item.getKey(), item);
		}
		return allGroups;
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.config.service.GroupService#handleDiff(org.apache.niolex.config.bean.ConfigItem)
	 */
	@Override
	public void handleDiff(ConfigItem diff) {
		String groupName = storage.findGroupName(diff.getGroupId());
		if (groupName != null) {
			boolean b = storage.updateConfigItem(groupName, diff);
			if (b) {
				// This is from other server, so we will not fire it to any server.
				dispatcher.fireClientEvent(groupName, diff);
			}
		}
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.config.service.GroupService#loadGroup(java.lang.String)
	 */
	@Override
	public void loadGroup(String groupName) {
		GroupConfig group = groupDao.loadGroup(groupName);
		if (group != null) {
			List<ConfigItem> allItems = itemDao.loadGroupItems(group.getGroupId());
			if (allItems != null && allItems.size() != 0) {
				Map<String, ConfigItem> groupData = group.getGroupData();
				for (ConfigItem item : allItems) {
					groupData.put(item.getKey(), item);
				}
			}
			storeGroup(group);
		}
	}

	/**
	 * Store this group config into memory storage.
	 * @param group
	 */
	private void storeGroup(GroupConfig conf) {
		List<ConfigItem>  clist = storage.store(conf);
		if (clist != null) {
			// Fire event to notify clients.
			for (ConfigItem item : clist) {
				dispatcher.fireEvent(conf.getGroupName(), item);
			}
		}
	}

}
