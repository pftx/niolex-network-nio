/**
 * FastCore.java
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
package org.apache.niolex.network.server;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.niolex.network.Config;
import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.PacketData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the fast core, the server side packet processing component.
 * This is definitely the core of the whole network server.<br>
 * For any reader want to understand this framework, read this class carefully.
 * We handle read, write, network error etc all here.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-8-17
 */
public class FastCore extends BasePacketWriter {
	private static final Logger LOG = LoggerFactory.getLogger(FastCore.class);

	/**
	 * The direct send buffer size.
	 */
	private static final int DIRECT_BUFFER_SIZE = Config.SERVER_DIRECT_BUFFER_SIZE;

	/**
	 * The buffer manager used to manage direct buffers.
	 */
	private static final BufferManager BUFFER_MANAGER = new BufferManager();

    /**
     * Internal used in FastCore. Please ignore.
     * Status indicate the running status of read and write.
     * NONE -> Not running
     * HEADER -> Reading(Writing) header
     * BODY -> Reading(Writing) body
     *
     * @author Xie, Jiyun
     *
     */
    public static enum Status {
        HEADER, BODY, NONE;
    }

    /**
     * The packet handler.
     */
    private final IPacketHandler packetHandler;

    /**
     * The server selector holding this handler.
     */
    private final SelectorHolder selector;

    /**
     * The socket channel this client handler is handling.
     */
    private final SocketChannel socketChannel;

    /**
     * The socket selection key of this channel.
     */
    private final SelectionKey selectionKey;

    /**
     * The head byte buffer.
     */
    private final ByteBuffer sendHeadBuffer = ByteBuffer.allocate(8);
    private final ByteBuffer recvHeadBuffer = ByteBuffer.allocate(8);

    /**
     * The direct small buffer, for faster send speed.
     */
    private final ByteBuffer directBuffer = BUFFER_MANAGER.getBuffer();

    /**
     * Current socket write status. attached to the selector or not.
     */
    private final AtomicBoolean writeAttached = new AtomicBoolean(false);

    /**
     * The client side name of this socket.
     */
    private String remoteName;


    /* 发送数据缓冲区*/
    private ByteBuffer sendBuffer;
    private Status sendStatus;
    private PacketData sendPacket;

    /* 接收数据缓冲区*/
    private ByteBuffer receiveBuffer;
    private Status receiveStatus;
    private PacketData receivePacket;

    /**
     * Constructor of FastCore, manage a SocketChannel inside.
     * We will register read operation to the selector in this method.
     *
     * @param packetHandler the packet handler
     * @param selector the socket selector
     * @param channel the socket channel
     */
    public FastCore(IPacketHandler packetHandler, SelectorHolder selector,
    		SocketChannel channel) throws IOException {
		super();
		this.packetHandler = packetHandler;
		this.selector = selector;
		this.socketChannel = channel;
		this.selectionKey = channel.register(selector.getSelector(), SelectionKey.OP_READ, this);

		// Initialize local variables.
        sendStatus = Status.NONE;
        receiveStatus = Status.HEADER;
        receiveBuffer = getReceiveBuffer();
        Socket so = channel.socket();
        // Initialize socket buffer.
        so.setTcpNoDelay(true);
        so.setSoLinger(false, 0);
        so.setSendBufferSize(Config.SO_BUFFER_SIZE);
        so.setReceiveBufferSize(Config.SO_BUFFER_SIZE);
        remoteName = so.getRemoteSocketAddress().toString();
        StringBuilder sb = new StringBuilder();
        sb.append("Remote Client [").append(remoteName);
        sb.append("] connected to local Port [").append(so.getLocalPort());
        sb.append("].");
        LOG.info(sb.toString());
    }

    /**
     * Clean the send head buffer and return it for use.
     *
     * @return the head buffer
     */
    private ByteBuffer getSendBuffer() {
        sendHeadBuffer.clear();
        return sendHeadBuffer;
    }

    /**
     * Clean the receive head buffer and return it for use.
     *
     * @return the head buffer
     */
    private ByteBuffer getReceiveBuffer() {
        recvHeadBuffer.clear();
        return recvHeadBuffer;
    }

    /**
     * {@inheritDoc}
     *
     * Override super method
     * @see org.apache.niolex.network.IPacketWriter#getRemoteName()
     */
    @Override
    public String getRemoteName() {
        return remoteName;
    }

    /**
     * Mainly use super method, we will attach the write operation to selector
     * if it's not attached yet.
     *
     * Override super method
     * @see org.apache.niolex.network.server.BasePacketWriter#handleWrite(org.apache.niolex.network.PacketData)
     */
    @Override
	public void handleWrite(PacketData sc) {
		super.handleWrite(sc);
		// Signal the selector there is data to write.
		if (writeAttached.compareAndSet(false, true)) {
			selector.changeInterestOps(selectionKey);
		}
	}

