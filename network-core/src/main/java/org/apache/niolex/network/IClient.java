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
public interface IClient extends IPacketWriter {

	/**
	 * Do real connect action, connect to server.
	 * This method will return immediately.
	 * @throws IOException
	 */
	public void connect() throws IOException;

	/**
	 * Stop this client.
	 */
	public void stop();

	/**
	 * Test whether this client is working now
	 * @return
	 */
	public boolean isWorking();

	/**
	 * Set the packet handler this client using
	 * @param packetHandler
	 */
	public void setPacketHandler(IPacketHandler packetHandler);

	/**
	 * Set the underline socket connect timeout
	 * This method must be called before connect()
	 *
	 * @param connectTimeout
	 */
	public void setConnectTimeout(int connectTimeout);

	/**
	 * Set the server Internet address this client want to connect
	 * This method must be called before connect()
	 *
	 * @param serverAddress
	 */
	public void setServerAddress(InetSocketAddress serverAddress);

	/**
	 * Get the current Internet address
	 * @return
	 */
	public InetSocketAddress getServerAddress();

}