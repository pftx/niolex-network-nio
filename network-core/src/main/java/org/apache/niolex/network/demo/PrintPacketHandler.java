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
 * Print the packet code and data into Console, and interpret packet body as simple string.
 * Used in DemoClient.
 *
 * @author Xie, Jiyun
 *
 */
public class PrintPacketHandler implements IPacketHandler {

	/**
	 * Override super method
	 * @see org.apache.niolex.network.IPacketHandler#handleClose(org.apache.niolex.network.IPacketWriter)
	 */
    @Override
    public void handleClose(IPacketWriter wt) {
        System.out.println("Error occured, this is from PrintPacketHandler. " + wt.getRemoteName());
    }

    /**
     * Override super method
     * @see org.apache.niolex.network.IPacketHandler#handlePacket(org.apache.niolex.network.PacketData, org.apache.niolex.network.IPacketWriter)
     */
    @Override
    public void handlePacket(PacketData sc, IPacketWriter wt) {
        System.out.print("Packet with code [" + sc.getCode() + "] received.");
        System.out.println(" body:\n" + new String(sc.getData()));
        System.out.println();
    }

}
