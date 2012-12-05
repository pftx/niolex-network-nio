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
	 * Server need to start threads internally to run.
	 * This method need to return after this server is started.
	 */
	public boolean start();

	/**
	 * Stop this server.
	 * After stop, the internal threads need to be stopped.
	 */
	public void stop();

	/**
	 * The current listen port.
	 * @return
	 */
	public int getPort();

	/**
	 * Set listen port.
	 * This method must be called before start()
	 * @param port
	 */
	public void setPort(int port);

	/**
	 * Get the packet handler set to server.
	 *
	 * @return the packetHandler
	 */
	public IPacketHandler getPacketHandler();

	/**
	 * Set the server side packet handler
	 * This method must be called before start()
	 *
	 * @param packetHandler the packetHandler to set
	 */
	public void setPacketHandler(IPacketHandler packetHandler);

	/**
	 * @return the acceptTimeOut
	 */
	public int getAcceptTimeOut();

	/**
	 * Set the server accept timeout
	 * This method must be called before start()
	 *
	 * @param acceptTimeOut the acceptTimeOut to set
	 */
	public void setAcceptTimeOut(int acceptTimeOut);

}