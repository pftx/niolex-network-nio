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

import org.apache.niolex.network.Packet;
import org.apache.niolex.network.PacketUtil;
import org.apache.niolex.rpc.server.SelectorHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the api core of server packet processing component.
 * This is definitely the core of the whole network server.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-8-17
 */
public class RpcCore {
	private static final Logger LOG = LoggerFactory.getLogger(RpcCore.class);

    /**
     * Internal used in FastCore. Please ignore.
     * Status indicate the running status of read and write.
     * HEADER -> Reading(Writing) header
     * BODY -> Reading(Writing) body
     *
     * @author Xie, Jiyun
     *
     */
    public static enum Status {
        RECEVE_HEADER, RECEVE_BODY, SEND_HEADER, SEND_BODY
    }

    /**
     * The packet handler.
     */
    private final Invoker invoker;

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
     * The name of this client socket.
     */
    private String remoteName;

    /*数据缓冲区*/
    private Status status;
    private ByteBuffer byteBuffer;
    private Packet packet;

    /**
     * Constructor of FastCore, manage a SocketChannel inside.
     *
     * @param invoker
     * @param selector
     * @param client
     */
    public RpcCore(Invoker invoker, SelectorHolder selector,
    		SocketChannel client) throws IOException {
		super();
		this.invoker = invoker;
		this.selectorHolder = selector;
		this.socketChannel = client;
		this.selectionKey = client.register(selector.getSelector(), SelectionKey.OP_READ, this);
		this.remoteName = client.socket().getRemoteSocketAddress().toString();

		// Initialize local variables.
        status = Status.RECEVE_HEADER;
        byteBuffer = ByteBuffer.allocate(8);

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
     * HEADER -> Need read header, means nothing is read by now
     * BODY -> Header is read, need to read body now
     *
     */
    public void handleRead() {
        try {
            int k = socketChannel.read(byteBuffer);
            if (k < 0) {
            	// This socket is closed now.
            	handleClose();
            	return;
            }
            if (!byteBuffer.hasRemaining()) {
                if (status == Status.RECEVE_HEADER) {
                	byteBuffer.flip();
                	packet = PacketUtil.parseHeader(byteBuffer);
                	byteBuffer = ByteBuffer.wrap(packet.getData());
                	status = Status.RECEVE_BODY;
                	socketChannel.read(byteBuffer);
                	if (!byteBuffer.hasRemaining()) {
                		readFinished();
                	}
                } else {
                	readFinished();
                }
            }
        } catch (Exception e) {
            LOG.info("Failed to read data from client socket: {}", e.toString());
            handleClose();
        }
    }

    /**
     * Read packet finished, we need to invoke packet handler.
     */
    public void readFinished() {
    	LOG.debug("Packet received. desc {}, size {}.", packet.descriptor(), packet.getLength());
    	packet = invoker.process(packet);
    	status = Status.SEND_HEADER;
    	byteBuffer = ByteBuffer.allocate(8);
    	PacketUtil.putHeader(packet, byteBuffer);
    	byteBuffer.flip();
    	selectorHolder.changeInterestOps(selectionKey, SelectionKey.OP_WRITE);
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
    	byteBuffer = ByteBuffer.allocate(8);
    	selectorHolder.changeInterestOps(selectionKey, SelectionKey.OP_READ);
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
    		selectionKey.cancel();
            socketChannel.close();
            StringBuilder sb = new StringBuilder();
            sb.append("Remote Client [").append(remoteName);
            sb.append("] disconnected.");
            LOG.info(sb.toString());
        } catch (IOException e) {
            LOG.info("Failed to close client socket: {}", e.getMessage());
        }
    }
}
