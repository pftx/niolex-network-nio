/**
 * GroupDiffHandler.java
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

import org.apache.niolex.config.bean.ConfigItem;
import org.apache.niolex.config.core.PacketTranslater;
import org.apache.niolex.config.service.GroupService;
import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Handle the group config item changed request, which means
 * other server changed a config.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-7-5
 */
@Component
public class GroupDiffHandler implements IPacketHandler {


	/**
	 * Do all the config group works.
	 */
	@Autowired
	private GroupService groupService;

	/**
	 * Override super method
	 * @see org.apache.niolex.network.IPacketHandler#handlePacket(org.apache.niolex.network.PacketData, org.apache.niolex.network.IPacketWriter)
	 */
	@Override
	public void handlePacket(PacketData sc, IPacketWriter wt) {
		ConfigItem item = PacketTranslater.toConfigItem(sc);
		groupService.svrSendDiff(item);
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.IPacketHandler#handleClose(org.apache.niolex.network.IPacketWriter)
	 */
	@Override
	public void handleClose(IPacketWriter wt) {
		// We do not need any work.
	}

}
