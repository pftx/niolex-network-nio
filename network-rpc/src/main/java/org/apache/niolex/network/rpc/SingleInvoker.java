/**
 * SingleInvoker.java
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
package org.apache.niolex.network.rpc;

import org.apache.niolex.network.IClient;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;

/**
 * This invoker can only handle invoke serially.
 * Please use this invoker with SocketClient only.
 *
 * @see org.apache.niolex.network.client.SocketClient
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-13
 */
public class SingleInvoker implements RemoteInvoker {
	private PacketData res;

	/**
	 * Override super method
	 * @see org.apache.niolex.network.IPacketHandler#handleRead(org.apache.niolex.network.PacketData, org.apache.niolex.network.IPacketWriter)
	 */
	@Override
	public void handleRead(PacketData sc, IPacketWriter wt) {
		this.res = sc;
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.IPacketHandler#handleClose(org.apache.niolex.network.IPacketWriter)
	 */
	@Override
	public void handleClose(IPacketWriter wt) {
		// Nothing to close
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.rpc.RemoteInvoker#invoke(org.apache.niolex.network.PacketData, org.apache.niolex.network.IClient)
	 */
	@Override
	public PacketData invoke(PacketData packet, IClient client) {
		this.res = null;
		client.handleWrite(packet);
		return res;
	}

}
