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
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The base BasePacketWriter, handle attach and PacketData storage.
 * PacketData will be stored in a ConcurrentLinkedQueue, which is good at many writes.
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
	 * Internal variables. Store all the packets need to send.
	 */
	private ConcurrentLinkedQueue<PacketData> sendPacketsQueue = new ConcurrentLinkedQueue<PacketData>();

	/**
	 * Initialize send iterator here.
	 */
	public BasePacketWriter() {
		super();
	}

	@Override
	public void handleWrite(PacketData sc) {
		sendPacketsQueue.add(sc);
	}

	/**
	 * Sub class need to use this method to get packets to send.
	 * @return
	 */
	protected PacketData handleNext() {
		return sendPacketsQueue.poll();
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

	@Override
	public int size() {
		return sendPacketsQueue.size();
	}

	/**
	 * Get the current non send packet queue.
	 * @return
	 */
	public ConcurrentLinkedQueue<PacketData> getRemainQueue() {
		return sendPacketsQueue;
	}

	/**
	 * replace the internal packet queue.
	 * @param list
	 */
	public void replaceQueue(ConcurrentLinkedQueue<PacketData> list) {
		this.sendPacketsQueue = list;
	}
}
