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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.niolex.commons.concurrent.ThreadUtil;
import org.apache.niolex.network.Config;
import org.apache.niolex.network.PacketData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The PacketClient connect to the specified NioServer, send and receive packets in two separate independent
 * threads. This client can be shared by multiple threads and use it concurrently, but it should be shared
 * only <b>after</b> it's connected to server.
 * <br>
 * This class is suitable for users who don't want to do any I/O in their own threads.
 * When user call {@link #handleWrite(PacketData)}, we will try to put the packet into our internal queue and
 * then we just return immediately. But if the internal queue is full, we will block on the queue to wait for
 * a room for the new packet. All the I/O will be handled in our internal threads.
 * <br>
 * The disadvantage of this class is that we will consume two threads. So don't create too many PacketClient in
 * one JVM.
 * 
 * @author Xie, Jiyun
 * @version 1.0.0, Date: 2012-6-13
 */
public class PacketClient extends BaseClient {
    private static final Logger LOG = LoggerFactory.getLogger(PacketClient.class);

    /**
     * The maximum input queue size.
     */
    private static final int MAX_QUEUE_SIZE = Config.CLIENT_MAX_QUEUE_SIZE;

    /**
     * The queue to store all the outstanding packets.
     */
    private final LinkedBlockingQueue<PacketData> sendPacketList = new LinkedBlockingQueue<PacketData>(MAX_QUEUE_SIZE);

    /**
     * The internal write thread.
     */
    private volatile Thread writeThread;

    /**
     * Create a PacketClient without any server address.<br>
     * Please call setter {@link #setServerAddress(InetSocketAddress)} or {@link #setServerAddress(String)} to set
     * server address before call {@link #connect()}.
     */
    public PacketClient() {
        super();
    }

    /**
     * Create a PacketClient with this specified server address.
     *
     * @param serverAddress the server address to connect to
     */
    public PacketClient(InetSocketAddress serverAddress) {
        super();
        this.serverAddress = serverAddress;
    }

    /**
     * {@inheritDoc}
     * Start two separate threads for reads and writes.
     *
     * @see org.apache.niolex.network.IClient#connect()
     */
    @Override
    public void connect() throws IOException {
        prepareSocket();
        this.isWorking = true;
        writeThread = new Thread(new WriteLoop(), "PacketClientW");
        writeThread.start();
        Thread tr = new Thread(new ReadLoop(), "PacketClientR");
        tr.start();
        LOG.info("Packet client connected to address: {}.", serverAddress);
    }

    /**
     * This is the override of super method.
     * 
     * @see org.apache.niolex.network.IClient#stop()
     */
    @Override
    public void stop() {
        this.isWorking = false;
        if (writeThread != null) {
            // The write thread will close the socket.
            writeThread.interrupt();
            ThreadUtil.join(writeThread);
            writeThread = null;
        }
    }

    /**
     * We put the packet into the internal queue. If the queue is not full, we will return immediately, otherwise
     * we will block until a space is available.
     *
     * @throws IllegalStateException when the queue is full and the thread is waiting and been interrupted
     * @see org.apache.niolex.network.IPacketWriter#handleWrite(org.apache.niolex.network.PacketData)
     */
    @Override
    public void handleWrite(PacketData sc) {
        try {
            sendPacketList.put(sc);
        } catch (InterruptedException e) {
            throw new IllegalStateException("Send List is Full, And thread is Interrupted when wait.", e);
        }
    }

    /**
     * Return the non-send packets size.
     *
     * @return current not-send packets size
     */
    public int outstandingSize() {
        return sendPacketList.size();
    }

    /**
     * The ReadLoop, reads packet from remote server over and over again.
     *
     * @author Xie, Jiyun
     *
     */
    public class ReadLoop implements Runnable {

        /**
         * Create read loop.
         */
        public ReadLoop() {
            super();
        }

        /**
         * The real read loop.
         */
        public void run() {
            try {
                while (isWorking) {
                    PacketData readPacket = readPacket();
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Packet received. desc {}, size {}.", readPacket.descriptor(), readPacket.getLength());
                    }
                    if (readPacket.getCode() == Config.CODE_HEART_BEAT) {
                        // Let's ignore the heart beat packet here.
                        continue;
                    }
                    packetHandler.handlePacket(readPacket, PacketClient.this);
                }
            } catch (Exception e) {
                if (isWorking) {
                    LOG.error("Error occured in read loop.", e);
                    /**
                     * Notice!! ~ Packet Client Error will be handled in the Read Loop, and
                     * the Write Loop will just return, so there will be just one Error thrown to the
                     * Upper layer.
                     */
                    PacketClient.this.stop();
                    packetHandler.handleClose(PacketClient.this);
                } else {
                    LOG.info("PacketClient Read loop stoped.");
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

        /**
         * Create a WriteLoop.
         */
        public WriteLoop() {
            super();
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
                        PacketData sendPacket = sendPacketList.poll(connectTimeout / 2, TimeUnit.MILLISECONDS);
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
                LOG.info("PacketClient Write loop stoped.");
            } catch (Exception e) {
                LOG.error("Error occured in write loop.", e);
            }
            // finally, we close the socket to make sure there's no resource leak.
            safeClose();
        }

        /**
         * Send new packet to remote server.
         * 
         * @param sendPacket the packet to be send to server
         * @throws IOException if I/O related error occurred
         */
        public void sendNewPacket(PacketData sendPacket) throws IOException {
            writePacket(sendPacket);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Packet sent. desc {}, queue size {}.", sendPacket.descriptor(), PacketClient.this.outstandingSize());
            }
        }
    }

}
