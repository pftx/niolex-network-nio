/**
 * ReplicaServiceImpl.java
 *
 * Copyright 2012 Niolex, Inc.
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
package org.apache.niolex.config.service.impl;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import org.apache.niolex.commons.test.MockUtil;
import org.apache.niolex.config.core.CodeMap;
import org.apache.niolex.config.handler.GroupAddedHandler;
import org.apache.niolex.config.handler.GroupDiffHandler;
import org.apache.niolex.config.service.ReplicaService;
import org.apache.niolex.network.Config;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.client.PacketClient;
import org.apache.niolex.network.handler.DispatchPacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-7-6
 */
@Service
public class ReplicaServiceImpl implements ReplicaService {
	private static final Logger LOG = LoggerFactory.getLogger(ReplicaServiceImpl.class);
	private static final String AUTH = "3836a809b1bd88a0f093916a4bc46a6b";

	private final Map<String, PacketClient> otherServers = new HashMap<String, PacketClient>();

	@Autowired
	private GroupDiffHandler diffHandler;
	@Autowired
	private GroupAddedHandler addHandler;

	/**
	 * Override super method
	 * @see org.apache.niolex.config.service.ReplicaService#connectToOtherServers(java.net.InetSocketAddress[])
	 */
	@Override
	public synchronized void connectToOtherServers(InetSocketAddress[] addresses) {
		for (InetSocketAddress addr : addresses) {
			if (!otherServers.containsKey(addr.toString())) {
				tryConnectToOtherServer(addr);
			}
		}
	}

	public void tryConnectToOtherServer(InetSocketAddress addr) {
		final PacketClient client = new PacketClient();
		otherServers.put(addr.toString(), client);
		LOG.info("Want to connect to {}.", addr);

		client.setConnectTimeout(60000);
		client.setServerAddress(addr);
		ReConnectHandler handler = new ReConnectHandler();
		handler.addHandler(CodeMap.GROUP_DIF, diffHandler);
		handler.addHandler(CodeMap.GROUP_ADD, addHandler);
		client.setPacketHandler(handler);

		// Do try connect in another thread.
		new Thread() { public void run() {initConnection(client);} }.start();
	}

	/**
	 * Try to connect to the other server infinitely.
	 * @param client
	 */
	public void initConnection(PacketClient client) {
		while (true) {
	    	try {
	    		client.connect();
				break;
			} catch (Exception e) {
				LOG.info("Error occured when connect to address: {}, client will retry. {}",
						client.getServerAddress(), e.toString());
				try {
					Thread.sleep(3000 + MockUtil.ranInt(10000));
				} catch (InterruptedException e1) {}
			}
    	}
		connected(client);
	}

	/**
	 * Connected to other server, so we init heart beat and init server subscribe.
	 * @param client
	 */
	public void connected(PacketClient client) {
		client.handleWrite(new PacketData(Config.CODE_REGR_HBEAT));
		client.handleWrite(new PacketData(CodeMap.SERVER_SUBS, AUTH));
	}

	class ReConnectHandler extends DispatchPacketHandler {

		@Override
	    public void handleClose(IPacketWriter wt) {
			LOG.warn("Connection to {} lost, system will try to reconnect.", wt.getRemoteName());
			initConnection((PacketClient) wt);
		}

	}
}
