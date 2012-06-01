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

import org.apache.niolex.commons.util.LRUHashMap;
import org.apache.niolex.commons.util.LinkedIterList;
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

	private static final String KEY = Config.ATTACH_KEY_SESS_SESSID;
	private Map<String, LinkedIterList<PacketData>> dataMap =
			new LRUHashMap<String, LinkedIterList<PacketData>>(Config.SERVER_FAULT_TOLERATE_SIZE);

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
			LinkedIterList<PacketData> data = dataMap.get(ssid);
			if (data != null) {
				wt.setRemainPackets(data);
				LOG.info("Data recoverd for client [{}] list size {}.", ssid, data.size());
				dataMap.remove(ssid);
			}
			wt.attachData(KEY, ssid);
		}
	}

	@Override
    public void handleError(IPacketWriter wt) {
		other.handleError(wt);
		String ssid = wt.getAttached(KEY);
		LinkedIterList<PacketData> els = wt.getRemainPackets();
		if (ssid != null) {
			dataMap.put(ssid, els);
			LOG.info("Fault tolerate for client [{}] remain size {}.", ssid, els.size());
		}
	}

}
