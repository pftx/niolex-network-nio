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
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The basic NioServer reads and writes Packet.
 * @author Xie, Jiyun
 */
public class NioServer implements IServer {
    private static final Logger LOG = LoggerFactory.getLogger(NioServer.class);

    /**
     * The server socket channel, which is there the server listening.
     */
    private ServerSocketChannel ss;

    /**
     * The server accept and read selector, which is the main selector.
     */
    private Selector mainSelector;

    /**
     * The listen thread.
     */
    private Thread mainThread;

    /**
     * The signal whether there is anything to write.
     */
    private final Semaphore available = new Semaphore(0);

    /**
     * The server write selector.
     */
    private Selector writeSelector;

    /**
     * The write thread.
     */
    private Thread writeThread;

    /**
     * The packet handler.
     */
    private IPacketHandler packetHandler;

    /**
     * The current server status.
     */
    private volatile boolean isListening = false;

    private int acceptTimeOut = Config.SERVER_ACCEPT_TIMEOUT;

    private int heartBeatInterval = Config.SERVER_HEARTBEAT_INTERVAL;

    private int port;

    private Map<SocketChannel, ClientHandler> clientMap = new HashMap<SocketChannel, ClientHandler>();

    /**
     * Run this NioServer as a Runnable.
     * It call the listen() method internally.
	 * Override super method
	 * @see org.apache.niolex.network.IServer#start()
	 */
    @Override
	public boolean start() {
        try {
            ss = ServerSocketChannel.open();
            ss.socket().bind(new InetSocketAddress(this.getPort()));
            ss.configureBlocking(false);
            mainSelector = Selector.open();
            writeSelector = Selector.open();
            ss.register(mainSelector, SelectionKey.OP_ACCEPT);
            isListening = true;
            run();
            LOG.info("Server started at {}", this.getPort());
            return true;
        } catch (IOException e) {
            LOG.error("Failed to start server.", e);
        }
        return false;
    }

    private void run() {
        try {
        	mainThread = new Thread() {
        		public void run() {
                    // Listen the main loop.
        			try {
						listenMain();
					} catch (IOException e) {
			            LOG.error("Error occured while server is listening. The server will now shutdown.", e);
			            NioServer.this.stop();
					}
                }
        	};
        	writeThread = new Thread() {
        		public void run() {
                    // Listen the write loop.
        			try {
        				listenWrite();
					} catch (IOException e) {
			            LOG.error("Error occured while server is listening. The server will now shutdown.", e);
			            NioServer.this.stop();
					}
                }
        	};
        	mainThread.start();
        	writeThread.start();
        } catch (Exception e) {
            LOG.error("Error occured while server is listening. The server will now shutdown.", e);
            this.stop();
        }
    }

    /**
     * This method will never return after this server stop or IOException.
     * Call stop() to shutdown this server.
     * @throws IOException
     */
    private void listenMain() throws IOException {
        while (isListening) {
            // Setting the timeout for accept method. Avoid that this server can not be shut
            // down when this thread is waiting to accept.
            mainSelector.select(acceptTimeOut);
            Set<SelectionKey> selectionKeys = mainSelector.selectedKeys();
            for (SelectionKey selectionKey: selectionKeys) {
                handleKey(selectionKey);
            }
            selectionKeys.clear();
            Thread.yield();
        }
    }

