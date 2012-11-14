/**
 * RemoteInvoker.java
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
import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.PacketData;

/**
 * This interface control sending packets to RPC server using the client passed in,
 * and wait for the results from server side.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0, Date: 2012-6-13
 */
public interface RemoteInvoker extends IPacketHandler {

	/**
	 * Invoke the write methods in the client, send packet data to server
	 * and wait for the response.
	 *
	 * @param packet the data need to send
	 * @param client the client used to send this data
	 * @return the packet returned from server
	 */
	public PacketData invoke(PacketData packet, IClient client);

}
