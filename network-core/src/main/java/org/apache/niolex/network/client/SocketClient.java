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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.RejectedExecutionException;

import org.apache.niolex.network.Config;
import org.apache.niolex.network.PacketData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The blocking implementation of IClient. This client can only be used in one
 * thread. If you want to reuse client in multithreading, use #PacketClient
 *
 * We will try to read one packet from remote after send on packet. If you are
 * in the situation that server will not respond, please use #PacketClient
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-13
 */
public class SocketClient extends BaseClient {
	private static final Logger LOG = LoggerFactory.getLogger(SocketClient.class);

    private DataInputStream inS;
    private DataOutputStream outS;

    /**
     * Crate a SocketClient without any server address
     * Call setter to set serverAddress before connect
     */
	public SocketClient() {
		super();
	}

	/**
	 * Create a SocketClient with the Server Address
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
        inS = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        outS = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
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
    		LOG.info("Client stoped");
    	} catch(Exception e) {
    		LOG.error("Error occured when stop the server.", e);
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
			readPacket.parsePacket(inS);
			LOG.debug("Packet received. desc {}, size {}.", readPacket.descriptor(), readPacket.getLength());
			if (readPacket.getCode() == Config.CODE_HEART_BEAT) {
            	// Let's ignore the heart beat packet here.
            	continue;
            }
			packetHandler.handleRead(readPacket, this);
			break;
		}
	}

}
