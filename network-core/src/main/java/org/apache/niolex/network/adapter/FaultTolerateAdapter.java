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

import org.apache.niolex.commons.util.LRUHashMap;
import org.apache.niolex.network.BasePacketWriter;
import org.apache.niolex.network.Config;
import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.packet.PacketTransformer;
import org.apache.niolex.network.packet.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the fault tolerate packet adapter.
 * The difference between handler and adapter is that adapter can be applied on everything and handler
 * only deal with a specific situation.
 *
 * In this adapter, the first connect packet need to be Session ID Packet, otherwise it can not handle the
 * fault toleration properly.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-5-30
 */
public class FaultTolerateAdapter implements IPacketHandler {
	private static final Logger LOG = LoggerFactory.getLogger(FaultTolerateAdapter.class);

	private static final String KEY = Config.ATTACH_KEY_FAULTTO_UUID;

	private Map<String, ConcurrentLinkedQueue<PacketData>> dataMap =
			new LRUHashMap<String, ConcurrentLinkedQueue<PacketData>>(Config.SERVER_FAULT_TOLERATE_MAP_SIZE);

	// The Handler need to be adapted.
	private IPacketHandler other;

	private PacketTransformer transformer;

	/**
	 * Implements a constructor, please pass a handler in.
	 * Decode Session ID with StringSerializer.
	 */
	public FaultTolerateAdapter(IPacketHandler other) {
		transformer = PacketTransformer.getInstance();
		transformer.addSerializer(new StringSerializer(Config.CODE_SESSN_REGR));
		this.other = other;
	}

	@Override
    public void handleRead(PacketData sc, IPacketWriter wt) {
		if (sc.getCode() != Config.CODE_SESSN_REGR) {
			other.handleRead(sc, wt);
		} else {
			String ssid = transformer.getDataObject(sc);
			ConcurrentLinkedQueue<PacketData> data = dataMap.get(ssid);
			if (data != null && wt instanceof BasePacketWriter) {
				BasePacketWriter bpw = (BasePacketWriter) wt;
				bpw.replaceQueue(data);
				LOG.info("Fault tolerate recoverd for client [{}] list size {}.", ssid, data.size());
				dataMap.remove(ssid);
			}
			wt.attachData(KEY, ssid);
		}
	}

	@Override
    public void handleClose(IPacketWriter wt) {
		other.handleClose(wt);
		String ssid = wt.getAttached(KEY);
		if (ssid != null && wt instanceof BasePacketWriter) {
			BasePacketWriter bpw = (BasePacketWriter) wt;
			ConcurrentLinkedQueue<PacketData> els = bpw.getRemainQueue();
			if (els.size() > 0) {
				dataMap.put(ssid, els);
				LOG.info("Fault tolerate received for client [{}] remain size {}.", ssid, els.size());
			}
		}
	}

}
