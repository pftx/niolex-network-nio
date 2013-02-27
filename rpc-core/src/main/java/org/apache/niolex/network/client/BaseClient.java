/**
 * BaseClient.java
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

import org.apache.niolex.commons.concurrent.WaitOn;
import org.apache.niolex.network.Config;
import org.apache.niolex.network.IClient;
import org.apache.niolex.network.Packet;

/**
 * The basic abstract implementation of IClient, handle connection
 * status and timeout properly.
 * Please extend this class for convenience.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5
 * @since 2013-2-27
 */
public abstract class BaseClient implements IClient {

    /**
     * The connection status of this Client.
     */
    protected Status connStatus;

    /**
     * The working status of this client.
     */
    protected boolean isWorking;

    /**
     * Socket connect timeout.
     */
    protected int connectTimeout = Config.SO_CONNECT_TIMEOUT;

    /**
     * Init the connection status.
     */
    public BaseClient() {
        this.connStatus = Status.INNITIAL;
    }

    /**
     * Override super method
     * @see org.apache.niolex.network.IClient#asyncInvoke(org.apache.niolex.network.Packet)
     */
    @Override
    public WaitOn<Packet> asyncInvoke(Packet sc) throws IOException {
        throw new UnsupportedOperationException("This method is not supported.");
    }

    /**
     * Override super method
     * @see org.apache.niolex.network.IClient#getStatus()
     */
    @Override
    public Status getStatus() {
        return connStatus;
    }

    /**
     * Override super method
     * @see org.apache.niolex.network.IClient#isWorking()
     */
    @Override
    public boolean isWorking() {
        return isWorking;
    }

    /**
     * Override super method
     * @see org.apache.niolex.network.IClient#setConnectTimeout(int)
     */
    @Override
    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    /**
     * Get the socket connect timeout.
     * @return the socket connect timeout
     */
    public int getConnectTimeout() {
        return connectTimeout;
    }

}
