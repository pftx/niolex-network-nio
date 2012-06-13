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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.niolex.network.Config;
import org.apache.niolex.network.IClient;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The packet invoker which is able to handle multiple threads.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-13
 */
public class PacketInvoker implements RpcInvoker {
	private static final Logger LOG = LoggerFactory.getLogger(PacketInvoker.class);


	/**
	 * The rpc handle timeout in milliseconds.
	 */
	private int rpcHandleTimeout = Config.RPC_HANDLE_TIMEOUT;

	/**
	 * The current waiting map.
	 */
	private Map<Integer, RpcWaitItem> waitMap = new ConcurrentHashMap<Integer, RpcWaitItem>();

	/**
	 * Override super method
	 *
	 * @see org.apache.niolex.network.rpc.RpcInvoker#invoke(org.apache.niolex.network.PacketData,
	 *      org.apache.niolex.network.IClient)
	 */
	@Override
	public PacketData invoke(PacketData rc, IClient client) {
		// 4. Set up the waiting information
		RpcWaitItem wi = new RpcWaitItem();
		wi.setThread(Thread.currentThread());
		Integer key = RpcUtil.generateKey(rc);
		waitMap.put(key, wi);
		// 5. Send request to remote server
		client.handleWrite(rc);
		// 6. Wait for result.
		long in = System.currentTimeMillis();
		while (System.currentTimeMillis() - in < rpcHandleTimeout) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// Do not care.
			}
			if (wi.getReceived() != null) {
				break;
			}
		}
		// 7. Clean the map.
		waitMap.remove(key);
		return wi.getReceived();
	}

	@Override
	public void handleRead(PacketData sc, IPacketWriter wt) {
		int key = RpcUtil.generateKey(sc);
		RpcWaitItem wi = waitMap.get(key);
		if (wi == null) {
			LOG.warn("Packet received for key [{}] have no handler, just ignored.", key);
		} else {
			waitMap.remove(key);
			wi.setReceived(sc);
			wi.getThread().interrupt();
		}
	}

	@Override
	public void handleClose(IPacketWriter wt) {
		for (RpcWaitItem wi : waitMap.values()) {
			wi.getThread().interrupt();
		}
	}


	public int getRpcHandleTimeout() {
		return rpcHandleTimeout;
	}

	public void setRpcHandleTimeout(int rpcHandleTimeout) {
		this.rpcHandleTimeout = rpcHandleTimeout;
	}
}
