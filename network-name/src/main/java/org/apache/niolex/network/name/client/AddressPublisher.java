/**
 * AddressPublisher.java
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
import java.util.ArrayList;
import java.util.List;

import org.apache.niolex.network.Config;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.name.bean.AddressRegiBean;
import org.apache.niolex.network.name.core.NameClient;

/**
 * Publish service address to name server.
 * This service will maintain connection to server with a long connection.
 * If this connection is broken, server will mark all the addresses published
 * from this service disconnected.
 *
 * After reconnected, we will try to republish all the addresses for you
 * automatically.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-6-27
 */
public class AddressPublisher extends NameClient {

	/**
	 * Store all the requests, retry them after reconnection.
	 */
	private final List<PacketData> list = new ArrayList<PacketData>();

	/**
	 * The constructor.
	 *
	 * @param serverAddress
	 * @throws IOException
	 */
	public AddressPublisher(String serverAddress) throws IOException {
		super(serverAddress);
	}

	/**
	 * Publish this service to name server.
	 *
	 * @param addressKey
	 * @param addressValue
	 */
	public synchronized void pushlishService(String addressKey, String addressValue) {
		AddressRegiBean regi = new AddressRegiBean(addressKey, addressValue);
		PacketData data = transformer.getPacketData(Config.CODE_NAME_PUBLISH, regi);
		list.add(data);
		client().handleWrite(data);
	}

	/**
	 * Publish all the services.
	 *
	 * Override super method
	 * @see org.apache.niolex.network.name.core.NameClient#reconnected()
	 */
	protected synchronized void reconnected() {
		for (PacketData data : list) {
			client().handleWrite(data);
		}
	}

	public int size() {
		return list.size();
	}

}
