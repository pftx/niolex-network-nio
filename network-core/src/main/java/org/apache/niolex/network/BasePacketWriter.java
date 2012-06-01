/**
 * BasePacketWriter.java
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
package org.apache.niolex.network;

import java.util.HashMap;
import java.util.Map;

import org.apache.niolex.commons.util.LinkedIterList;
import org.apache.niolex.commons.util.LinkedIterList.Iter;

/**
 * The base BasePacketWriter, handle attach and PacketData storage.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-5-30
 */
public abstract class BasePacketWriter implements IPacketWriter {

	/**
	 * Attachment map, save all attachments here.
	 */
	protected Map<String, Object> attachMap = new HashMap<String, Object>();

	/**
	 * Internal variables.
	 */
	private LinkedIterList<PacketData> sendPacketList = new LinkedIterList<PacketData>();
	private Iter<PacketData> sendIter;
	private int cacheSize;

	/**
	 * Initialize send iterator here.
	 */
	public BasePacketWriter() {
		super();
		this.sendIter = sendPacketList.iterator();
	}

	@Override
	public void handleWrite(PacketData sc) {
		sendPacketList.add(sc);
	}

	@Override
	public LinkedIterList<PacketData> getRemainPackets() {
		return sendPacketList;
	}

	@Override
	public void setRemainPackets(LinkedIterList<PacketData> list) {
		this.sendPacketList = list;
		this.sendIter = sendPacketList.iterator();
	}

	/**
	 * Sub class need to use this method to get packets to send.
	 * @return
	 */
	protected PacketData handleNext() {
		PacketData d = sendIter.next();
		if (d != null) {
			if (cacheSize >= Config.SERVER_CACHE_TOLERATE_SIZE) {
				sendPacketList.poll();
			} else {
				++ cacheSize;
			}
			d.setDataPos(0);
		}
		return d;
	}

	@Override
	public Object attachData(String key, Object value) {
		return attachMap.put(key, value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAttached(String key) {
		return (T)attachMap.get(key);
	}
}
