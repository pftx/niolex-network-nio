/**
 * SocketInvoker.java
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
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.niolex.commons.util.SystemUtil;
import org.apache.niolex.network.Config;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.client.BaseClient;
import org.apache.niolex.network.rpc.RpcException;
import org.apache.niolex.network.rpc.util.RpcUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
 * @version 0.6.1
 * @since Aug 16, 2016
 */
public class SocketInvoker extends BaseClient implements RemoteInvoker, Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(SocketInvoker.class);

    /**
     * Use this map to notify data to the corresponding thread.
     */
    private final ConcurrentMap<Integer, ArrayBlockingQueue<PacketData>> notifyMap = new ConcurrentHashMap<Integer, ArrayBlockingQueue<PacketData>>();

    /**
     * The thread who got this lock will read from socket.
     */
    private final Lock readLock = new ReentrantLock();

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
     * The stop status of this client.
     */
    protected volatile boolean isStoped = false;

    /**
     * The retry thread of this client.
     */
    protected volatile Thread thread;

    /**
     * Create a SocketInvoker with the specified server address.
     * 
     * @param serverAddress the server address to connect to
     */
    public SocketInvoker(InetSocketAddress serverAddress) {
        super();
        this.serverAddress = serverAddress;
    }

    /**
     * This is the override of super method.
     * @see org.apache.niolex.network.rpc.cli.RemoteInvoker#connect()
     */
    @Override
    public void connect() throws IOException {
        prepareSocket();
        this.isWorking = true;
        LOG.info("Socket invoker connected to address: {}.", serverAddress);
    }

    /**
     * This is the override of super method.
     * @see org.apache.niolex.network.rpc.cli.RemoteInvoker#stop()
     */
    @Override
    public void stop() {
        this.isWorking = false;
        this.isStoped = true;
        // Closing this socket will also close the socket's InputStream and OutputStream.
        Exception e = safeClose();
        if (e != null) {
            LOG.error("Error occured when stop the socket invoker.", e);
        }
        LOG.info("Socket invoker stoped.");
    }

    /**
     * This is the override of super method.
     * @see org.apache.niolex.network.rpc.cli.RemoteInvoker#sendPacket(org.apache.niolex.network.PacketData)
     */
    @Override
    public void sendPacket(PacketData packet) {
        handleWrite(packet);
    }

    /**
     * This is the override of super method.
     * @see org.apache.niolex.network.rpc.cli.RemoteInvoker#invoke(org.apache.niolex.network.PacketData)
     */
    @Override
    public PacketData invoke(PacketData packet) {
        // Set up the waiting information
        Integer key = RpcUtil.generateKey(packet);
        ArrayBlockingQueue<PacketData> answer = new ArrayBlockingQueue<PacketData>(1);
        notifyMap.put(key, answer);
        handleWrite(packet);

        long start = System.currentTimeMillis();
        while (true) {
            PacketData result = null;
            long sleep = start + rpcHandleTimeout - System.currentTimeMillis();
            if (sleep <= 0) {
                // Timeout, we clean the map, return null.
                notifyMap.remove(key);
                return null;
            }

            if (!readLock.tryLock()) {
                // Can not acquire the lock, some one is reading, we just wait for result.
                try {
                    result = answer.poll(sleep, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    // Ignore the exception.
                }
            } else {
                try {
                    result = readFromSocket(key);
                } catch (RpcException e) {
                    // Error occurred, clean the map, re-throw the exception.
                    notifyMap.remove(key);
                    throw e;
                } finally {
                    readLock.unlock();
                    // Notify another thread to read.
                    notifyAnotherThread();
                }
            }

            // Process done, check the result.
            if (result == null) {
                continue;
            } else {
                return result;
            }
        }
    }

    /**
     * Read packets from socket until we got our packet.
     * 
     * @param key the key used to match the response packet
     * @return the response packet
     */
    private PacketData readFromSocket(Integer key) {
        while (true) {
            try {
                PacketData resp = readPacket();
                Integer respKey = RpcUtil.generateKey(resp);

                // Notify the waiting thread, remove it from map for cleanup.
                ArrayBlockingQueue<PacketData> queue = notifyMap.remove(respKey);
                if (queue != null) {
                    queue.offer(resp);
                }

                // Return if we found the one.
                if (respKey.equals(key)) {
                    return resp;
                }
            } catch (IOException e) {
                LOG.info("I / O exception occurred when read from socket. {}", e.toString());
                checkStatus();
            }
        }
    }

    /**
     * Notify another thread to take over this thread to read packet from socket.
     */
    private void notifyAnotherThread() {
        Iterator<Entry<Integer, ArrayBlockingQueue<PacketData>>> iterator = notifyMap.entrySet().iterator();

        if (iterator.hasNext()) {
            try {
                iterator.next().getValue().put(null);
            } catch (InterruptedException e) {
                // Ignore the exception.
            }
        }
    }

    /**
     * This is the override of super method.
     * 
     * @see org.apache.niolex.network.IPacketWriter#handleWrite(org.apache.niolex.network.PacketData)
     */
    @Override
    public synchronized void handleWrite(PacketData sc) {
        try {
            writePacket(sc);
        } catch (IOException e) {
            checkStatus();
        }
    }

    /**
     * Check the connection status, throw exception if it's not valid. We will start a new thread to
     * retry to connect to server if necessary.
     */
    protected void checkStatus() {
        if (isStoped) {
            throw new RpcException("Client closed.", RpcException.Type.CONNECTION_CLOSED, null);
        }

        if (!socket.isConnected()) {
            isWorking = false;
            fireRetry();
            throw new RpcException("Client not connected.", RpcException.Type.NOT_CONNECTED, null);
        }
    }

    /**
     * Fire the retry thread to connect to server if necessary.
     */
    protected synchronized void fireRetry() {
        if (connectRetryTimes > 0 && thread == null) {
            thread = new Thread(this, "SocketInvoker-retry");
            thread.start();
        }
    }

    /**
     * This is the override of super method.
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        retryConnect();
        thread = null;
    }

    /**
     * Try to re-connect to server.
     *
     * @return true if connected
     */
    protected boolean retryConnect() {
        for (int i = 0; i < connectRetryTimes; ++i) {
            SystemUtil.sleep(sleepBetweenRetryTime);
            LOG.info("Base invoker try to reconnect to server round {} ...", i);
            try {
                prepareSocket();
                return isWorking = true;
            } catch (IOException e) {
                // Not connected.
                LOG.info("Try to re-connect to server failed. {}", e.toString());
            }
        }
        return false;
    }

    /**
     * This is the override of super method.
     * @see org.apache.niolex.network.rpc.cli.RemoteInvoker#getRemoteAddress()
     */
    @Override
    public String getRemoteAddress() {
        return getRemoteName();
    }

    /**
     * This is the override of super method.
     * @see org.apache.niolex.network.rpc.cli.RemoteInvoker#isReady()
     */
    @Override
    public boolean isReady() {
        return isWorking;
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
     * This is the override of super method.
     * 
     * @see org.apache.niolex.network.rpc.cli.RemoteInvoker#setRpcHandleTimeout(int)
     */
    @Override
    public void setRpcHandleTimeout(int rpcHandleTimeout) {
        this.rpcHandleTimeout = rpcHandleTimeout;
    }

}
