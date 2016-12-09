/**
 * IServer.java
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

/**
 * The interface of server.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-6-8
 */
public interface IServer {

	/**
	 * Start the Server, bind to the Port.
	 * Server need to start threads internally to run the main loop.
	 * <b>This method need to return after this server is started.</b>
	 * 
	 * @return true if server started, false if failed to start server
	 */
	public boolean start();

	/**
	 * Stop this server.
	 * After stop, the internal threads need to be stopped.
	 */
	public void stop();

	/**
	 * @return The current listening port.
	 */
	public int getPort();

	/**
	 * Set the listening port.
	 * <br>
	 * This method must be called before {@link #start()}
	 *
	 * @param port the new listening port
	 */
	public void setPort(int port);

	/**
	 * Get the packet handler set to this server.
	 *
	 * @return the packetHandler
	 */
	public IPacketHandler getPacketHandler();

	/**
	 * Set the server side packet handler.
	 * <br>
     * This method must be called before {@link #start()}
	 *
	 * @param packetHandler the packetHandler to set
	 */
	public void setPacketHandler(IPacketHandler packetHandler);

	/**
	 * @return the accept timeout
	 */
	public int getAcceptTimeout();

	/**
     * Set the server accept timeout.
     * <br>
     * This method must be called before {@link #start()}
     *
     * @param acceptTimeout the socket accept timeout in milliseconds
     */
	public void setAcceptTimeout(int acceptTimeout);

}