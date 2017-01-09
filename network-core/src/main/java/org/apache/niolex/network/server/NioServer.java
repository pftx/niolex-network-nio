/**
 * NioServer.java
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
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

import org.apache.niolex.commons.util.SystemUtil;
import org.apache.niolex.network.Config;
import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.IServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The base Nonblocking implementation of IServer.
 * Have only one thread process all the IO operations.
 *
 * @see MultiNioServer
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-6-11
 */
public class NioServer implements IServer, Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(NioServer.class);

    /**
     * The server socket channel, which is where the server is listening.
     */
    private ServerSocketChannel ss;

    /**
     * The server accept and read selector, which is the main selector.
     */
    private Selector mainSelector;

    /**
     * The selector holder enhancement, handle selector interests changes.
     */
    private SelectorHolder selectorHolder;

    /**
     * The listen thread.
     */
    private Thread mainThread;

    /**
     * The packet handler.
     */
    protected IPacketHandler packetHandler;

    /**
     * The current server listen status.
     */
    protected volatile boolean isListening = false;

    /**
     * The accept timeout, which is not important, user can leave it as it is.
     */
    protected int acceptTimeout = Config.SERVER_ACCEPT_TIMEOUT;

    /**
     * The current server port number. We will assign a default if user do not specify it.
     */
    protected int port = Config.SERVER_DEFAULT_PORT;

    /**
     * Start this Server and listen to the specified port.
     * It run a new thread to loop the main selector internally to handle accept request.
     *
     * Override super method
     * 
     * @see org.apache.niolex.network.IServer#start()
     */
    @Override
    public boolean start() {
        try {
            ss = ServerSocketChannel.open();
            ss.configureBlocking(false);
            ServerSocket so = ss.socket();
            so.setReceiveBufferSize(Config.SO_BUFFER_SIZE);
            so.setSoTimeout(Config.SO_CONNECT_TIMEOUT);
            so.setReuseAddress(Config.SO_REUSEADDR);

            so.bind(new InetSocketAddress(this.getPort()), Config.SO_BACKLOG);
            mainSelector = Selector.open();
            ss.register(mainSelector, SelectionKey.OP_ACCEPT);

            startLoop();
            LOG.info("Server started at {}", this.getPort());
            return true;
        } catch (Exception e) {
            LOG.error("Failed to start server.", e);
        }
        return false;
    }

    /**
     * This method will start a new thread.
     * Run selector internally.
     */
    private void startLoop() {
        isListening = true;
        mainThread = new Thread(this, "NioServer");
        selectorHolder = new SelectorHolder(mainThread, mainSelector);
        mainThread.start();
    }

    /**
     * This method will never return until this server was stopped or any Exception occurred.
     *
     * This is the override of super method.
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        try {
            // Listen the main loop.
            while (isListening) {
                // Setting the timeout for accept method. Avoid that this server can not be shut
                // down when this thread is waiting to accept.
                mainSelector.select(acceptTimeout);
                selectorHolder.changeAllInterestOps();
                Set<SelectionKey> selectedKeys = mainSelector.selectedKeys();
                for (SelectionKey selectionKey : selectedKeys) {
                    handleKey(selectionKey);
                }
                selectedKeys.clear();
            }
        } catch (Exception e) {
            LOG.error("Error occured while server is listening. The server will now shutdown.", e);
            stop();
        }
    }

    /**
     * Process all the IO requests, handle accept, read and write.
     * This method can not be overridden.
     * 
     * @param selectionKey the selection key to be handled
     */
    protected final void handleKey(SelectionKey selectionKey) {
        try {
            if (selectionKey.isAcceptable()) {
                SocketChannel client = ss.accept();
                // Try to ensure the returned client to be correct.
                if (client == null) {
                    return;
                }
                client.configureBlocking(false);
                // Register this client to a selector.
                registerClient(client);
                return;
            }
            FastCore fastCore = (FastCore) selectionKey.attachment();
            if (selectionKey.isValid() && selectionKey.isReadable()) {
                handleRead(fastCore);
            }
            if (selectionKey.isValid() && selectionKey.isWritable()) {
                handleWrite(fastCore);
            }
        } catch (Exception e) {
            if (e instanceof CancelledKeyException || e instanceof ClosedChannelException) {
                // Ignore these two exceptions.
                return;
            }
            LOG.info("Failed to handle socket: {}", e.toString());
        }
    }

    /**
     * {@link #handleKey(SelectionKey)} use This method to register the newly created
     * SocketChannel to a worker selector.<br>
     * This method will use the main selector to register network operations, and the
     * fast core will just run in the main thread.
     * <br>
     * Any Sub class can override this method to change the default behavior and use
     * there own selector.
     *
     * @param client the client socket channel
     * @throws IOException if I/O related error occurred
     */
    protected void registerClient(SocketChannel client) throws IOException {
        new FastCore(packetHandler, selectorHolder, client);
    }

    /**
     * Read packets from the socket channel.
     *
     * @param core the fast core
     */
    protected final void handleRead(FastCore core) {
        // Call this method repeatedly to empty the read buffer.
        while (core.handleRead())
            ;
    }

    /**
     * Write packets into the socket channel.
     *
     * @param core the fast core
     */
    protected final void handleWrite(FastCore core) {
        // Call this method repeatedly to fulfill the write buffer.
        while (core.handleWrite())
            ;
    }

    /**
     * Override super method
     * 
     * @see org.apache.niolex.network.IServer#stop()
     */
    @Override
    public void stop() {
        if (!isListening) {
            return;
        }
        // Mark the server as stopped, so main thread will return.
        isListening = false;
        try {
            ss.socket().close();
            ss.close();
            for (SelectionKey skey : mainSelector.keys()) {
                SystemUtil.close(skey.channel());
            }
            mainSelector.wakeup();
            mainThread.join();
            mainSelector.close();
            LOG.info("Server stoped.");
        } catch (Exception e) {
            LOG.error("Failed to stop server main thread.", e);
        }
    }

    /**
     * Override super method
     * 
     * @see org.apache.niolex.network.IServer#getPort()
     */
    @Override
    public int getPort() {
        return this.port;
    }

    /**
     * Override super method
     * 
     * @see org.apache.niolex.network.IServer#setPort(int)
     */
    @Override
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Override super method
     * 
     * @see org.apache.niolex.network.IServer#getPacketHandler()
     */
    @Override
    public IPacketHandler getPacketHandler() {
        return packetHandler;
    }

    /**
     * Override super method
     * 
     * @see org.apache.niolex.network.IServer#setPacketHandler(org.apache.niolex.network.IPacketHandler)
     */
    @Override
    public void setPacketHandler(IPacketHandler packetHandler) {
        this.packetHandler = packetHandler;
    }

    /**
     * Override super method
     * 
     * @see org.apache.niolex.network.IServer#getAcceptTimeout()
     */
    @Override
    public int getAcceptTimeout() {
        return acceptTimeout;
    }

    /**
     * Override super method
     * 
     * @see org.apache.niolex.network.IServer#setAcceptTimeout(int)
     */
    @Override
    public void setAcceptTimeout(int acceptTimeout) {
        this.acceptTimeout = acceptTimeout;
    }

}
