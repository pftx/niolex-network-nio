/**
 * NioServer.java
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
package org.apache.niolex.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The basic NioServer reads and writes Packet.
 * @author Xie, Jiyun
 */
public class NioServer implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(NioServer.class);

    private ServerSocketChannel ss;

    private Selector selector;

    private IPacketHandler packetHandler;

    private volatile boolean isListening = false;

    private int acceptTimeOut = 5000;

    private int heartBeatInterval = 10000;

    private int port;

    private Map<SocketChannel, ClientHandler> clientMap = new HashMap<SocketChannel, ClientHandler>();

    /**
     * Start the NioServer, bind to Port.
     * Need to call listen() after start manually
     */
    public boolean start() {
        try {
            ss = ServerSocketChannel.open();
            ss.socket().bind(new InetSocketAddress(this.getPort()));
            ss.configureBlocking(false);
            selector = Selector.open();
            ss.register(selector, SelectionKey.OP_ACCEPT);
            LOG.info("Server started at {}", this.getPort());
            return true;
        } catch (IOException e) {
            LOG.error("Failed to start server.", e);
        }
        return false;
    }

    /**
     * Run this NioServer as a Runnable.
     * It call the listen() method internally.
     */
    public void run() {
        try {
            listen();
        } catch (Exception e) {
            LOG.error("Error occured while server is listening.", e);
        }
    }

    /**
     * Run this NioServer manually.
     * This method will never return after this server stop or IOException.
     * Call stop() to shutdown this server.
     * @throws IOException
     */
    public void listen() throws IOException {
        isListening = true;
        while (isListening) {
            long startMil = System.nanoTime();
            // 选择�?��键，其相应的通道已为 I/O 操作准备就绪
            // Setting the timeout for accept method. Avoid can not be shut
            // down since blocking thread when waiting accept.
            selector.select(acceptTimeOut);
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            for (SelectionKey selectionKey: selectionKeys) {
                handleKey(selectionKey);
            }
            selectionKeys.clear();
            long timeMil = System.nanoTime() - startMil;
            // If the selector run too fast, so there is no data to send at all.
            // Consumed time small than 0.1 millisecond, sleep 1 millisecond.
            if (timeMil < 100000) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    // To not need to deal this.
                }
            }
        }
        LOG.info("Server is now shuting down.");
    }

    /**
     * handleKey use This method to create new ClientHandler
     * Any Sub class can override this method to change the behavior of ClientHandler.
     * The default client handler will just process in the main thread.
     * @param client
     * @return
     */
    protected ClientHandler getClientHandler(SocketChannel client) {
    	return new ClientHandler(client);
    }

    /**
     * Any Sub class can override this method to change the behavior of ClientHandler.
     * The default client handler will just process in the main thread.
     * @param clientHandler
     */
    protected void handleRead(ClientHandler clientHandler) {
    	// Call clientHandler.handleRead one time will just generate one Packet.
    	// If there are more Packet, we need to call it multiple times.
    	while (clientHandler.handleRead()) {
    		continue;
    	}
    }

    /**
     * Any Sub class can override this method to change the behavior of ClientHandler.
     * The default client handler will just process in the main thread.
     * @param clientHandler
     */
    protected void handleWrite(ClientHandler clientHandler) {
    	// Call clientHandler.handleWrite will just send one buffer.
    	// If that buffer is sent immediately, we need to call it again to send more data.
    	while (clientHandler.handleWrite()) {
    		continue;
    	}
    }


    /**
     * 处理请求
     */
    private void handleKey(SelectionKey selectionKey) throws IOException {
        SocketChannel client = null;
        try {
            if (selectionKey.isAcceptable()) {
                ServerSocketChannel server = (ServerSocketChannel) selectionKey.channel();
                client = server.accept();
                client.configureBlocking(false);
                clientMap.put(client, getClientHandler(client));
                client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            }
            if (selectionKey.isReadable()) {
                client = (SocketChannel) selectionKey.channel();
                ClientHandler clientHandler = clientMap.get(client);
                if (clientHandler != null) {
                	handleRead(clientHandler);
                }
            }
            if (selectionKey.isWritable()) {
                client = (SocketChannel) selectionKey.channel();
                ClientHandler clientHandler = clientMap.get(client);
                if (clientHandler != null) {
                	handleWrite(clientHandler);
                }
            }
        } catch (CancelledKeyException e) {
            LOG.info("Failed to handle client socket: {}", e.toString());
        }
    }

    /**
     * Stop this server.
     * After stop, the method listen() will return.
     */
    public void stop() {
        isListening = false;
        try {
            ss.socket().close();
            ss.close();
            selector.close();
        } catch (Exception e) {
            LOG.error("Failed to stop server.", e);
        }
    }

    /**
     * Internal used in ClientHandler. Please ignore.
     *
     * @author Xie, Jiyun
     *
     */
    public static enum Status {
        HEADER, BODY, NONE, DATA, SEND
    }

    /**
     * Handle Clients identified by SocketChannel.
     * Handle Reads and Writes by NIO.
     * @author Xie, Jiyun
     *
     */
    public class ClientHandler implements IPacketWriter {

        /* 缓冲区大�?*/
        private static final int BLOCK = 4096;

        /* 发�?数据缓冲�?*/
        private ByteBuffer sendBuffer = ByteBuffer.allocate(BLOCK);
        private Status sendStatus;
        private PacketData sendPacket;
        private int sendPos;
        private List<PacketData> sendPacketList = Collections.synchronizedList(new LinkedList<PacketData>());
        private long lastSentTime;

        /* 接收数据缓冲�?*/
        private ByteBuffer receiveBuffer = ByteBuffer.allocate(BLOCK);
        private Status receiveStatus;
        private PacketData receivePacket;
        private String remoteName;
        private SocketChannel client;

        /**
         * Constructor of ClientHandler, manage a SocketChannel inside.
         * @param sc
         */
        public ClientHandler(SocketChannel sc) {
            super();
            this.client = sc;
            sendStatus = Status.NONE;
            receiveStatus = Status.HEADER;
            remoteName = sc.socket().getRemoteSocketAddress().toString();
            lastSentTime = System.currentTimeMillis();
            StringBuilder sb = new StringBuilder();
            sb.append("Remote Client [").append(remoteName);
            sb.append("] connected to local Port [").append(sc.socket().getLocalPort());
            sb.append("].");
            LOG.info(sb.toString());
        }

        @Override
        public String getRemoteName() {
            return remoteName;
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
                client.read(receiveBuffer);
                if (receiveBuffer.position() > 0) {
                    if (receiveStatus == Status.HEADER && receiveBuffer.position() < 8) {
                        return false;
                    }
                    receiveBuffer.flip();

                    if (receiveStatus == Status.HEADER) {
                        receivePacket = new PacketData();
                        if (receivePacket.parseHeader(receiveBuffer)) {
                            packetHandler.handleRead(receivePacket, this);
                            receiveBuffer.compact();
                            return true;
                        } else {
                            receiveBuffer.clear();
                            receiveStatus = Status.BODY;
                        }
                    } else {
                        if (receivePacket.parseBody(receiveBuffer)) {
                            packetHandler.handleRead(receivePacket, this);
                            receiveStatus = Status.HEADER;
                            receiveBuffer.compact();
                            return true;
                        } else {
                            receiveBuffer.clear();
                        }
                    }
                }
            } catch (Exception e) {
                LOG.info("Failed to read data from client socket: {}", e.toString());
                handleError();
            }
            return false;
        }

        @Override
        public void handleWrite(PacketData sc) {
            sendPacketList.add(sc);
        }

        /**
         * Handle write request. called by NIO selector.
         * Send packets to client when there is any.
         *
         * Status change summary:
         * NONE -> Nothing is sending now
         * DATA -> Sending a packet, not finished yet, need to fill buffer again
         * SEND -> Sending a packet, all in the buffer. If buffer sent, can start new
         *
         */
        public boolean handleWrite() {
            try {
                if (sendStatus == Status.NONE) {
                    return sendNewPacket();
                } else {
                    if (sendPos != sendBuffer.limit()) {
                        sendPos = client.write(sendBuffer);
                        return sendPos == sendBuffer.limit();
                    } else {
                        if (sendStatus == Status.SEND) {
                        	sendStatus = Status.NONE;
                            return sendNewPacket();
                        } else {
                        	sendBuffer.clear();
                            if (sendPacket.generateData(sendBuffer)) {
                                sendStatus = Status.SEND;
                            }
                            sendBuffer.flip();
                            sendPos = client.write(sendBuffer);
                            return sendPos == sendBuffer.limit();
                        }
                    }
                }
            } catch (Exception e) {
                LOG.info("Failed to send data to client socket: {}", e.getMessage());
                handleError();
            }
            return false;
        }

        /**
         * Start to send a new packet, or send the first packet.
         * @throws IOException
         */
        private boolean sendNewPacket() throws IOException {
            if (sendPacketList.size() == 0) {
                if (lastSentTime + heartBeatInterval < System.currentTimeMillis()) {
                    sendPacketList.add(PacketData.getHeartBeatPacket());
                } else {
                    return false;
                }
            }
            lastSentTime = System.currentTimeMillis();
            sendPacket = sendPacketList.get(0);
            sendPacketList.remove(0);
            sendStatus = Status.DATA;
            sendBuffer.clear();
            if (sendPacket.generateData(sendBuffer)) {
                sendStatus = Status.SEND;
            }
            sendBuffer.flip();
            sendPos = client.write(sendBuffer);
            return sendPos == sendBuffer.limit();
        }

        /**
         * Error occurred when read or write.
         * Need to close socket and clean Map.
         * Call IPacketHandler.handleError internally.
         */
        private void handleError() {
            try {
                clientMap.remove(client);
                client.close();
            } catch (IOException e) {
                LOG.info("Failed to close client socket: {}", e.getMessage());
            } finally {
                packetHandler.handleError(this);
            }
        }
    }

    /**
     * The current listen port.
     * @return
     */
    public int getPort() {
        return this.port;
    }

    /**
     * Set listen port.
     * @param port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * @return the packetHandler
     */
    public IPacketHandler getPacketHandler() {
        return packetHandler;
    }

    /**
     * @param packetHandler the packetHandler to set
     */
    public void setPacketHandler(IPacketHandler packetHandler) {
        this.packetHandler = packetHandler;
    }

    /**
     * @return the acceptTimeOut
     */
    public int getAcceptTimeOut() {
        return acceptTimeOut;
    }

    /**
     * @param acceptTimeOut the acceptTimeOut to set
     */
    public void setAcceptTimeOut(int acceptTimeOut) {
        this.acceptTimeOut = acceptTimeOut;
    }

    /**
     * @return the heartBeatInterval
     */
    public int getHeartBeatInterval() {
        return heartBeatInterval;
    }

    /**
     * @param heartBeatInterval the heartBeatInterval to set
     */
    public void setHeartBeatInterval(int heartBeatInterval) {
        this.heartBeatInterval = heartBeatInterval;
    }

}
