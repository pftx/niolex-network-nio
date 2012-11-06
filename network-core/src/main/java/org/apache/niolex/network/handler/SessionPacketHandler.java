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

import org.apache.niolex.network.Config;
import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Create a session for every different client identified by socket connection,
 * send every request from the connection to that handler instance.
 *
 * @author Xie, Jiyun
 *
 */
public class SessionPacketHandler implements IPacketHandler {
	private static final Logger LOG = LoggerFactory.getLogger(SessionPacketHandler.class);

    private static final String KEY = Config.ATTACH_KEY_SESS_HANDLER;

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

    /**
     * We will send this message to the correct handler for this connection.
     * and we will do a log whether we find a handler or not.
     *
     * Override super method
     * @see org.apache.niolex.network.IPacketHandler#handleClose(org.apache.niolex.network.IPacketWriter)
     */
    @Override
    public void handleClose(IPacketWriter wt) {
        IPacketHandler h = wt.getAttached(KEY);
        if (h != null) {
            h.handleClose(wt);
            // Help GC to collect that handler.
            wt.attachData(KEY, null);
        }
        LOG.info("Session removed for remote: {}", wt.getRemoteName());
    }

    /**
     * Find a proper handler to handle the packet, if we can not find one,
     * we will create a new one.
     *
     * Override super method
     * @see org.apache.niolex.network.IPacketHandler#handleRead(org.apache.niolex.network.PacketData, org.apache.niolex.network.IPacketWriter)
     */
    @Override
    public void handleRead(PacketData sc, IPacketWriter wt) {
        IPacketHandler h = wt.getAttached(KEY);
        if (h == null) {
            h = factory.createHandler(wt);
            wt.attachData(KEY, h);
            LOG.info("Session created for remote: {}", wt.getRemoteName());
        }
        h.handleRead(sc, wt);
    }

    /**
     * @return the session handler factory
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

}
