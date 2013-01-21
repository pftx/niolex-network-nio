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
 * For example, code 0 for HeartBeat, code 1 for get description.
 *
 * User can set a default handler to handle all other packets not handled by dispatch map.
 * We do not support range handler now, so this handler is more demo than useful.
 *
 * @author Xie, Jiyun
 *
 */
public class DispatchPacketHandler implements IPacketHandler {
	private static final Logger LOG = LoggerFactory.getLogger(DispatchPacketHandler.class);

    private Map<Short, IPacketHandler> dispatchMap = new HashMap<Short, IPacketHandler>();
    private IPacketHandler defaultHandler;

    /**
     * Add a handler for code <code>
     * @param code
     * @param handler
     */
    public void addHandler(Short code, IPacketHandler handler) {
        dispatchMap.put(code, handler);
    }

    /**
     * Every Handler registered will be invoked for handleClose.
     *
     * Override super method
     * @see org.apache.niolex.network.IPacketHandler#handleClose(org.apache.niolex.network.IPacketWriter)
     */
    @Override
    public void handleClose(IPacketWriter wt) {
        for (IPacketHandler h : dispatchMap.values()) {
            h.handleClose(wt);
        }
        if (defaultHandler != null) {
            defaultHandler.handleClose(wt);
        }
    }

    /**
     * We will search the correct handler to handle this packet.
     * If we can not find one, we will do a warning log.
     *
     * Override super method
     * @see org.apache.niolex.network.IPacketHandler#handleRead(org.apache.niolex.network.PacketData, org.apache.niolex.network.IPacketWriter)
     */
    @Override
    public void handleRead(PacketData sc, IPacketWriter wt) {
        IPacketHandler h = dispatchMap.get(sc.getCode());
        if (h != null) {
            h.handleRead(sc, wt);
        } else if (defaultHandler != null) {
            defaultHandler.handleRead(sc, wt);
        } else {
            LOG.warn("No handler registered for Packet with code {}", sc.getCode());
        }
    }

    /**
     * Get the current default handler
     *
     * @return the default handler
     */
    public IPacketHandler getDefaultHandler() {
        return defaultHandler;
    }

    /**
     * Set the current default handler
     *
     * @param defaultHandler the new default handler
     */
    public void setDefaultHandler(IPacketHandler defaultHandler) {
        this.defaultHandler = defaultHandler;
    }

    /**
     * Return the internal dispatch map size. For user to check.
     *
     * @return the current dispatch map size
     */
    protected int getDispatchSize() {
    	return dispatchMap.size();
    }

}
