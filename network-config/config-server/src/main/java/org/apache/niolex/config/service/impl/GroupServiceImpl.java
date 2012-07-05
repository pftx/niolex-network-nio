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
import org.apache.niolex.config.event.ConfigEventDispatcher;
import org.apache.niolex.config.service.AuthenService;
import org.apache.niolex.config.service.GroupService;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-7-5
 */
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
	 * @see org.apache.niolex.config.service.GroupService#loadAllGroups()
	 */
	@Override
	public List<GroupConfig> loadAllGroups() {
		// TODO Auto-generated method stub
		//lastSyncTime = ..
		return null;
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
		// TODO Auto-generated method stub

	}

}
