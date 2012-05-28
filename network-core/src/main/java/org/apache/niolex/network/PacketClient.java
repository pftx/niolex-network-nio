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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The PacketClient connect to NioServer, send and receive packets.
 * @author Xie, Jiyun
 *
 */
public class PacketClient implements IPacketWriter {
	private static final Logger LOG = LoggerFactory.getLogger(PacketClient.class);

    private List<PacketData> sendPacketList = Collections.synchronizedList(new LinkedList<PacketData>());
    private InetSocketAddress serverAddress;
    private IPacketHandler packetHandler;
    private Socket socket;
    private Thread writeThread;
    private boolean isWorking;

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
     * Do real connect action. Start two separate threads for reads and writes.
     * This method will return immediately.
     * @throws IOException
     */
    public void connect() throws IOException {
    	this.isWorking = true;
        socket = new Socket();
        socket.connect(serverAddress);
        Thread tr = new Thread(new ReadLoop(socket.getInputStream()));
        tr.start();
        writeThread = new Thread(new WriteLoop(socket.getOutputStream()));
        writeThread.start();
        LOG.info("Client connected to address: {}", serverAddress);
    }

    /**
     * Stop this client.
     */
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
        if (writeThread != null) {
            writeThread.interrupt();
        }
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
                    packetHandler.handleRead(readPacket, PacketClient.this);
                }
            } catch(Exception e) {
                if (isWorking) {
                    LOG.error("Error occured in read loop.", e);
                } else {
                    LOG.info("Read loop stoped.");
                }
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
                    if (sendPacketList.size() == 0) {
                        // If nothing to send, let's sleep.
                        try {
                            Thread.sleep(100000);
                        } catch (InterruptedException e) {
                            // Let's ignore it.
                        }
                    } else {
                        sendNewPacket();
                    }
                }
                out.close();
                socket.close();
                LOG.info("Write loop stoped.");
            } catch(Exception e) {
                LOG.error("Error occured in write loop.", e);
            }
        }

        /**
         * Send new packet to remote server.
         * @throws IOException
         */
        public void sendNewPacket() throws IOException {
            PacketData sendPacket = sendPacketList.get(0);
            sendPacketList.remove(0);
            sendPacket.generateData(out);
        }
    }

    /**
     * @param packetHandler the packetHandler to set
     */
    public void setPacketHandler(IPacketHandler packetHandler) {
        this.packetHandler = packetHandler;
    }

	public InetSocketAddress getServerAddress() {
		return serverAddress;
	}

	public void setServerAddress(InetSocketAddress serverAddress) {
		this.serverAddress = serverAddress;
	}

	public boolean isWorking() {
		return isWorking;
	}

}
