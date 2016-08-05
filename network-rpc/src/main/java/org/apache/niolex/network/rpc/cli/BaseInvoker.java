/**
 * BaseInvoker.java
 *
 * Copyright 2016 the original author or authors.
 *
 * We licenses this file to you under the Apache License, version 2.0
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
package org.apache.niolex.network.rpc.cli;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.niolex.commons.concurrent.Blocker;
import org.apache.niolex.commons.concurrent.BlockerException;
import org.apache.niolex.commons.concurrent.WaitOn;
import org.apache.niolex.commons.util.SystemUtil;
import org.apache.niolex.network.Config;
import org.apache.niolex.network.ConnStatus;
import org.apache.niolex.network.IClient;
import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.rpc.RpcException;
import org.apache.niolex.network.rpc.util.RpcUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The base invoker which is able to handle multiple threads invocation concurrently.
 * Use this invoker along with the {@link org.apache.niolex.network.client.PacketClient} or
 * {@link org.apache.niolex.network.client.BlockingClient}
 *
 * @see org.apache.niolex.network.client.BlockingClient
 * @see org.apache.niolex.network.client.PacketClient
 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
 * @version 0.6.1
 * @since Aug 4, 2016
 */
public class BaseInvoker implements RemoteInvoker, IPacketHandler {
    private static final Logger LOG = LoggerFactory.getLogger(BaseInvoker.class);

    /**
     * The current waiting blocker.
     */
    private final Blocker<PacketData> blocker = new Blocker<PacketData>();

    /**
     * The low layer client to send and receive Rpc packets.
     */
    protected final IClient client;

    /**
     * The time to sleep between retry.
     */
    private int sleepBetweenRetryTime = Config.RPC_SLEEP_BT_RETRY;

    /**
     * The number of times to retry to get connected.
     */
    private int connectRetryTimes = Config.RPC_CONNECT_RETRY_TIMES;

    /**
     * The rpc handle timeout in milliseconds.
     */
    private int rpcHandleTimeout = Config.RPC_HANDLE_TIMEOUT;

    /**
     * The status of this Client.
     */
    protected volatile ConnStatus connStatus = ConnStatus.INNITIAL;

    /**
     * Construct a base invoker and set this object as the packet handler.
     * 
     * @param client the client used to communicate with server
     */
    public BaseInvoker(IClient client) {
        super();
        this.client = client;
        this.client.setPacketHandler(this);
    }

    /**
     * Connect the backed communication client to server, and set the internal status.
     *
     * @throws IOException if necessary
     */
    @Override
    public void connect() throws IOException {
        if (connStatus != ConnStatus.CONNECTED) {
            client.connect();
            connStatus = ConnStatus.CONNECTED;
        }
    }

    /**
     * Stop this client, and stop the backed communication client.
     */
    @Override
    public void stop() {
        if (connStatus != ConnStatus.CLOSED) {
            closeClient();
        }
    }

    /**
     * Check the client status before doing remote call and after got response.
     */
    public void checkStatus() {
        switch (connStatus) {
            case INNITIAL:
            case CONNECTING:
                throw new RpcException("Client not connected.", RpcException.Type.NOT_CONNECTED, null);
            case CLOSED:
                throw new RpcException("Client closed.", RpcException.Type.CONNECTION_CLOSED, null);
        }
    }

    /**
     * This is the override of super method.
     * 
     * @see org.apache.niolex.network.rpc.cli.RemoteInvoker#sendPacket(org.apache.niolex.network.PacketData)
     */
    @Override
    public void sendPacket(PacketData packet) {
        checkStatus();
        client.handleWrite(packet);
    }

    /**
     * This is the override of super method.
     * @see org.apache.niolex.network.rpc.cli.RemoteInvoker#invoke(org.apache.niolex.network.PacketData)
     */
    @Override
    public PacketData invoke(PacketData packet) {
        // 1. Set up the waiting information
        Integer key = RpcUtil.generateKey(packet);
        WaitOn<PacketData> waitOn = blocker.init(key);
        PacketData res = null;

        try {
            // 2. Send request to remote server
            checkStatus();
            client.handleWrite(packet);

            // 3. Wait for result.
            try {
                res = waitOn.waitForResult(rpcHandleTimeout);
            } catch (InterruptedException e) {
                throw new RpcException("Thread been interrupted.", RpcException.Type.INTERRUPTED, e);
            } catch (BlockerException e) {
                // This exception will not occur.
                checkStatus();
            }
        } finally {
            if (res == null) {
                // Release the key to prevent memory leak.
                blocker.release(key, packet);
            }
        }
        return res;
    }

    // ---------------------------------------------------------------------------------
    // Internal logics follows.
    // ---------------------------------------------------------------------------------

