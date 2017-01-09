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

import java.util.Map;
import java.util.TreeMap;

import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dispatch Packet by packet code.
 * Every code can be a type of packet, so register a Handler for a kind of Packet.
 * For example, code 0 for HeartBeat, code 1 for get description.
 * <br>
 * User can set a default handler to handle all other packets not handled by any
 * of the registered handlers.
 * We support range handler since 0.6.0, see {@link #addHandler(short, short, IPacketHandler)}
 * for details.
 * <b>User need to prepare all the handlers before add it to NisServer, if it's in use, user
 * should not add any handler.</b>
 *
 * @author Xie, Jiyun
 * @version 0.7.1
 */
public class DispatchPacketHandler implements IPacketHandler {
	private static final Logger LOG = LoggerFactory.getLogger(DispatchPacketHandler.class);

	/**
	 * This class is used to store handler mapping information.
	 * 
	 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
	 * @version 1.0.0
	 * @since May 19, 2016
	 */
	public static final class Handle {
        private final short startCode;
        private final short endCode;
	    private final IPacketHandler handler;
	    
        public Handle(short code, IPacketHandler handler) {
            this(code, code, handler);
        }
	    
        public Handle(short startCode, short endCode, IPacketHandler handler) {
            super();
            this.startCode = startCode;
            this.endCode = endCode;
            this.handler = handler;
        }
        
        public short getStartCode() {
            return startCode;
        }
        
        public short getEndCode() {
            return endCode;
        }
        
        public IPacketHandler getHandler() {
            return handler;
        }

        @Override
        public String toString() {
            return String.format("[%d - %d]", startCode, endCode);
        }
	}

    /**
     * The tree map to store all the handlers.
     */
    private final TreeMap<Short, Handle> dispatchMap = new TreeMap<Short, Handle>();

    /**
     * The default handler will be used if no mapping handler in the {@link #dispatchMap}.
     */
    private IPacketHandler defaultHandler;

    /**
     * Add a handler for packet with the specified code.
     *
     * @param code the packet code
     * @param handler the packet handler
     */
    public void addHandler(short code, IPacketHandler handler) {
        addHandler(code, code, handler);
    }
    
    /**
     * Add a handler for packets with packet code between startCode and endCode(includes both end).
     *
     * @param startCode the start packet code
     * @param endCode the end packet code
     * @param handler the packet handler
     * @throws IllegalArgumentException if the packet code range overlapped with another handler.
     */
    public void addHandler(int startCode, int endCode, IPacketHandler handler) {
        addHandler((short) startCode, (short) endCode, handler);
    }
    
    /**
     * Add a handler for packets with packet code between startCode and endCode(includes both end).
     *
     * @param startCode the start packet code
     * @param endCode the end packet code
     * @param handler the packet handler
     * @throws IllegalArgumentException if the packet code range overlapped with another handler.
     */
    public void addHandler(short startCode, short endCode, IPacketHandler handler) {
        if (startCode > endCode) {
            throw new IllegalArgumentException("startCode must less than or equal to endCode.");
        }
        // We need to check whether this new range overlap with any old range
        // -- Returns a key-value mapping associated with the least key greater than or
        // equal to the given key, or null if there is no such key.
        Map.Entry<Short, Handle> entry = dispatchMap.ceilingEntry(startCode);
        if (entry != null && entry.getValue().startCode <= endCode) {
            StringBuilder sb = new StringBuilder();
            Handle h = entry.getValue();
            sb.append("Overlapped with another handler. Handler class: ");
            sb.append(h.getHandler().getClass().getName()).append(", range: [");
            sb.append(h.getStartCode()).append(", ").append(h.getEndCode()).append("].");
            throw new IllegalArgumentException(sb.toString());
        }
        dispatchMap.put(endCode, new Handle(startCode, endCode, handler));
    }

    /**
     * Every Handler registered will be invoked for {@link #handleClose(IPacketWriter)}. User need
     * to decide what to do.
     *
     * Override super method
     * @see org.apache.niolex.network.IPacketHandler#handleClose(org.apache.niolex.network.IPacketWriter)
     */
    @Override
    public void handleClose(IPacketWriter wt) {
        for (Handle h : dispatchMap.values()) {
            h.handler.handleClose(wt);
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
     * @see org.apache.niolex.network.IPacketHandler#handlePacket(PacketData, IPacketWriter)
     */
    @Override
    public void handlePacket(PacketData sc, IPacketWriter wt) {
        short code = sc.getCode();
        Map.Entry<Short, Handle> entry = dispatchMap.ceilingEntry(code);
        
        if (entry != null && entry.getValue().startCode <= code) {
            entry.getValue().handler.handlePacket(sc, wt);
        } else if (defaultHandler != null) {
            defaultHandler.handlePacket(sc, wt);
        } else {
            LOG.warn("No handler registered for Packet with code {}, just ignored.", code);
        }
    }

    /**
     * Get the current default handler.
     *
     * @return the default handler
     */
    public IPacketHandler getDefaultHandler() {
        return defaultHandler;
    }

    /**
     * Set the current default handler.
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
