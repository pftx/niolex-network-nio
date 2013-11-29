/**
 * ClientManager.java
 *
 * Copyright 2013 The original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.niolex.network.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.niolex.commons.util.Runner;
import org.apache.niolex.commons.util.SystemUtil;
import org.apache.niolex.network.Config;
import org.apache.niolex.network.ConnStatus;
import org.apache.niolex.network.IClient;
import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.PacketData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class manage client connection status, retry and fail over.
 * User can set a list of server addresses into this class, and it
 * will pick up one randomly and try to connect to it.
 * <p>
 * User need to call {@link #connect()} manually to connect to server.
 * If this method return true, the client is connected, otherwise
 * it failed to connect to the first picked server, and we will try
 * to connect to others in the background.
 * <p>
 * If user want to make sure finally connect to one server, please
 * call {@link #waitForConnected()}. See to document of that method for
 * details.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5
 * @since 2013-1-11
 */
public class ClientManager {
    private static final Logger LOG = LoggerFactory.getLogger(ClientManager.class);

    /**
     * The time to sleep between retry.
     */
    private int sleepBetweenRetryTime = Config.RPC_SLEEP_BT_RETRY;

    /**
     * Times to retry get connected.
     */
    private int connectRetryTimes = Config.RPC_CONNECT_RETRY_TIMES;

    /**
     * The status of this Client.
     */
    private volatile ConnStatus connStatus;

    /**
     * The latch to wait for this manager to finally get a connection.
     */
    private volatile CountDownLatch connLatch;

    /**
     * The server address list
     */
    private List<InetSocketAddress> addressList;

    /**
     * The current using server address index
     */
    private int addressIndex;

    /**
     * The Client to be managed.
     */
    protected final IClient client;

    /**
     * Create a ClientManager to manage this client.
     *
     * @param client the client to be managed
     */
    public ClientManager(IClient client) {
        super();
        this.client = client;
        this.connStatus = ConnStatus.INNITIAL;
        this.connLatch = new CountDownLatch(1);
    }

    /**
     * Try to connect to one server.
     * We will only try once here, and if fail, we will start a background thread
     * to try all others and return false.
     *
     * @return true if connected, false otherwise.
     */
    public boolean connect() {
        if (addressList == null || addressList.size() == 0) {
            throw new IllegalArgumentException("Please set addressList first!");
        }
        // If client already connected, we just return.
        if (client.isWorking()) {
            return true;
        }
        try {
            doConnect();
            return true;
        } catch (IOException e) {
            LOG.warn("Error occured when try to connect to {}.", client.getServerAddress(), e);
            Runner.run(this, "retryConnect");
        }
        return false;
    }

    /**
     * Try to re-connect to server.
     * We will try at most {@link #connectRetryTimes} till we get a connection.
     *
     * @return true if connected, false otherwise
     */
    public boolean retryConnect() {
        if (addressList == null || addressList.size() == 0) {
            throw new IllegalArgumentException("Please set addressList first!");
        }
        // If client already connected, we just return.
        if (client.isWorking()) {
            return true;
        }
        this.connStatus = ConnStatus.CONNECTING;
        if (connLatch.getCount() == 0) {
            this.connLatch = new CountDownLatch(1);
        }
        for (int i = 0; i < connectRetryTimes; ++i) {
            SystemUtil.sleep(sleepBetweenRetryTime);
            LOG.info("RPC Client try to reconnect to server round {} ...", i);
            try {
                doConnect();
                return true;
            } catch (IOException e) {
                // Not connected.
                LOG.info("Try to re-connect to server failed. {}", e.toString());
            }
        }
        this.connStatus = ConnStatus.CLOSED;
        this.connLatch.countDown();
        return false;
    }

    /**
     * The internal method to do real connect.
     *
     * @throws IOException
     */
    private void doConnect() throws IOException {
        addressIndex = (addressIndex + 1) % addressList.size();
        client.setServerAddress(addressList.get(addressIndex));
        client.connect();
        this.connStatus = ConnStatus.CONNECTED;
        connLatch.countDown();
    }

