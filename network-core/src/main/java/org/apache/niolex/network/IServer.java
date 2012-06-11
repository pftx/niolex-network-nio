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
 * @Date: 2012-6-8
 */
public interface IServer {

	/**
	 * Start the NioServer, bind to Port.
	 * Server need to start threads internally to run.
	 * This method need to return after this server is started.
	 */
	public abstract boolean start();

	/**
	 * Stop this server.
	 * After stop, the internal threads need to be stopped.
	 */
	public abstract void stop();

	/**
	 * The current listen port.
	 * @return
	 */
	public abstract int getPort();

	/**
	 * Set listen port.
	 * @param port
	 */
	public abstract void setPort(int port);

	/**
	 * @return the packetHandler
	 */
	public abstract IPacketHandler getPacketHandler();

	/**
	 * @param packetHandler the packetHandler to set
	 */
	public abstract void setPacketHandler(IPacketHandler packetHandler);

	/**
	 * @return the acceptTimeOut
	 */
	public abstract int getAcceptTimeOut();

	/**
	 * @param acceptTimeOut the acceptTimeOut to set
	 */
	public abstract void setAcceptTimeOut(int acceptTimeOut);

	/**
	 * @return the heartBeatInterval
	 */
	public abstract int getHeartBeatInterval();

	/**
	 * @param heartBeatInterval the heartBeatInterval to set
	 */
	public abstract void setHeartBeatInterval(int heartBeatInterval);

}