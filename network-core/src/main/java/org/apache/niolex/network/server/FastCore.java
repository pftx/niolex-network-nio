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
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.niolex.network.Config;
import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.PacketData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the fast core of server side packet processing component.
 * This is definitely the core of the whole network server.
 * We handle read, write, network error etc all here.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-8-17
 */
public class FastCore extends BasePacketWriter {
	private static final Logger LOG = LoggerFactory.getLogger(FastCore.class);

	/**
	 * The max send buffer size.
	 */
	private static final int MAX_BUFFER_SIZE = Config.SERVER_NIO_BUFFER_SIZE;

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
        HEADER, BODY, NONE
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
     * The name of the client side of this socket.
     */
    private String remoteName;

    /**
     * Current socket write status. attached to the selector or not.
     */
    private boolean writeAttached = false;

    /**
     * The lock to control socket write attach & detach.
     */
    private Lock writeLock = new ReentrantLock();


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
     * @param packetHandler
     * @param selector
     * @param client
     */
    public FastCore(IPacketHandler packetHandler, SelectorHolder selector,
    		SocketChannel client) throws IOException {
		super();
		this.packetHandler = packetHandler;
		this.selector = selector;
		this.socketChannel = client;
		this.selectionKey = client.register(selector.getSelector(), SelectionKey.OP_READ, this);

		// Initialize local variables.
        sendStatus = Status.NONE;
        receiveStatus = Status.HEADER;
        receiveBuffer = getHeadBuffer(false);
        remoteName = client.socket().getRemoteSocketAddress().toString();
        StringBuilder sb = new StringBuilder();
        sb.append("Remote Client [").append(remoteName);
        sb.append("] connected to local Port [").append(client.socket().getLocalPort());
        sb.append("].");
        LOG.info(sb.toString());
    }

    /**
     * Clean the head buffer and return it.
     *
     * @return the head buffer
     */
    private ByteBuffer getHeadBuffer(boolean isSend) {
        if (isSend) {
            sendHeadBuffer.clear();
            return sendHeadBuffer;
        } else {
            recvHeadBuffer.clear();
            return recvHeadBuffer;
        }
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
		if (!writeAttached) {
			writeLock.lock();
			try {
				if (!writeAttached) {
					writeAttached = true;
					selector.changeInterestOps(selectionKey);
				}
			} finally {
				writeLock.unlock();
			}
		}
	}

    /**
     * handle read request. called by NIO selector.
     * This method will read packet over and over again. Never stop.
     *
     * Read status change summary:
     * HEADER -> Need read header, means nothing is read by now
     * BODY -> Header is read, need to read body now
     *
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
                		readFinished();
                    	return true;
                	}
                	return false;
                } else {
                	readFinished();
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
    public void readFinished() {
    	LOG.debug("Packet received. desc {}, size {}.", receivePacket.descriptor(), receivePacket.getLength());
    	// We send heart beat back directly, without notifying the packet handler.
    	if (receivePacket.getCode() == Config.CODE_HEART_BEAT) {
    		handleWrite(receivePacket);
    	} else {
    		packetHandler.handleRead(receivePacket, this);
    	}
    	receiveStatus = Status.HEADER;
    	receiveBuffer = getHeadBuffer(false);
    }

    /**
     * Handle write request. called by NIO selector.
     * Send packets to client when there is any.
     *
     * Status change summary:
     * NONE -> Nothing is sending now
     * HEADER -> Sending a packet, header now
     * BODY -> Sending a packet body
     *
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
                    	// Tell listener this packet has been send now.
                    	this.fireSendEvent(sendPacket);
                    	sendStatus = Status.NONE;
                    	LOG.debug("Packet sent. desc {}, size {}.", sendPacket.descriptor(), sendPacket.getLength());
                    	return sendNewPacket();
                    }
                }
            }
        } catch (Exception e) {
            LOG.info("Failed to send data to client socket: {}", e.getMessage());
            handleClose();
        }
        return false;
    }


    /**
     * Start to send a new packet.
     * If there is nothing to send, we will detach the write operation from selection key.
     *
     * @throws IOException
     */
    private boolean sendNewPacket() throws IOException {
    	sendPacket = super.handleNext();

        if (sendPacket == null) {
        	writeLock.lock();
			try {
				// We will redo the packet handle here.
				if (super.isEmpty()) {
					// Nothing to send, remove the OP_WRITE from selector.
					writeAttached = false;
					selectionKey.interestOps(SelectionKey.OP_READ);
				} else {
					return true;
				}
			} catch (Exception e) {
				LOG.warn("Failed to detach write from selector, client will stop. ", e.toString());
				handleClose();
			} finally {
				writeLock.unlock();
            }
            return false;
        } else {
        	return doSendNewPacket();
        }
    }

    /**
     * Do really send the packet.
     *
     * @return
     * @throws IOException
     */
    private boolean doSendNewPacket() throws IOException {
    	if (sendPacket.getLength() + 8 < MAX_BUFFER_SIZE) {
    		// We send small packets in just one buffer.
    		sendStatus = Status.BODY;
    		sendBuffer = ByteBuffer.allocate(sendPacket.getLength() + 8);
    		sendPacket.putHeader(sendBuffer);
    		sendBuffer.put(sendPacket.getData());
    	} else {
    		// Packet too large, we will send it multiple times.
    		sendStatus = Status.HEADER;
    		sendBuffer = getHeadBuffer(true);
    		sendPacket.putHeader(sendBuffer);
    	}
    	sendBuffer.flip();
    	socketChannel.write(sendBuffer);
        return !sendBuffer.hasRemaining();
    }

    /**
     * Error occurred when read or write.
     * Anyway, socket is closed.
     *
     * Need to close socket and clean Map.
     *
     * Call IPacketHandler.handleClose internally.
     */
    private void handleClose() {
    	try {
            socketChannel.close();
            StringBuilder sb = new StringBuilder();
            sb.append("Remote Client [").append(remoteName);
            sb.append("] disconnected.");
            LOG.info(sb.toString());
        } catch (IOException e) {
            LOG.info("Failed to close client socket: {}", e.getMessage());
        } finally {
        	try {
        		packetHandler.handleClose(this);
        	} finally {
        		super.channelClosed();
        	}
        }
    }

}
