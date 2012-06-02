/**
 * IClient.java
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
package org.apache.niolex.network;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-2
 */
public interface IClient {

	/**
	 * Do real connect action. Start two separate threads for reads and writes.
	 * This method will return immediately.
	 * @throws IOException
	 */
	public abstract void connect() throws IOException;

	/**
	 * Stop this client.
	 */
	public abstract void stop();

	/**
	 * @param packetHandler the packetHandler to set
	 */

	/**
	 * Test whether this client is working now
	 * @return
	 */
	public abstract boolean isWorking();

	/**
	 * Set the packet handler this client using
	 * @param packetHandler
	 */
	public abstract void setPacketHandler(IPacketHandler packetHandler);

	/**
	 * Set the Internet address to connect to
	 * @return
	 */
	public abstract InetSocketAddress getServerAddress();

	/**
	 * Get the current Internet address
	 * @param serverAddress
	 */
	public abstract void setServerAddress(InetSocketAddress serverAddress);

}