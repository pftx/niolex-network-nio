/**
 * SingleInvoker.java
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

import java.net.InetSocketAddress;

import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.client.SocketClient;

/**
 * This invoker can only handle invoke sequentially. We use {@link SocketClient} as the communication tool.
 * 
 * @see org.apache.niolex.network.client.SocketClient
 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
 * @version 2.1.2
 * @since Aug 3, 2016
 */
public class SingleInvoker extends BaseInvoker {

    /**
     * Used to save the response from server.
     */
    private PacketData res;

    /**
     * Create a SingleInvoker with the specified server address.
     * 
     * @param serverAddress the server address to connect to
     */
    public SingleInvoker(InetSocketAddress serverAddress) {
        super(new SocketClient(serverAddress));
    }

    /**
     * This is the override of super method.
     * @see org.apache.niolex.network.rpc.cli.RemoteInvoker#sendPacket(org.apache.niolex.network.PacketData)
     */
    @Override
    public synchronized void sendPacket(PacketData packet) {
        client.handleWrite(packet);
    }

    /**
     * This is the override of super method.
     * @see org.apache.niolex.network.rpc.cli.RemoteInvoker#invoke(org.apache.niolex.network.PacketData)
     */
    @Override
    public synchronized PacketData invoke(PacketData packet) {
        this.res = null;
        // SocketClient will handle read in this method.
        client.handleWrite(packet);
        return res;
    }

    @Override
    public void handlePacket(PacketData sc, IPacketWriter wt) {
        this.res = sc;
    }

    /**
     * This is the override of super method.
     * @see org.apache.niolex.network.rpc.cli.RemoteInvoker#setRpcHandleTimeout(int)
     */
    @Override
    public void setRpcHandleTimeout(int rpcHandleTimeout) {
        // This value is ignored.
    }

}
