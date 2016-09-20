/**
 * MultiAddressClient.java
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
package org.apache.niolex.rpc.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.niolex.network.Packet;
import org.apache.niolex.rpc.RpcException;

/**
 * This client encapsulate the {@link SocketClient}, provide
 * a container to handle multiple server addresses.
 * <p>
 * Server address need to be the following format:
 * IP:PORT/CONNECTION_NUMBER;IP:PORT/CONNECTION_NUMBER
 * i.e. -> 10.1.2.3:8090/10;10.1.2.4:8090/8
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5
 * @since 2013-2-27
 */
public class MultiAddressClient extends BaseClient {

    /**
     * Store the client list.
     */
    private final List<SocketClient> clientList = new ArrayList<SocketClient>();

    /**
     * The current server index.
     */
    private final AtomicInteger curIdx = new AtomicInteger(-1);

    /**
     * Create a MultiAddressClient with this server address.
     * address must in this format:
     * IP:PORT/CONNECTION_NUMBER;IP:PORT/CONNECTION_NUMBER ...
     * Multiple address separated by ";"
     *
     * @param serverAddress the multiple server addresses
     */
    public MultiAddressClient(String serverAddress) {
        super();
        String[] arr = serverAddress.split(" *[,;] *");
        for (String addr : arr) {
            String[] ddr = addr.split(" *[:/] *");
            int k = 1;
            if (ddr.length == 3) {
                k = Integer.parseInt(ddr[2]);
            }
            InetSocketAddress inetAddr = new InetSocketAddress(ddr[0], Integer.parseInt(ddr[1]));
            for (int j = 0; j < k; ++j) {
                clientList.add(new SocketClient(inetAddr));
            }
        }
        Collections.shuffle(clientList);
    }

    /**
     * Override super method
     * @see org.apache.niolex.network.IClient#connect()
     */
    @Override
    public void connect() throws IOException {
        for (SocketClient cli : clientList) {
            cli.connect();
        }
        this.connStatus = Status.CONNECTED;
    }

    /**
     * Override super method
     * @see org.apache.niolex.network.IClient#sendAndReceive(org.apache.niolex.network.Packet)
     */
    @Override
    public Packet sendAndReceive(Packet sc) throws IOException {
        for (int i = 0; i < clientList.size(); ++i) {
            SocketClient cli = nextClient();
            if (cli.getStatus() == Status.CONNECTED) {
                return cli.sendAndReceive(sc);
            }
        }
        throw new RpcException("No connection ready for use, Please check server status.", RpcException.Type.CLIENT_BUSY, null);
    }

    /**
     * Get the next client from the client list.
     *
     * @return the next client
     */
    private SocketClient nextClient() {
        int k = curIdx.incrementAndGet();
        // MAX_INT - 1K
        if (k > 2147482647) {
            curIdx.set(-1);
        }
        return clientList.get(k % clientList.size());
    }

    /**
     * Override super method
     * @see org.apache.niolex.network.IClient#stop()
     */
    @Override
    public void stop() {
        this.connStatus = Status.CLOSED;
        for (SocketClient cli : clientList) {
            cli.stop();
        }
    }

    /**
     * Set the sleep time between retry, default to 1 second.
     * @param sleepBetweenRetryTime
     */
    public void setSleepBetweenRetryTime(int sleepBetweenRetryTime) {
        for (SocketClient cli : clientList) {
            cli.setSleepBetweenRetryTime(sleepBetweenRetryTime);
        }
    }

    /**
     * Set the number of times to retry we connection lost from server.
     * @param connectRetryTimes
     */
    public void setConnectRetryTimes(int connectRetryTimes) {
        for (SocketClient cli : clientList) {
            cli.setConnectRetryTimes(connectRetryTimes);
        }
    }

}
