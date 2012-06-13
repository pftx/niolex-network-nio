/**
 * SocketClient.java
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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.RejectedExecutionException;

import org.apache.niolex.network.Config;
import org.apache.niolex.network.IClient;
import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.PacketData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The blocking implementation of IClient.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-13
 */
public class SocketClient implements IClient {
	private static final Logger LOG = LoggerFactory.getLogger(SocketClient.class);

    private InetSocketAddress serverAddress;
    private IPacketHandler packetHandler;
    private Socket socket;
    private DataInputStream inS;
    private DataOutputStream outS;
    private boolean isWorking;
    private int connectTimeout = Config.SO_CONNECT_TIMEOUT;

    /**
     * Crate a SocketClient without any server address
     * Call setter to set serverAddress before connect
     */
	public SocketClient() {
		super();
	}

	/**
	 * Create a SocketClient without any Server Address
	 * @param serverAddress
	 */
	public SocketClient(InetSocketAddress serverAddress) {
		super();
		this.serverAddress = serverAddress;
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.IClient#connect()
	 */
	@Override
	public void connect() throws IOException {
        socket = new Socket();
        socket.setSoTimeout(connectTimeout);
        socket.setTcpNoDelay(true);
        socket.connect(serverAddress);
        this.isWorking = true;
        inS = new DataInputStream(socket.getInputStream());
        outS = new DataOutputStream(socket.getOutputStream());
        LOG.info("Client connected to address: {}", serverAddress);
	}

    /**
	 * This is the override of super method.
	 * @see org.apache.niolex.network.IClient#stop()
	 */
    @Override
	public void stop() {
        this.isWorking = false;
        try {
    		inS.close();
    		outS.close();
    		socket.close();
    	} catch(Exception e) {
    		LOG.error("Error occured when stop the server.", e);
    	}
    }

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
	 * Override super method
	 * @see org.apache.niolex.network.IPacketWriter#handleWrite(org.apache.niolex.network.PacketData)
	 */
	@Override
	public void handleWrite(PacketData sc) {
        try {
			sc.generateData(outS);
			LOG.debug("Packet sent. desc {}, length {}.", sc.descriptor(), sc.getLength());
			handleRead();
		} catch (IOException e) {
			throw new RejectedExecutionException("Failed to write packet to client.", e);
		}
	}

	public void handleRead() throws IOException {
		PacketData readPacket = new PacketData();
		while (true) {
			readPacket.parseHeader(inS);
			LOG.debug("Packet received. desc {}, size {}.", readPacket.descriptor(), readPacket.getLength());
			if (readPacket.getCode() == Config.CODE_HEART_BEAT) {
            	// Let's ignore the heart beat packet here.
            	continue;
            }
			packetHandler.handleRead(readPacket, this);
			break;
		}
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.IPacketWriter#attachData(java.lang.String, java.lang.Object)
	 */
	@Override
	public Object attachData(String key, Object value) {
		throw new UnsupportedOperationException("This method has not implemented yet.");
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.IPacketWriter#getAttached(java.lang.String)
	 */
	@Override
	public <T> T getAttached(String key) {
		throw new UnsupportedOperationException("This method has not implemented yet.");
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.IPacketWriter#size()
	 */
	@Override
	public int size() {
		return 0;
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.IClient#isWorking()
	 */
	@Override
	public boolean isWorking() {
		return this.isWorking;
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.IClient#setPacketHandler(org.apache.niolex.network.IPacketHandler)
	 */
	@Override
	public void setPacketHandler(IPacketHandler packetHandler) {
		this.packetHandler = packetHandler;
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.IClient#setConnectTimeout(int)
	 */
	@Override
	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.IClient#setServerAddress(java.net.InetSocketAddress)
	 */
	@Override
	public void setServerAddress(InetSocketAddress serverAddress) {
		this.serverAddress = serverAddress;
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.IClient#getServerAddress()
	 */
	@Override
	public InetSocketAddress getServerAddress() {
		return this.serverAddress;
	}

}
