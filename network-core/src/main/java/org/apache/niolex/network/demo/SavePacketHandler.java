/**
 * SavePacketHandler.java
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
package org.apache.niolex.network.demo;

import java.util.Collection;

import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;

/**
 * Save all the read packet data into the list passed in the constructor.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-5-31
 */
public class SavePacketHandler implements IPacketHandler {
	// The collection to save data
	private final Collection<PacketData> collection;

	/**
	 * Constructor.
	 *
	 * @param collection the collection to save data
	 */
	public SavePacketHandler(Collection<PacketData> collection) {
		super();
		this.collection = collection;
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.IPacketHandler#handlePacket(org.apache.niolex.network.PacketData, org.apache.niolex.network.IPacketWriter)
	 */
	@Override
	public void handlePacket(PacketData sc, IPacketWriter wt) {
	    if (collection != null)
	        collection.add(sc);
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.IPacketHandler#handleClose(org.apache.niolex.network.IPacketWriter)
	 */
	@Override
	public void handleClose(IPacketWriter wt) {
	}

}
