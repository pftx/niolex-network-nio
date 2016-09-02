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
import java.util.NoSuchElementException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.niolex.commons.util.SystemUtil;
import org.apache.niolex.network.Config;
import org.apache.niolex.network.ConnStatus;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.client.BaseClient;
import org.apache.niolex.network.rpc.RpcException;
import org.apache.niolex.network.rpc.util.RpcUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The socket invoker, we do not need any extra thread to read from or write to socket, we
 * use the RPC thread to do this. This class is useful when there are too many socket connections
 * to be managed in one JVM.
 * 
 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
 * @version 0.6.1
 * @since Aug 16, 2016
 */
public class SocketInvoker extends BaseClient implements RemoteInvoker, Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(SocketInvoker.class);

    /**
     * Use this map to notify data to the corresponding thread.
     */
    private final ConcurrentMap<Integer, Notifier> notifyMap = new ConcurrentHashMap<Integer, Notifier>();

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
     * The connection status of this invoker.
     */
    protected volatile ConnStatus connStatus = ConnStatus.INNITIAL;

    /**
     * The retry thread of this client.
     */
    protected volatile Thread retryThread;

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
        this.connStatus = ConnStatus.CONNECTED;
        LOG.info("Socket invoker connected to address: {}.", serverAddress);
    }

    /**
     * This is the override of super method.
     * @see org.apache.niolex.network.rpc.cli.RemoteInvoker#stop()
     */
    @Override
    public void stop() {
        this.isWorking = false;
        this.connStatus = ConnStatus.CLOSED;
        releaseAll();

        // Closing this socket will also close the socket's InputStream and OutputStream.
        Exception e = safeClose();
        if (e != null) {
            LOG.error("Error occured when stop the socket invoker.", e);
        }
        LOG.info("Socket invoker stoped.");
    }

    /**
     * Release all the threads from waiting and clean the notify map.
     */
    protected void releaseAll() {
        for (Notifier n : notifyMap.values()) {
            n.interrupt();
        }
        // Clean the notify map.
        notifyMap.clear();
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
        Notifier notifier = new Notifier();
        ArrayBlockingQueue<PacketData> answer = notifier.getAnswer();
        notifyMap.put(key, notifier);
        handleWrite(packet);

        PacketData result = null;
        try {
            result = waitForResult(answer, key);
        } finally {
            // Cleanup.
            cleanAndNotify(result, key);
        }
        return result;
    }

    /**
     * Notify another thread to take over this thread to read packet from socket.
     * 
     * @param needClear whether we need clear the key from notify map
     * @param key the notify key used to clean from map
     */
    private void cleanAndNotify(Object result, Integer key) {
        if (result == null) {
            // Clean the notify map.
            notifyMap.remove(key);
        }

        // Clean the interrupt flag.
        Thread.interrupted();
        notifyIt(notifyMap.values().iterator());
    }

    /**
     * Notify the first thread in the iterator.
     * 
     * @param iterator the notify map iterator
     */
    protected void notifyIt(Iterator<Notifier> iterator) {
        if (iterator.hasNext()) {
            try {
                iterator.next().interrupt();
            } catch (NoSuchElementException e) {
                LOG.info("No one to notify when I want to hand over read lock.");
            }
        }
    }

    /**
     * Wait for the result from server, we will read from socket if necessary.
     * 
     * @param answer the blocking queue used to put answer
     * @param key the packet key used to find the result
     * @return the result
     */
    protected PacketData waitForResult(ArrayBlockingQueue<PacketData> answer, Integer key) {
        long end = System.currentTimeMillis() + rpcHandleTimeout;
        PacketData result = null;

        while (true) {
            long sleep = end - System.currentTimeMillis();
            // Case 1. Timeout or socket is broken.
            if (sleep <= 0 || connStatus != ConnStatus.CONNECTED) {
                return null;
            }

            if (readLock.tryLock()) {
                try {
                    // Case 2. We already got the answer.
                    if ((result = answer.poll()) != null) {
                        return result;
                    }

                    // Case 3. We try to read the answer.
                    result = readFromSocket(key);
                } finally {
                    readLock.unlock();
                }
            } else {
                // Case 4. Can not acquire the lock, some one is reading, we just wait for result.
                try {
                    result = answer.poll(sleep, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    // Ignore the exception.
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
    protected PacketData readFromSocket(Integer key) {
        try {
            while (true) {
                PacketData resp = readPacket();
                Integer respKey = RpcUtil.generateKey(resp);

                // Notify the waiting thread, remove it from map for cleanup.
                Notifier n = notifyMap.remove(respKey);
                if (n != null) {
                    n.answer.offer(resp);
                } else {
                    LOG.info("Packet with key {} don't have receiver.", respKey);
                }

                // Return if we found the one.
                if (respKey.equals(key)) {
                    return resp;
                }
            }
        } catch (IOException e) {
            throw checkStatus(e);
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
            throw checkStatus(e);
        }
    }

    /**
     * The socket is invalid, check the connection status. We will start a new thread to retry to connect to
     * server if necessary.
     * 
     * @param e the exception thrown from socket
     * @return the proper {@link RpcException}
     */
    protected RpcException checkStatus(IOException e) {
        if (connStatus == ConnStatus.CLOSED) {
            return new RpcException("Client closed.", RpcException.Type.CONNECTION_CLOSED, e);
        }

        if (connStatus == ConnStatus.CONNECTED) {
            connStatus = ConnStatus.CONNECTING;
            releaseAll();
            fireRetry();
        }

        return new RpcException("Client not connected.", RpcException.Type.NOT_CONNECTED, e);
    }

    /**
     * Fire the retry thread to connect to server if necessary.
     */
    protected synchronized void fireRetry() {
        if (connectRetryTimes > 0 && retryThread == null) {
            retryThread = new Thread(this, "SocketInvoker-retry");
            retryThread.start();
        }
    }

    /**
     * This is the override of super method.
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        if (!retryConnect()) {
            LOG.error("We can not re-connect to server after retry times, invoker will stop.");
            // Try to shutdown this invoker, inform all the threads.
            stop();
        }
        retryThread = null;
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
                connStatus = ConnStatus.CONNECTED;
                return true;
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
        return connStatus == ConnStatus.CONNECTED;
    }

    /**
     * Return whether this invoker is stopped or not.
     * 
     * @return true if this invoker was stopped, false otherwise
     */
    public boolean isStoped() {
        return connStatus == ConnStatus.CLOSED;
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

    /**
     * The class is used by the notify map.
     * 
     * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
     * @version 0.6.1
     * @since Aug 18, 2016
     */
    protected static final class Notifier {
        private final ArrayBlockingQueue<PacketData> answer;
        private final Thread thread;

        /**
         * Create a new notifier, we create a blocking queue inside and save the current thread.
         */
        public Notifier() {
            answer = new ArrayBlockingQueue<PacketData>(1);
            thread = Thread.currentThread();
        }

        /**
         * Get the blocking queue used to store answer.
         * 
         * @return the answer queue
         */
        public ArrayBlockingQueue<PacketData> getAnswer() {
            return answer;
        }

        /**
         * Interrupt the thread waiting on the answer queue.
         */
        public void interrupt() {
            thread.interrupt();
        }

    }
}
