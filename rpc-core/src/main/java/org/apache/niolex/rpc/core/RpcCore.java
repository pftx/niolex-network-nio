/**
 * RpcCore.java
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
package org.apache.niolex.rpc.core;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import org.apache.niolex.network.Config;
import org.apache.niolex.network.Packet;
import org.apache.niolex.network.PacketUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the Rpc core of server packet processing component.
 * This is definitely the core of the whole network server.
 * There is a {@link #ConnectionCore} class to handle client side network.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-8-17
 */
public class RpcCore {
	private static final Logger LOG = LoggerFactory.getLogger(RpcCore.class);

	/**
	 * The max buffer size. for packets small than this threshold will be send at one time.
	 */
	private static final int MAX_BUFFER_SIZE = Config.SERVER_NIO_BUFFER_SIZE;

    /**
     * Internal used in RpcCore. Please ignore.
     * Status indicate the running status of read and write.
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
     * The server selector holding this handler.
     */
    private final SelectorHolder selectorHolder;

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
    private final ByteBuffer headBuffer = ByteBuffer.allocate(8);

    /**
     * The name of the remote side of this socket.
     */
    private String remoteName;

    /*数据缓冲区*/
    private Status status;
    private ByteBuffer byteBuffer;
    private Packet packet;

    /**
     * Constructor of RpcCore, manage a SocketChannel inside.
     *
     * @param selector
     * @param client
     */
    public RpcCore(SelectorHolder selector, SocketChannel client) throws IOException {
		super();
		this.selectorHolder = selector;
		this.socketChannel = client;
		this.selectionKey = client.register(selector.getSelector(), SelectionKey.OP_READ, this);
		this.remoteName = client.socket().getRemoteSocketAddress().toString();

		// Initialize local variables, Prepare to read data.
        status = Status.RECEVE_HEADER;
        byteBuffer = headBuffer;

        StringBuilder sb = new StringBuilder();
        sb.append("Remote Client [").append(remoteName);
        sb.append("] connected to local Port [").append(client.socket().getLocalPort());
        sb.append("].");
        LOG.info(sb.toString());
    }


    /**
     * handle read request. called by NIO selector.
     *
     * Read status change summary:
     * RECEVE_HEADER -> Need read header, means nothing is read by now
     * RECEVE_BODY -> Header is read, need to read body now
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
            LOG.info("Failed to read data from client socket: {}", e.toString());
            handleClose();
        }
        return false;
    }

    /**
     * Read packet finished, we will detach this channel from read.
     */
    public Packet readFinished() {
    	LOG.debug("Packet received. desc {}, size {}.", packet.descriptor(), packet.getLength());
    	selectionKey.interestOps(0);
    	return packet;
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
    		byteBuffer = headBuffer;
    		headBuffer.clear();
    		PacketUtil.putHeader(packet, byteBuffer);
    	}
    	// Make buffer ready for read.
    	byteBuffer.flip();
    	selectorHolder.changeInterestOps(selectionKey, SelectionKey.OP_WRITE);
    }

    /**
     * Handle write request. called by NIO selector.
     * Send packets to client.
     *
     * Status change summary:
     * SEND_HEADER -> Sending a packet, header now
     * SEND_BODY -> Sending a packet body
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
            LOG.info("Failed to send data to client socket: {}", e.getMessage());
            handleClose();
        }
    }

    /**
     * Packet write finished, we need to try read again.
     */
    public void writeFinished() {
    	// Send OK, try read.
    	status = Status.RECEVE_HEADER;
    	byteBuffer = headBuffer;
        headBuffer.clear();
    	selectionKey.interestOps(SelectionKey.OP_READ);
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
            sb.append("Remote Client [").append(remoteName);
            sb.append("] disconnected.");
            LOG.info(sb.toString());
        } catch (IOException e) {
            LOG.info("Failed to close client socket: {}", e.getMessage());
        }
    }
}
