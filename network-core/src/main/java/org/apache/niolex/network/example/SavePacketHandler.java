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
package org.apache.niolex.network.example;

import java.util.List;

import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;

/**
 * Save read data into the list send in.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-5-31
 */
public class SavePacketHandler implements IPacketHandler {
	// The list to save data
	private List<PacketData> list;

	/**
	 * Set the list to save data
	 * @param list
	 */
	public SavePacketHandler(List<PacketData> list) {
		super();
		this.list = list;
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.IPacketHandler#handleRead(org.apache.niolex.network.PacketData, org.apache.niolex.network.IPacketWriter)
	 */
	@Override
	public void handleRead(PacketData sc, IPacketWriter wt) {
		list.add(sc);
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.IPacketHandler#handleError(org.apache.niolex.network.IPacketWriter)
	 */
	@Override
	public void handleError(IPacketWriter wt) {
	}

}
