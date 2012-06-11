/**
 * IPacketHandler.java
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
 * The Packet Handler Interface.
 * @author Xie, Jiyun
 *
 */
public interface IPacketHandler {

    /**
     * Handle the Packet, and write results to IPacketWriter if you want.
     * @param sc The Packet need Handle
     * @param wt Write results
     */
    public void handleRead(PacketData sc, IPacketWriter wt);

    /**
     * The remote peer identified by this IPacketWriter is broken.
     * @param wt
     */
    public void handleClose(IPacketWriter wt);
}
