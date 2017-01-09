/**
 * WriteEvent.java
 *
 * Copyright 2012 Niolex, Inc.
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
package org.apache.niolex.network.event;

import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;

/**
 * Packet write event.
 * Our system use asynchronous way to send data.
 * That is, when you call {@link IPacketWriter#handleWrite(PacketData)}, we just put the packet into
 * the out going queue. System will send it when the network is ready.
 * <br>
 * But, how could you be sure that your packet was sent? Use event based methods.
 *
 * @see org.apache.niolex.network.IPacketWriter#addEventListener(WriteEventListener)
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-6-14
 */
public class WriteEvent {

	/**
	 * The packet writer who sent this packet.
	 */
	private final IPacketWriter packetWriter;

	/**
	 * The packet just sent to remote peer.
	 */
	private final PacketData packetData;

	/**
	 * Construct a write event.
	 * 
	 * @param packetWriter the packet writer
	 * @param packetData the packet data
	 */
	public WriteEvent(IPacketWriter packetWriter, PacketData packetData) {
        super();
        this.packetWriter = packetWriter;
        this.packetData = packetData;
    }

    /**
	 * @return The packet writer who sent this packet.
	 */
	public IPacketWriter getPacketWriter() {
		return packetWriter;
	}

	/**
	 * @return The packet just sent to remote peer.
	 */
	public PacketData getPacketData() {
		return packetData;
	}

}
