/**
 * PacketClient.java
 *
 * Copyright 2011 Niolex, Inc.
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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The PacketClient connect to NioServer, send and receive packets.
 * @author Xie, Jiyun
 *
 */
public class PacketClient implements IPacketWriter, IClient {
	private static final Logger LOG = LoggerFactory.getLogger(PacketClient.class);

	private LinkedBlockingDeque<PacketData> sendPacketList = new LinkedBlockingDeque<PacketData>();

    private InetSocketAddress serverAddress;
    private IPacketHandler packetHandler;
    private Socket socket;
    private Thread writeThread;
    private boolean isWorking;
    private int connectTimeout = Config.SO_CONNECT_TIMEOUT;


    /**
     * Create a PacketClient without any Server Address
     * Call setter to set serverAddress before connect
     */
    public PacketClient() {
		super();
	}

	/**
     * Create a PacketClient with this Server Address
     * @param serverAddress
     */
    public PacketClient(InetSocketAddress serverAddress) {
        super();
        this.serverAddress = serverAddress;
    }

    /**
	 * This is the override of super method.
	 * @see org.apache.niolex.network.IClient#connect()
	 */
    @Override
	public void connect() throws IOException {
        socket = new Socket();
        socket.setSoTimeout(connectTimeout);
        socket.connect(serverAddress);
        this.isWorking = true;
        Thread tr = new Thread(new ReadLoop(socket.getInputStream()));
        tr.start();
        writeThread = new Thread(new WriteLoop(socket.getOutputStream()));
        writeThread.start();
        LOG.info("Client connected to address: {}", serverAddress);
    }

    /**
	 * This is the override of super method.
	 * @see org.apache.niolex.network.IClient#stop()
	 */
    @Override
	public void stop() {
        this.isWorking = false;
        if (writeThread != null) {
            writeThread.interrupt();
        }
    }

    @Override
    public String getRemoteName() {
    	if (socket == null) {
    		return serverAddress.toString() + "-0000";
    	} else {
    		return serverAddress.toString() + "-" + socket.getLocalPort();
    	}
    }

    @Override
    public void handleWrite(PacketData sc) {
    	sendPacketList.add(sc);
    }

	@Override
	public Object attachData(String key, Object value) {
		throw new UnsupportedOperationException("This method has not implemented yet.");
	}

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
		return sendPacketList.size();
	}


    /**
     * The ReadLoop, reads packet from remote server over and over again.
     *
     * @author Xie, Jiyun
     *
     */
    public class ReadLoop implements Runnable {
        private DataInputStream in;

        /**
         * Create read loop by InputStream
         * @param in
         */
        public ReadLoop(InputStream in) {
            super();
            this.in = new DataInputStream(in);
        }

        /**
         * The real read loop.
         */
        public void run() {
            try {
                while (isWorking) {
                    PacketData readPacket = new PacketData();
                    readPacket.parseHeader(in);
                    LOG.debug("Packet received. desc {}, size {}.", readPacket.descriptor(), readPacket.getLength());
                    packetHandler.handleRead(readPacket, PacketClient.this);
                }
            } catch(Exception e) {
                if (isWorking) {
                    LOG.error("Error occured in read loop.", e);
                    // Notice!
                    /**
                     * Packet Client Error will be handled in the Read Loop.
                     * So the Write Loop will just return, so there will be just one Error to the
                     * Upper layer.
                     */
                    isWorking = false;
                    packetHandler.handleError(PacketClient.this);
                } else {
                    LOG.info("Read loop stoped.");
                }
            } finally {
            	try {
            		in.close();
            	} catch(Exception e) {}
            }
        }
    }

    /**
     * The WriteLoop, write packet to remote server when there is any.
     *
     * @author Xie, Jiyun
     *
     */
    public class WriteLoop implements Runnable {
        DataOutputStream out;

        /**
         * Create a WriteLoop by OutputStream
         * @param out
         */
        public WriteLoop(OutputStream out) {
            super();
            this.out = new DataOutputStream(out);
        }

        /**
         * The real write loop.
         */
        public void run() {
            try {
                while (isWorking) {
                    try {
                    	PacketData sendPacket = sendPacketList.takeFirst();
                    	if (sendPacket != null) {
                    		// If nothing to send, let's sleep.
                    		sendNewPacket(sendPacket);
                    	}
                    } catch (InterruptedException e) {
                        // Let's ignore it.
                    }
                }
                LOG.info("Write loop stoped.");
            } catch(Exception e) {
                LOG.error("Error occured in write loop.", e);
            } finally {
            	try {
            		out.close();
            		socket.close();
            	} catch(Exception e) {}
            }
        }

        /**
         * Send new packet to remote server.
         * @throws IOException
         */
        public void sendNewPacket(PacketData sendPacket) throws IOException {
            sendPacket.generateData(out);
            LOG.debug("Packet sent. desc {}, queue {}.", sendPacket.descriptor(), PacketClient.this.size());
        }
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

	public int getConnectTimeout() {
		return connectTimeout;
	}

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
