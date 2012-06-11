/**
 * PrintPacketHandler.java
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
package org.apache.niolex.network.demo;

import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;


/**
 * Print Packet into Console
 * @author Xie, Jiyun
 *
 */
public class PrintPacketHandler implements IPacketHandler {

    /* (non-Javadoc)
     * @see com.renren.ad.datacenter.follower.network.IPacketHandler#handleError()
     */
    @Override
    public void handleClose(IPacketWriter wt) {
        System.out.println("Error occured, this is from PrintPacketHandler. " + wt.getRemoteName());
    }

    /* (non-Javadoc)
     * @see com.renren.ad.datacenter.follower.network.IPacketHandler#handleRead(com.renren.ad.datacenter.follower.network.Packet, com.renren.ad.datacenter.follower.network.IPacketWriter)
     */
    @Override
    public void handleRead(PacketData sc, IPacketWriter wt) {
        System.out.print("\n\nPacket with code [" + sc.getCode() + "] received.");
        System.out.println(" body:\n" + new String(sc.getData()));
    }

}
