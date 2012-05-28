/**
 * DispatchPacketHandler.java
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

import java.util.HashMap;
import java.util.Map;

import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Dispatch Packet by packet code.
 * Every code can be a type of packet, so register a Handler for a kind of Packet.
 * Foe example, code 0 for HeartBeat, code 1 for get description.
 * @author Xie, Jiyun
 *
 */
public class DispatchPacketHandler implements IPacketHandler {
	private static final Logger LOG = LoggerFactory.getLogger(DispatchPacketHandler.class);

    private Map<Short, IPacketHandler> dispatchMap = new HashMap<Short, IPacketHandler>();

    /**
     * Add a handler for code <code>
     * @param code
     * @param handler
     */
    public void addHandler(Short code, IPacketHandler handler) {
        dispatchMap.put(code, handler);
    }

    /* (non-Javadoc)
     * @see com.renren.ad.datacenter.follower.network.IPacketHandler#handleError()
     */
    /**
     * Every Handler registered will be invoked.
     */
    @Override
    public void handleError(IPacketWriter wt) {
        for (IPacketHandler h : dispatchMap.values()) {
            h.handleError(wt);
        }
    }

    /* (non-Javadoc)
     * @see com.renren.ad.datacenter.follower.network.IPacketHandler#handleRead(com.renren.ad.datacenter.follower.network.Packet, com.renren.ad.datacenter.follower.network.IPacketWriter)
     */
    @Override
    public void handleRead(PacketData sc, IPacketWriter wt) {
        IPacketHandler h = dispatchMap.get(sc.getCode());
        if (h != null) {
            h.handleRead(sc, wt);
        } else {
            LOG.warn("No handler registered for Packet with code {}", sc.getCode());
        }
    }

    /**
     * Return the internal dispatch map size. For unit test.
     * @return
     */
    protected int getDispatchSize() {
    	return dispatchMap.size();
    }

}
