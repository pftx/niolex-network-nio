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
 * The Client side interface, Define the network control methods.
 * A Client must be able to write packets, so it extends the #IPacketWriter interface.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-6-2
 */
public interface IClient extends IPacketWriter {

	/**
	 * Do real connect action, connect to server.
	 * This method will return immediately after get connected.
	 *
	 * @throws IOException
	 */
	public void connect() throws IOException;

	/**
	 * Stop this client.
	 */
	public void stop();

	/**
	 * Test whether this client is working now
	 *
	 * @return the current status
	 */
	public boolean isWorking();

	/**
	 * Set the packet handler this client going to use
	 *
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
	 * Set the server Internet address this client want to connect
	 * This method must be called before connect()
	 *
	 * @param serverAddress
	 */
	public void setServerAddress(String serverAddress);

	/**
	 * Get the current Internet address of Remote server.
	 *
	 * @return the remote server address
	 */
	public InetSocketAddress getServerAddress();

}