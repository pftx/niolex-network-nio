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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.niolex.commons.codec.StringUtil;
import org.apache.niolex.commons.test.MockUtil;
import org.apache.niolex.commons.util.DateTimeUtil;
import org.apache.niolex.commons.util.SystemUtil;
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
	private static final Set<InetAddress> IP_SET = SystemUtil.getAllLocalAddresses();

	private final Map<String, PacketClient> otherServers = new HashMap<String, PacketClient>();

	@Autowired
	private GroupDiffHandler diffHandler;
	@Autowired
	private GroupAddedHandler addHandler;
	// The local Server listening port
	private int localPort;

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
		/**
		 * First of all, let's remove localhost from the list.
		 */
		if (IP_SET.contains(addr.getAddress()) && localPort == addr.getPort()) {
			return;
		}
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
		long sleepTime = 0;
		while (true) {
	    	try {
	    		client.connect();
				break;
			} catch (Exception e) {
				LOG.info("Error occured when connect to address: {}, client will retry. {}",
						client.getServerAddress(), e.toString());
				try {
					if (sleepTime < DateTimeUtil.MINUTE * 10) {
						sleepTime += 3000 + MockUtil.ranInt(10000);
					}
					Thread.sleep(sleepTime);
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
		private boolean reTry = true;

		/**
		 * Add this to listen server subscribe result.
		 */
		@Override
	    public void handleRead(PacketData sc, IPacketWriter wt) {
			if (sc.getCode() == CodeMap.RES_SERVER_SUBS) {
				String result = StringUtil.utf8ByteToStr(sc.getData());
				if (!result.equalsIgnoreCase("OK")) {
					// Subscribe to server failed.
					LOG.warn("Subscribe to server {} failed: {}.", wt.getRemoteName(), result);
					reTry = false;
					((PacketClient) wt).stop();
				}
			} else {
				super.handleRead(sc, wt);
			}
		}

		@Override
	    public void handleClose(IPacketWriter wt) {
			if (reTry) {
				LOG.warn("Connection to {} lost, system will try to reconnect.", wt.getRemoteName());
				initConnection((PacketClient) wt);
			}
		}

	}

	public void setDiffHandler(GroupDiffHandler diffHandler) {
		this.diffHandler = diffHandler;
	}

	public void setAddHandler(GroupAddedHandler addHandler) {
		this.addHandler = addHandler;
	}

	public void setLocalPort(int localPort) {
		this.localPort = localPort;
	}

}
