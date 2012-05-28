/**
 * IPacketWriter.java
 *
 * Copyright 2011 Niolex, Inc.
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
package org.apache.niolex.network;

/**
 * The Packet Writer Interface.
 * @author Xie, Jiyun
 *
 */
public interface IPacketWriter {

    /**
     * The string representation of the remote peer. i.e. The IP address.
     * @return
     */
    public String getRemoteName();

    /**
     * Write Packet to the remote peer.
     * You can write as many packets as you want.
     * @param sc The Packet to write
     */
    public void handleWrite(PacketData sc);

}
