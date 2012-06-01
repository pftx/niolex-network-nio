/**
 * EchoPacketHandler.java
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

import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Echo the input Packet to the output, just for test.
 * @author Xie, Jiyun
 *
 */
public class EchoPacketHandler implements IPacketHandler {
	private static final Logger LOG = LoggerFactory.getLogger(EchoPacketHandler.class);

    /* (non-Javadoc)
     * @see com.renren.ad.datacenter.follower.network.IPacketHandler#handleError()
     */
    @Override
    public void handleError(IPacketWriter wt) {
    	LOG.warn("Error occured from remote: " + wt.getRemoteName());
    }

    /* (non-Javadoc)
     * @see com.renren.ad.datacenter.follower.network.IPacketHandler#handleRead(com.renren.ad.datacenter.follower.network.Packet, com.renren.ad.datacenter.follower.network.IPacketWriter)
     */
    @Override
    public void handleRead(PacketData sc, IPacketWriter wt) {
        wt.handleWrite(sc);
        LOG.info("Packet with code [{}] from remote [{}] echoed back.", sc.getCode()
        		, wt.getRemoteName());
    }

}
