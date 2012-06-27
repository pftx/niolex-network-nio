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
import java.util.List;

import org.apache.niolex.commons.event.ConcurrentEventDispatcher;
import org.apache.niolex.commons.event.EventListener;
import org.apache.niolex.commons.event.IEventDispatcher;
import org.apache.niolex.network.Config;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.name.bean.AddressRecord;
import org.apache.niolex.network.name.core.NameClient;

/**
 * Subscribe to name server to listen address change.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-20
 */
public class AddressSubscriber extends NameClient implements IEventDispatcher<AddressRecord> {

	/**
	 * The service address dispatcher proxy.
	 */
	private final ConcurrentEventDispatcher<AddressRecord> dispatcher = new ConcurrentEventDispatcher<AddressRecord>();

	/**
	 * The rpc handle timeout in milliseconds.
	 */
	private int rpcHandleTimeout = Config.RPC_HANDLE_TIMEOUT;

	/**
	 * Construct a subscriber by a client.
	 * @param client
	 * @throws IOException
	 */
	public AddressSubscriber(String serverAddress) throws IOException {
		super(serverAddress);
	}

	/**
	 * Proxy method.
	 * Override super method
	 * @see org.apache.niolex.commons.event.IEventDispatcher#addListener(org.apache.niolex.commons.event.EventListener)
	 */
	public void addListener(EventListener<AddressRecord> eListener) {
		dispatcher.addListener(eListener);
	}

	/**
	 * Proxy method.
	 * Override super method
	 * @see org.apache.niolex.commons.event.IEventDispatcher#removeListener(org.apache.niolex.commons.event.EventListener)
	 */
	public void removeListener(EventListener<AddressRecord> eListener) {
		dispatcher.removeListener(eListener);
	}

	/**
	 * Proxy method.
	 * Override super method
	 * @see org.apache.niolex.commons.event.IEventDispatcher#fireEvent(java.lang.Object)
	 */
	public void fireEvent(AddressRecord e) {
		dispatcher.fireEvent(e);
	}

	/**
	 * It's fire event.
	 * Override super method
	 * @see org.apache.niolex.network.name.core.NameClient#handleDiff(org.apache.niolex.network.name.bean.AddressRecord)
	 */
	@Override
	protected void handleDiff(AddressRecord bean) {
		fireEvent(bean);
	}

	/**
	 * Get service addresses list.
	 *
	 * @param serviceKey
	 * @param listener
	 * @return
	 */
	public List<String> getServiceAddrList(String serviceKey, EventListener<AddressRecord> listener) {
		// We can listen changes from now on.
		addListener(listener);
		// Register this subscriber.
		PacketData listnName = transformer.getPacketData(Config.CODE_NAME_OBTAIN, serviceKey);
		client.handleWrite(listnName);
		try {
			List<String> list = waiter.waitForResult(client, rpcHandleTimeout);
			return list;
		} catch (Exception e) {
			throw new NameServiceException("Error occured when getServiceAddrList", e);
		}
	}

	public void setRpcHandleTimeout(int rpcHandleTimeout) {
		this.rpcHandleTimeout = rpcHandleTimeout;
	}

}