    /**
     * This method will never return after this server stop or IOException.
     * Call stop() to shutdown this server.
     * @throws IOException
     */
    private void listenWrite() throws IOException {
    	while (isListening) {
    		try {
    			available.tryAcquire(10, TimeUnit.MILLISECONDS);
    			available.drainPermits();
    		} catch (Exception e) {
    			// To not need to deal this.
    		}
    		writeSelector.select(10);
    		Set<SelectionKey> selectionKeys = writeSelector.selectedKeys();
    		for (SelectionKey selectionKey: selectionKeys) {
    			handleKey(selectionKey);
    		}
    		selectionKeys.clear();
    	}
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
                client.register(mainSelector, SelectionKey.OP_READ);
                client.register(writeSelector, SelectionKey.OP_WRITE);
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
        } catch (Exception e) {
        	if (e instanceof CancelledKeyException || e instanceof ClosedChannelException) {
        		return;
        	}
            LOG.info("Failed to handle client socket: {}", e.toString());
        }
    }

    /**
	 * Override super method
	 * @see org.apache.niolex.network.IServer#stop()
	 */
    @Override
	public void stop() {
        isListening = false;
        try {
            ss.socket().close();
            ss.close();
            mainThread.interrupt();
            writeThread.interrupt();
        } catch (Exception e) {
            LOG.error("Failed to stop server.", e);
        }
        try {
        	mainThread.join();
        	writeThread.join();
	        mainSelector.close();
	        writeSelector.close();
        } catch (Exception e) {
            LOG.error("Failed to stop server.", e);
        }
        for (SocketChannel sc : clientMap.keySet()) {
        	try {
				sc.close();
			} catch (IOException e) {
				// Do nothing.
			}
        }
        clientMap.clear();
        LOG.info("Server stoped.");
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
    public class ClientHandler extends BasePacketWriter {

        /* 缓冲区大小*/
        private static final int BLOCK = Config.SERVER_NIO_BUFFER_SIZE;

        /* 发送数据缓冲区*/
        private ByteBuffer sendBuffer = ByteBuffer.allocate(BLOCK);
        private Status sendStatus;
        private PacketData sendPacket;
        private int sendPos;
        private long lastSentTime;

        /* 接收数据缓冲区*/
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

        @Override
		public void handleWrite(PacketData sc) {
			super.handleWrite(sc);
			// Signal the write thread there is data to write.
			available.release();
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
                int k = client.read(receiveBuffer);
                if (k < 0) {
                	// This socket is closed now.
                	handleClose();
                	return false;
                }
                if (receiveBuffer.position() > 0) {
                    if (receiveStatus == Status.HEADER && receiveBuffer.position() < 8) {
                        return false;
                    }
                    receiveBuffer.flip();

                    if (receiveStatus == Status.HEADER) {
                        receivePacket = new PacketData();
                        if (receivePacket.parseHeader(receiveBuffer)) {
                        	LOG.debug("Packet received. desc {}, size {}.", receivePacket.descriptor(), receivePacket.getLength());
                            packetHandler.handleRead(receivePacket, this);
                            receiveBuffer.compact();
                            return true;
                        } else {
                            receiveBuffer.clear();
                            receiveStatus = Status.BODY;
                        }
                    } else {
                        if (receivePacket.parseBody(receiveBuffer)) {
                        	LOG.debug("Packet received. desc {}, size {}.", receivePacket.descriptor(), receivePacket.getLength());
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
                handleClose();
            }
            return false;
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
                        	LOG.debug("Packet sent. desc {}, size {}.", sendPacket.descriptor(), sendPacket.getLength());
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
                handleClose();
            }
            return false;
        }

        /**
         * Start to send a new packet, or send the first packet.
         * @throws IOException
         */
        private boolean sendNewPacket() throws IOException {
        	sendPacket = super.handleNext();

            if (sendPacket == null) {
                if (lastSentTime + heartBeatInterval < System.currentTimeMillis()) {
                    super.handleWrite(PacketData.getHeartBeatPacket());
                }
                return false;
            }
            lastSentTime = System.currentTimeMillis();
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
         * Anyway, socket is closed.
         *
         * Need to close socket and clean Map.
         *
         * Call IPacketHandler.handleClose internally.
         */
        private void handleClose() {
        	try {
                clientMap.remove(client);
                client.close();
                StringBuilder sb = new StringBuilder();
                sb.append("Remote Client [").append(remoteName);
                sb.append("] disconnected.");
                LOG.info(sb.toString());
            } catch (IOException e) {
                LOG.info("Failed to close client socket: {}", e.getMessage());
            } finally {
            	packetHandler.handleClose(this);
            }
        }

    }

    /**
	 * Override super method
	 * @see org.apache.niolex.network.IServer#getPort()
	 */
    @Override
	public int getPort() {
        return this.port;
    }

    /**
	 * Override super method
	 * @see org.apache.niolex.network.IServer#setPort(int)
	 */
    @Override
	public void setPort(int port) {
        this.port = port;
    }

    /**
	 * Override super method
	 * @see org.apache.niolex.network.IServer#getPacketHandler()
	 */
    @Override
	public IPacketHandler getPacketHandler() {
        return packetHandler;
    }

    /**
	 * Override super method
	 * @see org.apache.niolex.network.IServer#setPacketHandler(org.apache.niolex.network.IPacketHandler)
	 */
    @Override
	public void setPacketHandler(IPacketHandler packetHandler) {
        this.packetHandler = packetHandler;
    }

    /**
	 * Override super method
	 * @see org.apache.niolex.network.IServer#getAcceptTimeOut()
	 */
    @Override
	public int getAcceptTimeOut() {
        return acceptTimeOut;
    }

    /**
	 * Override super method
	 * @see org.apache.niolex.network.IServer#setAcceptTimeOut(int)
	 */
    @Override
	public void setAcceptTimeOut(int acceptTimeOut) {
        this.acceptTimeOut = acceptTimeOut;
    }

    /**
	 * Override super method
	 * @see org.apache.niolex.network.IServer#getHeartBeatInterval()
	 */
    @Override
	public int getHeartBeatInterval() {
        return heartBeatInterval;
    }

    /**
	 * Override super method
	 * @see org.apache.niolex.network.IServer#setHeartBeatInterval(int)
	 */
    @Override
	public void setHeartBeatInterval(int heartBeatInterval) {
        this.heartBeatInterval = heartBeatInterval;
    }

}
