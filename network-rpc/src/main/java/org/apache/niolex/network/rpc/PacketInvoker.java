/**
 * PacketInvoker.java
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

import org.apache.niolex.commons.concurrent.Blocker;
import org.apache.niolex.commons.concurrent.WaitOn;
import org.apache.niolex.network.Config;
import org.apache.niolex.network.IClient;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.rpc.util.RpcUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The packet invoker which is able to handle multiple threads.
 * Use this invoker along with the {@link org.apache.niolex.network.client.PacketClient} or
 * {@link org.apache.niolex.network.client.BlockingClient}
 *
 * @see org.apache.niolex.network.client.PacketClient
 * @see org.apache.niolex.network.client.BlockingClient
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0, Date: 2012-6-13
 */
public class PacketInvoker implements RemoteInvoker {
	private static final Logger LOG = LoggerFactory.getLogger(PacketInvoker.class);


	/**
	 * The current waiting blocker.
	 */
	private final Blocker<PacketData> blocker = new Blocker<PacketData>();

	/**
	 * The rpc handle timeout in milliseconds.
	 */
	private int rpcHandleTimeout = Config.RPC_HANDLE_TIMEOUT;

	/**
	 * Override super method
	 *
	 * @see org.apache.niolex.network.rpc.RemoteInvoker#invoke(org.apache.niolex.network.PacketData,
	 *      org.apache.niolex.network.IClient)
	 */
	@Override
	public PacketData invoke(PacketData rc, IClient client) {
		// 1. Set up the waiting information
		Integer key = RpcUtil.generateKey(rc);
		WaitOn<PacketData> waitOn = blocker.init(key);
		// 2. Send request to remote server
		client.handleWrite(rc);
		// 3. Wait for result.
		PacketData res = null;
		try {
			res = waitOn.waitForResult(rpcHandleTimeout);
		} catch (Exception e) {}
		if (res == null) {
			// Release the key to prevent memory leak.
			blocker.release(key, rc);
		}
		return res;
	}

	/**
	 * Release the waiting thread.
	 * If there is no thread waiting for this packet, we do a Warn log.
	 *
	 * Override super method
	 * @see org.apache.niolex.network.IPacketHandler#handlePacket(org.apache.niolex.network.PacketData,
	 *  org.apache.niolex.network.IPacketWriter)
	 */
	@Override
	public void handlePacket(PacketData sc, IPacketWriter wt) {
		Integer key = RpcUtil.generateKey(sc);
		boolean isOk = blocker.release(key, sc);
		if (!isOk) {
			LOG.warn("Packet received for key [{}] have no handler, just ignored.", key);
		}
	}

	/**
	 * Release all the threads on hold.
	 *
	 * Override super method
	 * @see org.apache.niolex.network.IPacketHandler#handleClose(org.apache.niolex.network.IPacketWriter)
	 */
	@Override
	public void handleClose(IPacketWriter wt) {
		blocker.releaseAll();
	}

	/**
	 * Get the current timeout.
	 *
	 * @return the current timeout
	 */
	public int getRpcHandleTimeout() {
		return rpcHandleTimeout;
	}

	/**
	 * The rpc holding thread will return null if the result is not ready after
	 * this time.
	 *
	 * @param rpcHandleTimeout the timeout to set to
	 */
	public void setRpcHandleTimeout(int rpcHandleTimeout) {
		this.rpcHandleTimeout = rpcHandleTimeout;
	}
}
