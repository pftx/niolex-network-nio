/**
 * NioConnCore.java
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
package org.apache.niolex.rpc.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import org.apache.niolex.commons.concurrent.Blocker;
import org.apache.niolex.network.Config;
import org.apache.niolex.network.Packet;
import org.apache.niolex.network.PacketUtil;
import org.apache.niolex.rpc.core.SelectorHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the Client core of client packet processing component.
 * This is definitely the core of the whole non blocking network client.
 * This class handle network problem and reading, writing.
 * After data is ready, encapsulate it into Packet.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-8-17
 */
public class NioConnCore {
	private static final Logger LOG = LoggerFactory.getLogger(NioConnCore.class);

	/**
	 * The max buffer size. for packets small than this threshold will be send at one time.
	 */
	private static final int MAX_BUFFER_SIZE = Config.SERVER_NIO_BUFFER_SIZE;

    /**
     * Internal used in NioConnCore. Please ignore.
     * Status indicate the current running status of read and write.
     * RECEVE_HEADER -> Waiting to Read header from Remote
     * SEND_HEADER -> Waiting to Write header into Socket Channel
     * RECEVE_BODY -> Reading body
     * SEND_BODY -> Writing body
     *
     * @author Xie, Jiyun
     *
     */
    public static enum Status {
        RECEVE_HEADER, RECEVE_BODY, SEND_HEADER, SEND_BODY
    }

    /**
     * The client channel selector is held in this handler.
     */
    private final SelectorHolder selectorHolder;

    /**
     * The socket channel this client core is handling.
     */
    private final SocketChannel socketChannel;

    /**
     * The socket container hold all the sockets, including this one.
     */
    private final NioConnManager socketHolder;

    /**
     * The socket selection key of this channel.
     */
    private final SelectionKey selectionKey;

    /**
     * The name of this client socket.
     */
    private String remoteName;

    /*数据缓冲区*/
    private Status status;
    private ByteBuffer byteBuffer;
    private Packet packet;

    /**
     * Constructor of NioConnCore, manage a SocketChannel inside.
     *
     * @param selector
     * @param client
     * @param socketHolder
     * @throws IOException
     */
    public NioConnCore(SelectorHolder selector, SocketChannel client, NioConnManager socketHolder) throws IOException {
		super();
		this.selectorHolder = selector;
		this.socketChannel = client;
		this.socketHolder = socketHolder;
		this.selectionKey = client.register(selector.getSelector(), SelectionKey.OP_CONNECT, this);
    }

    /**
     * handle client connected request. We will do a log and mark this client ready.
     * @throws IOException
     */
    public void handleConnect() {
    	try {
	    	socketChannel.finishConnect();
	    	selectionKey.interestOps(0);
	    	socketHolder.ready(this);
    	} catch (Exception e) {
    		handleClose();
    		return;
    	}

    	this.remoteName = socketChannel.socket().getRemoteSocketAddress().toString();
        StringBuilder sb = new StringBuilder();
        sb.append("Local socket [").append(socketChannel.socket().getLocalPort());
        sb.append("] connected to remote address [").append(remoteName);
        sb.append("].");
        LOG.info(sb.toString());
    }

    /**
     * Prepare to write this packet, attache this channel to write.
     * @param pc
     */
    public void prepareWrite(Packet pc) {
    	packet = pc;
    	if (pc.getLength() + 8 <= MAX_BUFFER_SIZE) {
    		status = Status.SEND_BODY;
    		byteBuffer = ByteBuffer.allocate(8 + pc.getLength());
    		PacketUtil.putHeader(packet, byteBuffer);
    		byteBuffer.put(pc.getData());
    	} else {
    		status = Status.SEND_HEADER;
    		byteBuffer = ByteBuffer.allocate(8);
    		PacketUtil.putHeader(packet, byteBuffer);
    	}
    	// Make buffer ready for read.
    	byteBuffer.flip();
    	selectorHolder.changeInterestOps(selectionKey, SelectionKey.OP_WRITE);
    }

    /**
     * Handle write request. called by NIO selector.
     * Send packets to server.
     *
     * Status change summary:
     * HEADER -> Sending a packet, header now
     * BODY -> Sending a packet body
     *
     */
    public void handleWrite() {
        try {
        	socketChannel.write(byteBuffer);
        	if (!byteBuffer.hasRemaining()) {
        		if (status == Status.SEND_HEADER) {
        			status = Status.SEND_BODY;
        			byteBuffer = ByteBuffer.wrap(packet.getData());
                	socketChannel.write(byteBuffer);
                	if (!byteBuffer.hasRemaining()) {
                		writeFinished();
                	}
        		} else {
        			writeFinished();
        		}
        	}
        } catch (Exception e) {
            LOG.info("Failed to send data to server: {}", e.getMessage());
            handleClose();
        }
    }

    /**
     * Packet write finished, we need to try read data from server now.
     */
    public void writeFinished() {
    	// Send OK, try read.
    	status = Status.RECEVE_HEADER;
    	byteBuffer = ByteBuffer.allocate(8);
    	selectionKey.interestOps(SelectionKey.OP_READ);
    }

    /**
     * handle read request. called by NIO selector.
     *
     * Read status change summary:
     * HEADER -> Need read header, means nothing is read by now
     * BODY -> Header is read, need to read body now
     *
     *@return true if read finished.
     */
    public boolean handleRead() {
        try {
            int k = socketChannel.read(byteBuffer);
            if (k < 0) {
            	// This socket is closed now.
            	handleClose();
            	return false;
            }
            if (!byteBuffer.hasRemaining()) {
                if (status == Status.RECEVE_HEADER) {
                	byteBuffer.flip();
                	packet = PacketUtil.parseHeader(byteBuffer);
                	byteBuffer = ByteBuffer.wrap(packet.getData());
                	status = Status.RECEVE_BODY;
                	socketChannel.read(byteBuffer);
                	return !byteBuffer.hasRemaining();
                } else {
                	return true;
                }
            }
        } catch (Exception e) {
            LOG.info("Failed to read data from server: {}", e.toString());
            handleClose();
        }
        return false;
    }

    /**
     * Read packet finished, we will detach this channel from read.
     *
     * @param blocker use this to release the result.
     */
    public void readFinished(Blocker<Packet> blocker) {
    	LOG.debug("Packet received. desc {}, size {}.", packet.descriptor(), packet.getLength());
    	if (packet.getCode() == Config.CODE_HEART_BEAT) {
            // Let's ignore the heart beat packet here.
    	    return;
    	}
    	// Read packet finished, we need to invoke packet handler.
    	blocker.release(this, packet);
    	// Return this resource to the pool.
    	selectionKey.interestOps(0);
    	socketHolder.ready(this);
    }

    /**
     * Test whether this client core is valid.
     * @return true if it's valid
     */
    public boolean isValid() {
    	return this.selectionKey.isValid();
    }


    /**
     * Error occurred when read or write.
     * Anyway, socket is closed.
     *
     * Need to close socket and clean Map.
     *
     * Call selectionKey.cancel internally, and detach this from selectionKey.
     */
    private void handleClose() {
    	try {
    		// Make this key invalid.
    		selectionKey.cancel();
    		selectionKey.attach(null);
    		// Close channel.
            socketChannel.close();
            // Write LOG.
            StringBuilder sb = new StringBuilder();
            sb.append("Client disconnected from Remote [").append(remoteName);
            sb.append("].");
            LOG.info(sb.toString());
        } catch (IOException e) {
            LOG.info("Failed to close client socket: {}", e.getMessage());
        } finally {
        	socketHolder.close(this);
        }
    }
}