    /**
     * Handle read request. called by NIO selector.
     * If this method returns true, selector need to call this method again.
     * <pre>
     * Read status change summary:
     * HEADER -> Need read header, means nothing is read by now
     * BODY -> Header is read, need to read body now
     * </pre>
     * @return true if there are some more data needs to be read.
     */
    public boolean handleRead() {
        try {
            int k = socketChannel.read(receiveBuffer);
            if (k < 0) {
            	// This socket is closed now.
            	handleClose();
            	return false;
            }
            if (!receiveBuffer.hasRemaining()) {
                receiveBuffer.flip();
                if (receiveStatus == Status.HEADER) {
                	receivePacket = new PacketData();
                	receivePacket.parseHeader(receiveBuffer);
                	receiveBuffer = ByteBuffer.wrap(receivePacket.getData());
                	receiveStatus = Status.BODY;
                	socketChannel.read(receiveBuffer);
                	if (!receiveBuffer.hasRemaining()) {
                		packetFinished();
                    	return true;
                	}
                	return false;
                } else {
                	packetFinished();
                	return true;
                }
            }
        } catch (Exception e) {
            LOG.info("Failed to read data from client socket: {}", e.toString());
            handleClose();
        }
        return false;
    }

    /**
     * Read packet finished, we need to invoke packet handler, and
     * init another read cycle.
     */
    public void packetFinished() {
    	LOG.debug("Packet received. desc {}, size {}.", receivePacket.descriptor(), receivePacket.getLength());
    	// We send heart beat back directly, without notifying the packet handler.
    	if (receivePacket.getCode() == Config.CODE_HEART_BEAT) {
    		handleWrite(receivePacket);
    	} else {
    		packetHandler.handlePacket(receivePacket, this);
    	}
    	receiveStatus = Status.HEADER;
    	receiveBuffer = getReceiveBuffer();
    }

    /**
     * Handle write request. called by NIO selector.
     * Send packets to client when network is free.
     * <pre>
     * Status change summary:
     * NONE -> Nothing is sending now
     * HEADER -> Sending a packet, header now
     * BODY -> Sending a packet body
     * </pre>
     * @return true if there are some more free space to write to.
     */
    public boolean handleWrite() {
        try {
            if (sendStatus == Status.NONE) {
                return sendNewPacket();
            } else {
                if (sendBuffer.hasRemaining()) {
                    socketChannel.write(sendBuffer);
                    return !sendBuffer.hasRemaining();
                } else {
                    if (sendStatus == Status.HEADER) {
                    	sendStatus = Status.BODY;
                    	sendBuffer = ByteBuffer.wrap(sendPacket.getData());
                    	socketChannel.write(sendBuffer);
                        return !sendBuffer.hasRemaining();
                    } else {
                    	// Tell listener this packet has been sent just now.
                    	this.fireSendEvent(sendPacket);
                    	sendStatus = Status.NONE;
                    	LOG.debug("Packet sent. desc {}, size {}.", sendPacket.descriptor(), sendPacket.getLength());
                    	return sendNewPacket();
                    }
                }
            }
        } catch (Exception e) {
            LOG.info("Failed to send data to client socket: {}", e.toString());
            handleClose();
        }
        return false;
    }


    /**
     * Start to send a new packet.
     * If there is nothing to send, we will detach the write operation from selection key.
     *
     * @return true if there are some more free space to write to.
     * @throws IOException if I/O error occurred
     */
    private boolean sendNewPacket() throws IOException {
    	sendPacket = super.handleNext();
    	// If there is no packet in the queue, we try to reset attach flag.
        if (sendPacket == null) {
            writeAttached.set(false);
            // After we set the flag, we check the queue again.
            if (isEmpty()) {
                // Nothing to send, remove the OP_WRITE from selector.
                selectionKey.interestOps(SelectionKey.OP_READ);
                return false;
            } else {
                // Queue is not empty, we return true, system will redo the packet handle.
                return true;
            }
        } else {
        	return doSendNewPacket();
        }
    }

    /**
     * Do really send the packet.
     *
     * @return true if there are some more free space to write to.
     * @throws IOException if I/O error occurred
     */
    private boolean doSendNewPacket() throws IOException {
    	if (sendPacket.getLength() + 8 <= DIRECT_BUFFER_SIZE) {
    		// We send small packets in just one buffer.
    		sendStatus = Status.BODY;
    		sendBuffer = directBuffer;
    		sendBuffer.clear();
    		sendPacket.putHeader(sendBuffer);
    		sendBuffer.put(sendPacket.getData());
    	} else {
    		// Packet too large, we will send it multiple times.
    		sendStatus = Status.HEADER;
    		sendBuffer = getSendBuffer();
    		sendPacket.putHeader(sendBuffer);
    	}
    	sendBuffer.flip();
    	socketChannel.write(sendBuffer);
        return !sendBuffer.hasRemaining();
    }

    /**
     * Error occurred when read or write.
     * Anyway, socket is closed.
     * Need to close socket and invoke {@link #channelClosed()} to clean internal Maps.
     * <br>
     * We will call {@link IPacketHandler#handleClose(org.apache.niolex.network.IPacketWriter)} internally.
     */
    private void handleClose() {
    	try {
    	    // Close socket and streams.
            socketChannel.close();
            BUFFER_MANAGER.giveBack(directBuffer);
            StringBuilder sb = new StringBuilder();
            sb.append("Remote Client [").append(remoteName);
            sb.append("] disconnected.");
            LOG.info(sb.toString());
        } catch (Exception e) {
            LOG.info("Failed to close client socket: {}", e.toString());
        }
    	// Call this at last.
    	try {
            packetHandler.handleClose(this);
        } catch (Exception e) {
            // Catch this method for safety.
            LOG.info("Error occurred when invoke {}.handleClose(..)", packetHandler.getClass().getName(), e);
        }
    	// Super method, clean internal data structures.
    	channelClosed();
    }

}
