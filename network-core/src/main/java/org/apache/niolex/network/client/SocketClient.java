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

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.niolex.commons.util.Runner;
import org.apache.niolex.network.Config;
import org.apache.niolex.network.PacketData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The blocking implementation of IClient. We use synchronized methods to handle both read and write.
 * Users maybe use this class in multi-threads, but you need to handle packet read carefully, because
 * the packet you read maybe not the expected response of your sent packet. So we recommend you to use
 * it only in one thread.
 * <br>
 * If you set {@link #setAutoRead(boolean)} to true, then we will read one packet after write every
 * packet. (Heart beat packet will be ignored).<br>
 * If it's set to false, we will not read from server, please invoke {@link #handleRead()} manually.
 * <br>
 * If you want to let us handle read operations, use {@link PacketClient} or {@link BlockingClient}
 * instead.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0, Date: 2012-6-13
 */
public class SocketClient extends BaseClient {
    private static final Logger LOG = LoggerFactory.getLogger(SocketClient.class);

    /**
     * Automatically read data from remote server.
     */
    private boolean autoRead = true;

    /**
     * Crate a SocketClient without any server address.<br>
     * Please call setter {@link #setServerAddress(InetSocketAddress)} or {@link #setServerAddress(String)} to set
     * server address before call {@link #connect()}.
     */
    public SocketClient() {
        super();
    }

    /**
     * Create a SocketClient with the specified server address.
     * 
     * @param serverAddress the server address to connect to
     */
    public SocketClient(InetSocketAddress serverAddress) {
        super();
        this.serverAddress = serverAddress;
    }

    /**
     * Override super method
     * 
     * @see org.apache.niolex.network.IClient#connect()
     */
    @Override
    public void connect() throws IOException {
        prepareSocket();
        this.isWorking = true;
        LOG.info("Socket client connected to address: {}.", serverAddress);
    }

    /**
     * This is the override of super method.
     * 
     * @see org.apache.niolex.network.IClient#stop()
     */
    @Override
    public void stop() {
        this.isWorking = false;
        // Closing this socket will also close the socket's InputStream and OutputStream.
        Exception e = safeClose();
        if (e != null) {
            LOG.error("Error occured when stop the socket client.", e);
        }
        LOG.info("Socket client stoped.");
    }

    /**
     * Override super method
     * 
     * @see org.apache.niolex.network.IPacketWriter#handleWrite(org.apache.niolex.network.PacketData)
     */
    @Override
    public synchronized void handleWrite(PacketData sc) {
        try {
            writePacket(sc);
            LOG.debug("Packet sent. desc {}, length {}.", sc.descriptor(), sc.getLength());
            if (autoRead) {
                handleRead();
            }
        } catch (IOException e) {
            // When IO exception occurred, this socket is invalid, we need to close it.
            if (this.isWorking) {
                LOG.error("Error occured, SocketClient forced to stop.", e);
                stop();
                // Notify the handler. We use a new thread to do this, because the packet handler might
                // want to recover the connection status, which will be time-consuming.
                Runner.run(packetHandler, "handleClose", this);
            }
            // Throw an exception to the invoker.
            throw new IllegalStateException("Failed to send packet to server.", e);
        }
    }

    /**
     * Read one packet from remote server. If we read a heart beat, we will
     * retry again until we read a real packet.
     *
     * @throws IOException if network problem occurred
     */
    public synchronized void handleRead() throws IOException {
        while (true) {
            PacketData readPacket = readPacket();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Packet received. desc {}, size {}.", readPacket.descriptor(), readPacket.getLength());
            }
            if (readPacket.getCode() == Config.CODE_HEART_BEAT) {
                // Let's ignore the heart beat packet here.
                continue;
            }
            packetHandler.handlePacket(readPacket, this);
            break;
        }
    }

    /**
     * @return The current auto read status.
     */
    public boolean isAutoRead() {
        return autoRead;
    }

    /**
     * Set whether you need we automatically read one data packet for you after we finished one packet write.
     *
     * @param autoRead the new autoRead status
     */
    public void setAutoRead(boolean autoRead) {
        this.autoRead = autoRead;
    }

}
