/**
 * FaultTolerateSPacketHandler.java
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
package org.apache.niolex.network.handler;

import java.util.Collection;
import java.util.Map;

import org.apache.niolex.commons.util.LRUHashMap;
import org.apache.niolex.network.Config;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.packet.PacketTransformer;
import org.apache.niolex.network.packet.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the fault tolerate session packet handler.
 * In this handler, the first connect packet need to be Session ID Packet, otherwise it can not handle the
 * fault toleration.
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-5-30
 */
public class FaultTolerateSPacketHandler extends SessionPacketHandler {
	private static final Logger LOG = LoggerFactory.getLogger(FaultTolerateSPacketHandler.class);

	private static final String KEY = Config.ATTACH_KEY_SESS_SESSID;
	private Map<String, Collection<PacketData>> dataMap =
			new LRUHashMap<String, Collection<PacketData>>(Config.SERVER_FAULT_TOLERATE_SIZE);

	private PacketTransformer transformer;

	/**
	 * Decode Session ID with StringSerializer.
	 */
	public FaultTolerateSPacketHandler() {
		transformer = PacketTransformer.getInstance();
		transformer.addSerializer(new StringSerializer(Config.CODE_SESSN_REGR));
	}

	/**
	 * Implements super constructor.
	 *
	 * @param factory
	 */
	public FaultTolerateSPacketHandler(IHandlerFactory factory) {
		super(factory);
		transformer = PacketTransformer.getInstance();
		transformer.addSerializer(new StringSerializer(Config.CODE_SESSN_REGR));
	}



	@Override
    public void handleRead(PacketData sc, IPacketWriter wt) {
		if (sc.getCode() != Config.CODE_SESSN_REGR) {
			super.handleRead(sc, wt);
		} else {
			String ssid = transformer.getDataObject(sc);
			Collection<PacketData> data = dataMap.get(ssid);
			if (data != null) {
				for (PacketData pc : data) {
					wt.handleWrite(pc);
				}
				LOG.info("Data recoverd for client [{}] list size {}.", ssid, data.size());
				dataMap.remove(ssid);
			}
			wt.attachData(KEY, ssid);
		}
	}

	@Override
    public void handleError(IPacketWriter wt) {
		super.handleError(wt);
		String ssid = wt.getAttached(KEY);
		Collection<PacketData> els = wt.getRemainPackets();
		if (ssid != null) {
			dataMap.put(ssid, els);
			LOG.info("Fault tolerate for client [{}] remain size {}.", ssid, els.size());
		}
	}

}
