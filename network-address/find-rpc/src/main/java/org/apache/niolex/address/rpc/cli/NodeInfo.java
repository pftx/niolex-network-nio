/**
 * NodeInfo.java
 *
 * Copyright 2012 Niolex, Inc.
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
package org.apache.niolex.address.rpc.cli;

import java.net.InetSocketAddress;

/**
 * Store server address node information.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5, $Date: 2012-12-4$
 */
public class NodeInfo {

    /**
     * The data convert protocol.
     */
    private String protocol;

    /**
     * The server address.
     */
    private InetSocketAddress address;

    /**
     * The weight of this server, default to 1.
     */
    private int weight;

    /**
     * @return the protocol
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * @param protocol the protocol to set
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * @return the address
     */
    public InetSocketAddress getAddress() {
        return address;
    }

    /**
     * @param address the address to set
     */
    public void setAddress(InetSocketAddress address) {
        this.address = address;
    }

    /**
     * @return the weight
     */
    public int getWeight() {
        return weight;
    }

    /**
     * @param weight the weight to set
     */
    public void setWeight(int weight) {
        this.weight = weight;
    }

    /**
     * Override super method
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return ((address == null) ? 0 : address.hashCode());
    }

    /**
     * Override super method
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof NodeInfo)) return false;
        NodeInfo other = (NodeInfo) obj;
        if (address == null) {
            return other.address == null;
        } else {
            return address.equals(other.address);
        }
    }

    /**
     * Override super method
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return protocol + ":/" + address + "#" + weight;
    }

}
