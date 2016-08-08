/**
 * RemoteInvoker.java
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

import org.apache.niolex.network.PacketData;

/**
 * This interface manages sending packets to RPC server using appropriate client,
 * and wait for the results from server side.
 * 
 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
 * @version 0.6.1
 * @since Aug 3, 2016
 */
public interface RemoteInvoker {

    /**
     * Connect to server.
     * 
     * @throws IOException if I / O related error occurred
     */
    void connect() throws IOException;

    /**
     * Stop the connection to server.
     */
    void stop();

    /**
     * Send the packet data to server. (Maybe in a later time.) We do not care about the response from server.
     *
     * @param packet the data need to be sent to server
     */
    void sendPacket(PacketData packet);

    /**
     * Send packet data to server and wait for the response in blocking mode.
     *
     * @param packet the data need to be sent to server
     * @return the packet returned from server
     */
    PacketData invoke(PacketData packet);

    /**
     * When send packet in blocking mode, the rpc blocking thread will return null if the result is not ready after
     * this specified time.
     *
     * @param rpcHandleTimeout the timeout to set to
     */
    void setRpcHandleTimeout(int rpcHandleTimeout);

    /**
     * @return The string representation of the remote peer. i.e. The IP address.
     */
    String getRemoteAddress();

    /**
     * Whether this invoker is ready to work.
     *
     * @return true if this invoker is valid and ready to work.
     */
    public boolean isReady();

}
