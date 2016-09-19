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

import org.apache.niolex.network.Config;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.name.bean.AddressRegiBean;
import org.apache.niolex.network.name.core.NameClient;

/**
 * Publish service address to name server.
 * This service will maintain connection to server with a long connection.
 * If this connection is broken, server will mark all the addresses published
 * from this service as disconnected.
 * <br>
 * After reconnected, we will try to republish all the addresses for you
 * automatically.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-6-27
 */
public class AddressPublisher extends NameClient {

	/**
     * The constructor.
     *
     * @param serverAddress the name server address
     * @throws IOException if I / O related error occurred
     */
	public AddressPublisher(String serverAddress) throws IOException {
		super(serverAddress);
	}

	/**
     * Publish this service to name server.
     *
     * @param addressKey the service key
     * @param addressValue the service address
     */
	public void pushlishService(String addressKey, String addressValue) {
		AddressRegiBean regi = new AddressRegiBean(addressKey, addressValue);
		PacketData data = transformer.getPacketData(Config.CODE_NAME_PUBLISH, regi);
		savePacket(data);
		client().handleWrite(data);
	}

}
