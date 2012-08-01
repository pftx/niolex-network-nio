/**
 * ServerSubscribeHandler.java
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

import org.apache.niolex.commons.codec.StringUtil;
import org.apache.niolex.config.core.CodeMap;
import org.apache.niolex.config.event.ConfigEventDispatcher;
import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-8-1
 */
@Component
public class ServerSubscribeHandler implements IPacketHandler {

	private static final String AUTH = "3836a809b1bd88a0f093916a4bc46a6b";

	/**
	 * Dispatch event to clients.
	 */
	@Autowired
	private ConfigEventDispatcher dispatcher;

	/**
	 * Override super method
	 * @see org.apache.niolex.network.IPacketHandler#handleRead(org.apache.niolex.network.PacketData, org.apache.niolex.network.IPacketWriter)
	 */
	@Override
	public void handleRead(PacketData sc, IPacketWriter wt) {
		String s = StringUtil.utf8ByteToStr(sc.getData());
		if (s != null && s.equals(AUTH)) {
			dispatcher.addOtherServer(wt);
			wt.handleWrite(new PacketData(CodeMap.RES_SERVER_SUBS, "OK"));
		} else {
			// Failed to subscribe to this server.
			wt.handleWrite(new PacketData(CodeMap.RES_SERVER_SUBS, "Auth Failed"));
		}
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.IPacketHandler#handleClose(org.apache.niolex.network.IPacketWriter)
	 */
	@Override
	public void handleClose(IPacketWriter wt) {
		// Remove this client from other server list.
		dispatcher.removeOtherServer(wt);
	}

}