    /**
     * Wait for we finally get a connection or been interrupted.
     * <p>
     * Notion! Please call {@link #connect()} or
     * {@link #retryConnect()} first! We will not start a thread
     * to try to connect to server in this method.
     *
     * @return true if connected, false if failed to connect to any server and we already
     * stopped to retry.
     * @throws InterruptedException if the current thread is interrupted while waiting
     */
    public boolean waitForConnected() throws InterruptedException {
        this.connLatch.await();
        return this.connStatus == ConnStatus.CONNECTED;
    }

    /**
     * Stop the retry, close the client, and mark this client manager
     * as closed.
     * <p>
     * We will set the {@link #connectRetryTimes} to 0 to stop the thread
     * try to reconnect. So if any user want to reuse this client manager
     * please re-set this field.
     */
    public void close() {
        this.connectRetryTimes = 0;
        this.client.stop();
        this.connStatus = ConnStatus.CLOSED;
    }

    /**
     * @return  the internal client
     */
    public IClient client() {
        return client;
    }

    /**
     * Get Connection Status of this client manager.
     *
     * @return current status
     */
    public ConnStatus getConnStatus() {
        return connStatus;
    }

    /**
     * Set the time in milliseconds that client with sleep between retry to connect
     * to server.
     *
     * @param sleepBetweenRetryTime
     */
    public void setSleepBetweenRetryTime(int sleepBetweenRetryTime) {
        this.sleepBetweenRetryTime = sleepBetweenRetryTime;
    }

    /**
     * @return the current sleepBetweenRetryTime
     */
    public int getSleepBetweenRetryTime() {
        return sleepBetweenRetryTime;
    }

    /**
     * Set retry times.
     *
     * @param connectRetryTimes
     */
    public void setConnectRetryTimes(int connectRetryTimes) {
        this.connectRetryTimes = connectRetryTimes;
    }

    /**
     * @return the current connectRetryTimes
     */
    public int getConnectRetryTimes() {
        return connectRetryTimes;
    }

    /**
     * Set the server address list.
     * We will shuffle it here in order to make it act more randomly.
     *
     * @param addressList
     */
    public void setAddressList(List<InetSocketAddress> addressList) {
        Collections.shuffle(addressList);
        this.addressList = addressList;
    }

    /**
     * Set the server address list.
     * We will shuffle it here in order to make it act more randomly.
     *
     * @param addressStr
     */
    public void setAddressList(String addressStr) {
        String[] addrArr = addressStr.split("[,; ]+");
        List<InetSocketAddress> list = new ArrayList<InetSocketAddress>();
        for (String addr : addrArr) {
            String[] aa = addr.split(":");
            list.add(new InetSocketAddress(aa[0], Integer.parseInt(aa[1])));
        }
        Collections.shuffle(list);
        this.addressList = list;
    }

    /**
     * @return the current addressList
     */
    public List<InetSocketAddress> getAddressList() {
        return addressList;
    }

    /**
     * Set the socket connect timeout.
     *
     * @param timeout the new connect timeout
     * @see org.apache.niolex.network.IClient#setConnectTimeout(int)
     */
    public void setConnectTimeout(int timeout) {
        this.client.setConnectTimeout(timeout);
    }

    /**
     * Write packet to server.
     *
     * @param sc the packet
     * @see org.apache.niolex.network.IPacketWriter#handleWrite(PacketData)
     */
    public void handleWrite(PacketData sc) {
        client.handleWrite(sc);
    }

    /**
     * Set packet handler.
     *
     * @param packetHandler the packet handler
     * @see org.apache.niolex.network.IClient#setPacketHandler(IPacketHandler)
     */
    public void setPacketHandler(IPacketHandler packetHandler) {
        client.setPacketHandler(packetHandler);
    }

}
