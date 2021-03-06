/**
 * ConnectionCore.java
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
package org.apache.niolex.rpc.client.nio;

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
 * This is the Client side nio connection core of client packet processing component.
 * This is definitely the core of the whole non blocking network client.
 * This class handle network problem and reading, writing.
 * After data is ready, encapsulate it into Packet.
 * <br>
 * The connection core is working in semi-duplex mode, it will not read and write at the
 * same time. The whole status flow is:
 * SEND_HEADER -&gt; SEND_BODY -&gt; RECEVE_HEADER -&gt; RECEVE_BODY
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-8-17
 */
public class ConnectionCore {
	private static final Logger LOG = LoggerFactory.getLogger(ConnectionCore.class);

	/**
	 * The max buffer size. for packets small than this threshold will be send at one time.
	 */
	private static final int MAX_BUFFER_SIZE = Config.SERVER_NIO_BUFFER_SIZE;

    /**
     * Internal used in ConnectionCore. Please ignore.
     * Status indicate the current running status of read and write.
     * RECEVE_HEADER -&gt; Waiting to Read header from Remote
     * SEND_HEADER -&gt; Waiting to Write header into Socket Channel
     * RECEVE_BODY -&gt; Reading body
     * SEND_BODY -&gt; Writing body
     *
     * @author Xie, Jiyun
     */
    public static enum Status {
        RECEVE_HEADER, RECEVE_BODY, SEND_HEADER, SEND_BODY;
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
    private final ConnectionHolder connHolder;

    /**
     * The socket selection key of this channel.
     */
    private final SelectionKey selectionKey;

    /**
     * The head byte buffer.
     */
    private final ByteBuffer headBuffer = ByteBuffer.allocate(8);

    /**
     * The name of this client socket.
     */
    private String remoteName;

    /**
     * The Data buffer.
     */
    private ByteBuffer byteBuffer;
    private Status status;
    private Packet packet;

    /**
     * Constructor of ConnectionCore, manage a SocketChannel inside.
     *
     * @param selector the selector holder
     * @param channel the client side socket channel
     * @param connHolder the connection core manager
     * @throws IOException if I / O related error occurred
     */
    public ConnectionCore(SelectorHolder selector, SocketChannel channel, ConnectionHolder connHolder) throws IOException {
		super();
		this.selectorHolder = selector;
		this.socketChannel = channel;
        this.connHolder = connHolder;
		this.selectionKey = channel.register(selector.getSelector(), SelectionKey.OP_CONNECT, this);
    }

    /**
     * handle client connected request. We will do a log and mark this client ready.
     */
    public void handleConnect() {
    	try {
	    	socketChannel.finishConnect();
	    	selectionKey.interestOps(0);
	    	connHolder.ready(this);
    	} catch (Exception e) {
            LOG.error("Failed to mark socket channel as connected.", e);
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
     * Prepare to write this packet, attache this channel to write. This is the start of the
     * whole workflow.
     * 
     * @param pc the packet to be written to socket
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
            byteBuffer = getHeadBuffer();
            PacketUtil.putHeader(packet, byteBuffer);
        }
        // Make buffer ready for write to server.
        byteBuffer.flip();
        selectorHolder.changeInterestOps(selectionKey, SelectionKey.OP_WRITE);
    }

    /**
     * Clean the head buffer and return it.
     * 
     * @return the head buffer
     */
    private ByteBuffer getHeadBuffer() {
        headBuffer.clear();
        return headBuffer;
    }

    /**
     * Handle write request. called by NIO selector.
     * Send packets to server.
     *
     * Status change summary:
     * 
     * <pre>
     * SEND_HEADER -&gt; Sending a packet, header now
     * SEND_BODY -&gt; Sending a packet body
     * </pre>
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
        LOG.debug("Packet sent. desc {}, length {}.", packet.descriptor(), packet.getLength());
    	status = Status.RECEVE_HEADER;
    	byteBuffer = getHeadBuffer();
    	selectionKey.interestOps(SelectionKey.OP_READ);
    }

    /**
     * handle read request. called by NIO selector.
     *
     * Read status change summary:
     * 
     * <pre>
     * RECEVE_HEADER -&gt; Need read header, means nothing is read by now
     * RECEVE_BODY -&gt; Header is read, need to read body now.
     * </pre>
     *
     * @return true if read finished
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
     * @param blocker use this to release the result
     */
    public void readFinished(Blocker<Packet> blocker) {
    	LOG.debug("Packet received. desc {}, length {}.", packet.descriptor(), packet.getLength());
    	if (packet.getCode() == Config.CODE_HEART_BEAT) {
            // Let's ignore the heart beat packet here.
    	    return;
    	}
    	// Read packet finished, we need to invoke packet handler.
    	blocker.release(this, packet);
    	// Return this resource to the pool.
    	selectionKey.interestOps(0);
    	connHolder.ready(this);
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
            sb.append("Client disconnected from Remote [").append(remoteName).append("].");
            LOG.info(sb.toString());
        } catch (IOException e) {
            LOG.info("Failed to close client connection: {}", e.getMessage());
        } finally {
        	connHolder.close(this);
        }
    }

}
