/**
 * AddressSubscriber.java
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
package org.apache.niolex.network.name.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.niolex.commons.concurrent.Blocker;
import org.apache.niolex.commons.concurrent.WaitOn;
import org.apache.niolex.commons.test.Check;
import org.apache.niolex.network.Config;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.name.bean.AddressRecord;
import org.apache.niolex.network.name.core.NameClient;

/**
 * Subscribe to name server to listen to address changes.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-6-20
 */
public class AddressSubscriber extends NameClient {

	/**
     * The service address listener map.
     */
	private final HashMap<String, AddressEventListener> map = new HashMap<String, AddressEventListener>();

	/**
	 * The help class to wait for result.
	 */
	protected final Blocker<List<String>> waiter = new Blocker<List<String>>();

	/**
	 * The rpc handle timeout in milliseconds.
	 */
	private int rpcHandleTimeout = Config.RPC_HANDLE_TIMEOUT;

	/**
     * Construct a subscriber by a server address.
     *
     * @param serverAddress the name server address
     * @throws IOException if I / O related error occurred
     */
	public AddressSubscriber(String serverAddress) throws IOException {
		super(serverAddress);
	}

	/**
	 * It's fire event.
	 * Override super method
	 * @see org.apache.niolex.network.name.core.NameClient#handleDiff(org.apache.niolex.network.name.bean.AddressRecord)
	 */
	@Override
	protected void handleDiff(AddressRecord bean) {
		AddressEventListener li = map.get(bean.getAddressKey());
		if (li != null) {
			switch (bean.getStatus()) {
				case OK:
					li.addressAdd(bean.getAddressValue());
					break;
				case DEL:
					li.addressRemove(bean.getAddressValue());
					break;
			}
		}
	}

	/**
	 * This is a direct answer of subscribe address.
	 * Override super method
	 * @see org.apache.niolex.network.name.core.NameClient#handleRefresh(java.util.List)
	 */
	@Override
	protected void handleRefresh(List<String> list) {
        // The last string in the address list is not an address, but the address key.
		int last = list.size() - 1;
		if (last > -1) {
			String addressKey = list.get(last);
			list = list.subList(0, last);
			if (!waiter.release(addressKey, list)) {
                // If no one is waiting for this list, then it's server want to refresh all clients.
				AddressEventListener li = map.get(addressKey);
				if (li != null) {
					li.addressRefresh(list);
				}
			}
		}
	}

	/**
	 * Get service addresses list, and add the listener to listen changes.
	 *
	 * @param serviceKey the service key
	 * @param listener the address event listener
	 * @return The current addresses list
	 * @throws NameServiceException if any exception occurred.
	 */
	public List<String> getServiceAddrList(String serviceKey, AddressEventListener listener) {
        Check.notNull(listener, "The listener should not be null.");
		// We can listen changes from now on.
		map.put(serviceKey, listener);
		// Register this subscriber.
		PacketData obtain = transformer.getPacketData(Config.CODE_NAME_OBTAIN, serviceKey);
		savePacket(obtain);
		WaitOn<List<String>> on = waiter.init(serviceKey);
		client().handleWrite(obtain);
		try {
			List<String> list = on.waitForResult(rpcHandleTimeout);
			return list;
		} catch (Exception e) {
			throw new NameServiceException("Error occured when getServiceAddrList", e);
		}
	}

	public void setRpcHandleTimeout(int rpcHandleTimeout) {
		this.rpcHandleTimeout = rpcHandleTimeout;
	}

}
