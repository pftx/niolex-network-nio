/**
 * BaseClient.java
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
package org.apache.niolex.network.client;

import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.niolex.network.Config;
import org.apache.niolex.network.IClient;
import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.event.WriteEventListener;

/**
 * The base implementation of IClient, please extend this class for convenience.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0, Date: 2012-6-14
 */
public abstract class BaseClient implements IClient {

	/**
	 * The socket address this client it going to connect.
	 */
    protected InetSocketAddress serverAddress;

    /**
     * The packet handler when packet received.
     */
    protected IPacketHandler packetHandler;

    /**
     * The client socket under control.
     */
    protected Socket socket;

    /**
     * The status of this client.
     */
    protected volatile boolean isWorking;

    /**
     * Socket connect timeout.
     */
    protected int connectTimeout = Config.SO_CONNECT_TIMEOUT;

	/**
	 * Override super method
	 * @see org.apache.niolex.network.IPacketWriter#getRemoteName()
	 */
    @Override
    public String getRemoteName() {
    	if (socket == null) {
    		return serverAddress.toString() + "-0000";
    	} else {
    		return serverAddress.toString() + "-" + socket.getLocalPort();
    	}
    }

    /**
     * Safely close the socket.
     *
     * @return null if success, exception if error occurred.
     */
    protected Exception safeClose() {
        try {
            if (socket != null) socket.close();
            return null;
        } catch (Exception e) {
            return e;
        }
    }

	/**
	 * Override super method
	 * @see org.apache.niolex.network.IPacketWriter#addEventListener(org.apache.niolex.network.event.WriteEventListener)
	 */
	@Override
	public void addEventListener(WriteEventListener listener) {
		throw new UnsupportedOperationException("This method is not supported.");
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.IPacketWriter#attachData(java.lang.String, java.lang.Object)
	 */
	@Override
	public Object attachData(String key, Object value) {
		throw new UnsupportedOperationException("This method is not supported.");
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.IPacketWriter#getAttached(java.lang.String)
	 */
	@Override
	public <T> T getAttached(String key) {
		throw new UnsupportedOperationException("This method is not supported.");
	}

    /**
	 * This is the override of super method.
	 * @see org.apache.niolex.network.IClient#setPacketHandler(org.apache.niolex.network.IPacketHandler)
	 */
    @Override
	public void setPacketHandler(IPacketHandler packetHandler) {
        this.packetHandler = packetHandler;
    }

	/**
	 * This is the override of super method.
	 * @see org.apache.niolex.network.IClient#getServerAddress()
	 */
	@Override
	public InetSocketAddress getServerAddress() {
		return serverAddress;
	}

	/**
	 * This is the override of super method.
	 * @see org.apache.niolex.network.IClient#setServerAddress(java.net.InetSocketAddress)
	 */
	@Override
	public void setServerAddress(InetSocketAddress serverAddress) {
		this.serverAddress = serverAddress;
	}

	/**
	 * This is the override of super method.
	 *
	 * @return the socket connect timeout.
	 */
	public int getConnectTimeout() {
		return connectTimeout;
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.IClient#setConnectTimeout(int)
	 */
	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	/**
	 * This is the override of super method.
	 * @see org.apache.niolex.network.IClient#isWorking()
	 */
	@Override
	public boolean isWorking() {
		return isWorking;
	}

}
