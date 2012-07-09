/**
 * AuthSubscribeHandler.java
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
package org.apache.niolex.config.handler;

import java.util.List;
import java.util.Set;

import org.apache.niolex.config.bean.SubscribeBean;
import org.apache.niolex.config.config.AttachKey;
import org.apache.niolex.config.core.CodeMap;
import org.apache.niolex.config.core.PacketTranslater;
import org.apache.niolex.config.event.ConfigEventDispatcher;
import org.apache.niolex.config.service.AuthenService;
import org.apache.niolex.config.service.GroupService;
import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Handle clients subscribe authentication packets.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-7-5
 */
@Component
public class AuthSubscribeHandler implements IPacketHandler {

	/**
	 * Dispatch event to clients.
	 */
	@Autowired
	private ConfigEventDispatcher dispatcher;

	/**
	 * Do all the authentication works.
	 */
	@Autowired
	private AuthenService service;

	/**
	 * Do all the config group works.
	 */
	@Autowired
	private GroupService groupService;




	/**
	 * Override super method
	 * @see org.apache.niolex.network.IPacketHandler#handleRead(org.apache.niolex.network.PacketData, org.apache.niolex.network.IPacketWriter)
	 */
	@Override
	public void handleRead(PacketData sc, IPacketWriter wt) {
		// Parse bean from packet data.
		SubscribeBean bean = PacketTranslater.toSubscribeBean(sc);
		// Step 1. Deal with auth info.
		if (!service.authUser(bean, wt)) {
			// Auth failed.
			wt.handleWrite(new PacketData(CodeMap.AUTH_FAIL));
			return;
		}
		// Step 2. Deal with group subscribe.
		List<String> list = bean.getGroupList();
		if (list == null || list.size() == 0) {
			return;
		}

		// Start to try subscribe group list.

		for (String groupName : list) {
			groupService.subscribeGroup(groupName, wt);
		}
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.IPacketHandler#handleClose(org.apache.niolex.network.IPacketWriter)
	 */
	@Override
	public void handleClose(IPacketWriter wt) {
		// Remove all the listeners of this client.
		Set<String> set = wt.getAttached(AttachKey.GROUP_SET);
		if (set == null || set.size() == 0) {
			return;
		}
		for (String groupName : set) {
			dispatcher.removeListener(groupName, wt);
		}
	}

}
