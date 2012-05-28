/**
 * SessionPacketHandler.java
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
 * Create a session for every client, send every request to that handler instance.
 * @author Xie, Jiyun
 *
 */
public class SessionPacketHandler implements IPacketHandler {
	private static final Logger LOG = LoggerFactory.getLogger(SessionPacketHandler.class);

    private Map<IPacketWriter, IPacketHandler> handlerMap = new HashMap<IPacketWriter, IPacketHandler>();

    private IHandlerFactory factory;

    /**
     * Create a SessionPacketHandler without a factory, please use setter to set a factory.
     */
    public SessionPacketHandler() {
        super();
    }

    /**
     * Create a SessionPacketHandler with a factory, this is ready to use.
     * @param factory
     */
    public SessionPacketHandler(IHandlerFactory factory) {
        super();
        this.factory = factory;
    }

    /* (non-Javadoc)
     * @see com.renren.ad.datacenter.follower.network.IPacketHandler#handleError(com.renren.ad.datacenter.follower.network.IPacketWriter)
     */
    @Override
    public void handleError(IPacketWriter wt) {
        IPacketHandler h = handlerMap.get(wt);
        if (h != null) {
            handlerMap.remove(wt);
            h.handleError(wt);
            LOG.info("Session removed for remote: {}", wt.getRemoteName());
        }
    }

    /* (non-Javadoc)
     * @see com.renren.ad.datacenter.follower.network.IPacketHandler#handleRead(com.renren.ad.datacenter.follower.network.PacketHelper, com.renren.ad.datacenter.follower.network.IPacketWriter)
     */
    @Override
    public void handleRead(PacketData sc, IPacketWriter wt) {
        IPacketHandler h = handlerMap.get(wt);
        if (h == null) {
            h = factory.createHandler(wt);
            handlerMap.put(wt, h);
            LOG.info("Session created for remote: {}", wt.getRemoteName());
        }
        h.handleRead(sc, wt);
    }

    /**
     * @return the factory
     */
    public IHandlerFactory getFactory() {
        return factory;
    }

    /**
     * @param factory the factory to set
     */
    public void setFactory(IHandlerFactory factory) {
        this.factory = factory;
    }

    /**
     * Return the internal handler size, for unit test.
     * @return
     */
    protected int getHandlerSize() {
    	return handlerMap.size();
    }
}
