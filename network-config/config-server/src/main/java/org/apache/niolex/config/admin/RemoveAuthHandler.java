/**
 * RemoveAuthHandler.java
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
package org.apache.niolex.config.admin;

import org.apache.niolex.commons.codec.StringUtil;
import org.apache.niolex.config.core.CodeMap;
import org.apache.niolex.config.service.AuthenService;
import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-7-10
 */
@Component
public class RemoveAuthHandler implements IPacketHandler {

	/**
	 * Do all the authentication works.
	 */
	@Autowired
	private AuthenService service;

	/**
	 * Override super method
	 * @see org.apache.niolex.network.IPacketHandler#handlePacket(org.apache.niolex.network.PacketData, org.apache.niolex.network.IPacketWriter)
	 */
	@Override
	public void handlePacket(PacketData sc, IPacketWriter wt) {
		String[] args = StringUtil.utf8ByteToStr(sc.getData()).split(",");
		String s = service.removeReadAuth(args[0], args[1], wt);
		wt.handleWrite(new PacketData(CodeMap.RES_REMOVE_AUTH, s));
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
