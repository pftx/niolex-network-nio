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
import org.apache.niolex.network.serialize.PacketTransformer;
import org.apache.niolex.network.serialize.StringSerializer;
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

	private final Map<String, ConcurrentLinkedQueue<PacketData>> dataMap =
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
    public void handlePacket(PacketData sc, IPacketWriter wt) {
		if (sc.getCode() != Config.CODE_REGR_UUID) {
			other.handlePacket(sc, wt);
		} else {
			String ssid = transformer.getDataObject(sc);
			if (wt instanceof BasePacketWriter) {
			    restorePackets(ssid, (BasePacketWriter) wt);
			}
			// Prepare environment for the next fault tolerate.
			wt.attachData(KEY_UUID, ssid);
			wt.attachData(KEY_RRLIST, new CircularList<PacketData>(RR_SIZE));
			// Attach it self to listen all the write events.
			wt.addEventListener(this);
		}
	}

	/**
	 * Restore the stored packets into the recovered client.
	 *
	 * @param ssid the client session ID
	 * @param bpw the basic packet writer
	 */
	protected void restorePackets(String ssid, BasePacketWriter bpw) {
	    ConcurrentLinkedQueue<PacketData> data = dataMap.get(ssid);
        if (data == null) {
            return;
        }
        dataMap.remove(ssid);
        PacketData sc;
        ConcurrentLinkedQueue<PacketData> newQueue = bpw.getRemainQueue();
        final int size = data.size();
        // We do this poll and write to add all the data in the new queue to the back of this old queue.
        while ((sc = newQueue.poll()) != null) {
            data.add(sc);
        }
        // Do the real fault tolerance.
        while ((sc = data.poll()) != null) {
            bpw.handleWrite(sc);
        }
        LOG.info("Fault tolerate recoverd for client [{}] list size {}.", ssid, size);
	}

	@Override
    public void handleClose(IPacketWriter wt) {
		other.handleClose(wt);
		if (wt instanceof BasePacketWriter) {
		    String ssid = wt.getAttached(KEY_UUID);
		    // The list is the last N packets sent to client.
		    CircularList<PacketData> list = wt.getAttached(KEY_RRLIST);
		    wt.attachData(KEY_RRLIST, null);
		    storePackets(ssid, (BasePacketWriter) wt, list);
		}
	}

	/**
	 * Store the non-sent packets into the internal data map.
	 *
	 * @param ssid the session ID
	 * @param bpw the basic packet writer
	 * @param list the non-sent list
	 */
	protected void storePackets(String ssid, BasePacketWriter bpw, CircularList<PacketData> list) {
	    // Check ssid first.
	    if (ssid == null) {
	        return;
	    }
	    // Store the non-send data.
        ConcurrentLinkedQueue<PacketData> els = bpw.getRemainQueue();
        int remainSize = els.size();
        /**
         * Due to the network buffer, the last N packets may have send to client or
         * not. We try to re-send them when client re-connected.
         */
        if (list != null) {
            // Add the last N packets into the queue.
            int end = list.size();
            for (int i = 0; i < end; ++i) {
                els.add(list.get(i));
            }
            // Poll the elements of this queue and add them to the back.
            while (remainSize-- > 0) {
                els.add(els.poll());
            }
        }
        if (els.size() > 0) {
            dataMap.put(ssid, els);
            LOG.info("Fault tolerate stored for client [{}] remain size {}.", ssid, els.size());
        }
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.event.WriteEventListener#afterSent(org.apache.niolex.network.event.WriteEvent)
	 */
	@Override
	public void afterSent(WriteEvent wEvent) {
		IPacketWriter wt = wEvent.getPacketWriter();
		CircularList<PacketData> list = wt.getAttached(KEY_RRLIST);
		if (list != null) {
			list.add(wEvent.getPacketData());
		}
	}

}
