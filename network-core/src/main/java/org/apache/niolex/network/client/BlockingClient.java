/**
 * BlockingClient.java
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
import java.net.SocketTimeoutException;

import org.apache.niolex.network.Config;
import org.apache.niolex.network.PacketData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The BlockingClient connect to NioServer, send packet one by one, but receive packets in it's
 * own thread. So this client can be used in multiple threads, and we save one write thread for
 * you compare to {@link PacketClient}.
 *
 * @author Xie, Jiyun
 * @version 1.0.8, Date: 2012-12-13
 */
public class BlockingClient extends BaseClient {
	private static final Logger LOG = LoggerFactory.getLogger(BlockingClient.class);

	/**
	 * The read loop instance.
	 */
	protected ReadLoop rLoop = new ReadLoop();

    /**
     * Create a BlockingClient without any Server Address.<br>
     * Call setter to set serverAddress before connect
     */
    public BlockingClient() {
		super();
	}

	/**
     * Create a BlockingClient with this Server Address.
     *
     * @param serverAddress
     */
    public BlockingClient(InetSocketAddress serverAddress) {
        super();
        this.serverAddress = serverAddress;
    }

    /**
     * {@inheritDoc}
     * Start one thread for read packets, write will be done in user thread.
     *
	 * This is the override of super method.
	 * @see org.apache.niolex.network.IClient#connect()
	 */
    @Override
	public void connect() throws IOException {
        prepareSocket();
        this.isWorking = true;
        Thread tr = new Thread(rLoop, "BlockingClient");
        tr.start();
        LOG.info("Blocking client connected to address: {}.", serverAddress);
    }

    /**
	 * This is the override of super method.
	 * @see org.apache.niolex.network.IClient#stop()
	 */
    @Override
	public void stop() {
        this.isWorking = false;
        safeClose();
    }

    @Override
    public synchronized void handleWrite(PacketData sc) {
        try {
            writePacket(sc);
            LOG.debug("Packet sent. desc {}.", sc.descriptor());
        } catch (IOException e) {
            // Throw an exception to the invoker.
            throw new IllegalStateException("Failed to send packet to server.", e);
        }
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
                    try {
                        PacketData readPacket = readPacket();
                        LOG.debug("Packet received. desc {}, size {}.", readPacket.descriptor(), readPacket.getLength());
                        if (readPacket.getCode() == Config.CODE_HEART_BEAT) {
                            // Let's ignore the heart beat packet here.
                            continue;
                        }
                        packetHandler.handlePacket(readPacket, BlockingClient.this);
                    } catch (SocketTimeoutException e) {
                        handleWrite(PacketData.getHeartBeatPacket());
                    }
                }
            } catch(Exception e) {
                if (isWorking) {
                    LOG.error("Error occured in read loop.", e);
                    // Notice!
                    /**
                     * Blocking Client Error will be handled in the Read Loop.
                     * So the Write Loop will just return, so there will be just one Error to the
                     * Upper layer.
                     */
                    BlockingClient.this.stop();
                    packetHandler.handleClose(BlockingClient.this);
                } else {
                    LOG.info("Read loop stoped.");
                }
            }
        }
    }

}
