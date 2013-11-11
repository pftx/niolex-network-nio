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
package org.apache.niolex.network.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.apache.niolex.commons.stream.StreamUtil;
import org.apache.niolex.network.Config;
import org.apache.niolex.network.PacketData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The PacketClient connect to NioServer, send and receive packets in it's
 * own threads. This client can be used in multiple threads.
 *
 * @author Xie, Jiyun
 * @version 1.0.0, Date: 2012-6-13
 */
public class PacketClient extends BaseClient {
	private static final Logger LOG = LoggerFactory.getLogger(PacketClient.class);

	/**
	 * The queue to store all the out sending packets.
	 */
	private final LinkedBlockingDeque<PacketData> sendPacketList = new LinkedBlockingDeque<PacketData>();

	/**
	 * The internal write thread.
	 */
    private Thread writeThread;


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
     * Start two separate threads for reads and writes.
     *
	 * This is the override of super method.
	 * @see org.apache.niolex.network.IClient#connect()
	 */
    @Override
	public void connect() throws IOException {
        // Ensure resource closed.
        stop();
        // Start a new world.
        socket = new Socket();
        socket.setSoTimeout(connectTimeout);
        socket.setTcpNoDelay(true);
        socket.connect(serverAddress);
        this.isWorking = true;
        writeThread = new Thread(new WriteLoop(socket.getOutputStream()));
        writeThread.start();
        Thread tr = new Thread(new ReadLoop(socket.getInputStream()));
        tr.start();
        LOG.info("Packet client connected to address: {}", serverAddress);
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
            try {
                writeThread.join();
            } catch (InterruptedException e) {}
            writeThread = null;
        }
    }

    @Override
    public void handleWrite(PacketData sc) {
    	sendPacketList.add(sc);
    }

	/**
	 * Return the non-send packets size.
	 *
	 * @return current not-send packets size
	 */
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
        private final DataInputStream in;

        /**
         * Create read loop by InputStream
         * @param in
         */
        public ReadLoop(InputStream in) {
            super();
            this.in = new DataInputStream(new BufferedInputStream(in));
        }

        /**
         * The real read loop.
         */
        public void run() {
            try {
                while (isWorking) {
                    PacketData readPacket = new PacketData();
                    readPacket.parsePacket(in);
                    LOG.debug("Packet received. desc {}, size {}.", readPacket.descriptor(), readPacket.getLength());
                    if (readPacket.getCode() == Config.CODE_HEART_BEAT) {
                    	// Let's ignore the heart beat packet here.
                    	continue;
                    }
                    packetHandler.handlePacket(readPacket, PacketClient.this);
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
                    PacketClient.this.stop();
                    packetHandler.handleClose(PacketClient.this);
                } else {
                    LOG.info("Read loop stoped.");
                }
            } finally {
                StreamUtil.closeStream(in);
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
    	private final DataOutputStream out;

        /**
         * Create a WriteLoop by OutputStream
         * @param out
         */
        public WriteLoop(OutputStream out) {
            super();
            this.out = new DataOutputStream(new BufferedOutputStream(out));
        }

        /**
         * The real write loop.
         */
        public void run() {
            try {
                while (isWorking) {
                    try {

                    	/**
                    	 * The write thread wait on this queue till there is data.
                    	 */
                    	PacketData sendPacket = sendPacketList.pollFirst(connectTimeout / 2, TimeUnit.MILLISECONDS);
                    	if (sendPacket != null) {
                    		sendNewPacket(sendPacket);
                    	} else {
                    		// If nothing to send, let's send a heart beat.
                    		sendNewPacket(PacketData.getHeartBeatPacket());
                    	}
                    } catch (InterruptedException e) {
                        // Let's ignore it.
                    }
                }
                LOG.info("Write loop stoped.");
            } catch(Exception e) {
                LOG.error("Error occured in write loop.", e);
            } finally {
                StreamUtil.closeStream(out);
                safeClose();
            }
        }

        /**
         * Send new packet to remote server.
         * @throws IOException
         */
        public void sendNewPacket(PacketData sendPacket) throws IOException {
            sendPacket.generateData(out);
            LOG.debug("Packet sent. desc {}, queue size {}.", sendPacket.descriptor(), PacketClient.this.size());
        }
    }

}
