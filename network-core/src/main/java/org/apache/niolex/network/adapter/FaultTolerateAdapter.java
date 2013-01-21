/**
 * FaultTolerateAdapter.java
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
package org.apache.niolex.network.adapter;

import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.niolex.commons.collection.LRUHashMap;
import org.apache.niolex.commons.collection.CircularList;
import org.apache.niolex.network.Config;
import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.event.WriteEvent;
import org.apache.niolex.network.event.WriteEventListener;
import org.apache.niolex.network.packet.PacketTransformer;
import org.apache.niolex.network.packet.StringSerializer;
import org.apache.niolex.network.server.BasePacketWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the fault tolerate packet adapter.
 * This adapter try to save all the non-send packets into internal data structure,
 * and send them when client re-connected to this server.
 * <p>
 * You can use this handler to deduce the rate of packet lose when network is not stable.
 * The packet order will not be remained after fault toleration.
 * <p>
 * The difference between handler and adapter is that adapter can be applied on everything and handler
 * only deal with a specific situation.
 * <p>
 * In this adapter, the first connect packet need to be Session ID Packet, otherwise it can not handle the
 * fault toleration properly.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-5-30
 */
public class FaultTolerateAdapter implements IPacketHandler, WriteEventListener {
	private static final Logger LOG = LoggerFactory.getLogger(FaultTolerateAdapter.class);

	private static final String KEY_UUID = Config.ATTACH_KEY_FAULTTO_UUID;

	private static final String KEY_RRLIST = Config.ATTACH_KEY_FAULT_RRLIST;

	private static final int RR_SIZE = Config.SERVER_CACHE_TOLERATE_PACKETS_SIZE;

	private Map<String, ConcurrentLinkedQueue<PacketData>> dataMap =
			new LRUHashMap<String, ConcurrentLinkedQueue<PacketData>>(Config.SERVER_FAULT_TOLERATE_MAP_SIZE);

	// The Handler need to be adapted.
	private final IPacketHandler other;

	private final PacketTransformer transformer;

	/**
	 * Implements a constructor, please pass a handler in.
	 * Decode Session ID with StringSerializer.
	 */
	public FaultTolerateAdapter(IPacketHandler other) {
		transformer = PacketTransformer.getInstance();
		transformer.addSerializer(new StringSerializer(Config.CODE_REGR_UUID));
		this.other = other;
	}

	@Override
    public void handleRead(PacketData sc, IPacketWriter wt) {
		if (sc.getCode() != Config.CODE_REGR_UUID) {
			other.handleRead(sc, wt);
		} else {
			String ssid = transformer.getDataObject(sc);
			ConcurrentLinkedQueue<PacketData> data = dataMap.get(ssid);

			// Do the real fault tolerate.
			if (data != null && wt instanceof BasePacketWriter) {
				BasePacketWriter bpw = (BasePacketWriter) wt;
				bpw.replaceQueue(data);
				// We do this poll and write to trigger client attach itself to write.
				sc = data.poll();
				bpw.handleWrite(sc);
				LOG.info("Fault tolerate recoverd for client [{}] list size {}.", ssid, data.size());
				dataMap.remove(ssid);
			}

			// Prepare environment for the next fault tolerate.
			wt.attachData(KEY_UUID, ssid);
			CircularList<PacketData> list = new CircularList<PacketData>(RR_SIZE);
			wt.attachData(KEY_RRLIST, list);
			// Attach it self to listen all the write events.
			wt.addEventListener(this);
		}
	}

	@Override
    public void handleClose(IPacketWriter wt) {
		other.handleClose(wt);
		String ssid = wt.getAttached(KEY_UUID);
		if (ssid != null && wt instanceof BasePacketWriter) {
			BasePacketWriter bpw = (BasePacketWriter) wt;
			// Store the non-send data.
			ConcurrentLinkedQueue<PacketData> els = bpw.getRemainQueue();
			// The rrlist is to store the last N packets send to client.
			CircularList<PacketData> list = wt.getAttached(KEY_RRLIST);
			/**
			 * Due to the network buffer, the last N packets may have send to client or
			 * not. We try to re-send them when client re-connected.
			 */
			if (list != null) {
				PacketData sc;
				int end = list.size();
				for (int i = 0; i < end; ++i) {
					sc = list.get(i);
					els.add(sc);
				}
			}
			if (els.size() > 0) {
				dataMap.put(ssid, els);
				LOG.info("Fault tolerate received for client [{}] remain size {}.", ssid, els.size());
			}
		}
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.event.WriteEventListener#afterSend(org.apache.niolex.network.event.WriteEvent)
	 */
	@Override
	public void afterSend(WriteEvent wEvent) {
		IPacketWriter wt = wEvent.getPacketWriter();
		CircularList<PacketData> list = wt.getAttached(KEY_RRLIST);
		if (list != null) {
			list.add(wEvent.getPacketData());
		}
	}

}