    /**
     * Release the waiting thread.
     * If there is no thread waiting for this packet, we do a info log.
     *
     * @see org.apache.niolex.network.IPacketHandler#handlePacket(org.apache.niolex.network.PacketData,
     *      org.apache.niolex.network.IPacketWriter)
     */
    @Override
    public void handlePacket(PacketData sc, IPacketWriter wt) {
        Integer key = RpcUtil.generateKey(sc);
        boolean isOk = blocker.release(key, sc);
        if (!isOk) {
            LOG.info("Packet received for key [{}] have no handler, just ignored.", key);
        }
    }

    /**
     * We will retry to connect to server in this method.
     *
     * @see org.apache.niolex.network.IPacketHandler#handleClose(org.apache.niolex.network.IPacketWriter)
     */
    @Override
    public void handleClose(IPacketWriter wt) {
        if (connStatus == ConnStatus.CLOSED) {
            return;
        }
        connStatus = ConnStatus.CONNECTING;
        if (!retryConnect()) {
            LOG.error("We can not re-connect to server after retry times, invoker will stop.");
            // Try to shutdown this Client, inform all the threads.
            closeClient();
        }
    }

    /**
     * Try to re-connect to server.
     *
     * @return true if connected
     */
    private boolean retryConnect() {
        for (int i = 0; i < connectRetryTimes; ++i) {
            SystemUtil.sleep(sleepBetweenRetryTime);
            LOG.info("Base invoker try to reconnect to server round {} ...", i);
            try {
                client.connect();
                this.connStatus = ConnStatus.CONNECTED;
                return true;
            } catch (IOException e) {
                // Not connected.
                LOG.info("Try to re-connect to server failed. {}", e.toString());
            }
        }
        return false;
    }

    /**
     * Close the backed client, and invoke the handle close method on the invoker.
     */
    protected void closeClient() {
        connStatus = ConnStatus.CLOSED;
        client.stop();

        // Release all the threads on hold.
        blocker.releaseAll();
    }

    /**
     * @return The string representation of the remote peer. i.e. The IP address.
     */
    @Override
    public String getRemoteAddress() {
        return client.getRemoteName();
    }

    /**
     * Set the server Internet address this client want to connect to.
     * This method must be called before {@link #connect()}
     *
     * @param serverAddress the server address
     */
    public void setServerAddress(InetSocketAddress serverAddress) {
        client.setServerAddress(serverAddress);
    }

    /**
     * Set the server Internet address this client want to connect to.
     * This method must be called before {@link #connect()}
     *
     * @param serverAddress the server address
     */
    public void setServerAddress(String serverAddress) {
        client.setServerAddress(serverAddress);
    }

    /**
     * Get Connection Status of this rpc client.
     *
     * @return current connection status
     */
    public ConnStatus getConnStatus() {
        return connStatus;
    }

    /**
     * Whether the connection is valid.
     *
     * @return true if this invoker is valid and ready to work.
     */
    @Override
    public boolean isReady() {
        return connStatus == ConnStatus.CONNECTED;
    }

    /**
     * Set the socket connect timeout.
     * This method must be called before {@link #connect()}
     *
     * @param timeout the connect timeout to set
     */
    public void setConnectTimeout(int timeout) {
        this.client.setConnectTimeout(timeout);
    }

    /**
     * Get the time in milliseconds that client with sleep between retry to connect
     * to server.
     *
     * @return the time in milliseconds
     */
    public int getSleepBetweenRetryTime() {
        return sleepBetweenRetryTime;
    }

    /**
     * Set the time in milliseconds that client with sleep between retry to connect
     * to server.
     *
     * @param sleepBetweenRetryTime the time to set
     */
    public void setSleepBetweenRetryTime(int sleepBetweenRetryTime) {
        this.sleepBetweenRetryTime = sleepBetweenRetryTime;
    }

    /**
     * Get the connect retry times.
     *
     * @return the retry times
     */
    public int getConnectRetryTimes() {
        return connectRetryTimes;
    }

    /**
     * Set the connect retry times.
     *
     * @param connectRetryTimes the connect retry times to set
     */
    public void setConnectRetryTimes(int connectRetryTimes) {
        this.connectRetryTimes = connectRetryTimes;
    }

    /**
     * Get the current RPC handle timeout.
     *
     * @return the current timeout
     */
    public int getRpcHandleTimeout() {
        return rpcHandleTimeout;
    }

    /**
     * The rpc holding thread will return null if the result is not ready after
     * this time.
     *
     * @param rpcHandleTimeout the timeout to set to
     */
    @Override
    public void setRpcHandleTimeout(int rpcHandleTimeout) {
        this.rpcHandleTimeout = rpcHandleTimeout;
    }

}
