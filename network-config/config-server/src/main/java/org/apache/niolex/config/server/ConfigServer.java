/**
 * ConfigServer.java
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
package org.apache.niolex.config.server;

import java.net.InetSocketAddress;

import org.apache.niolex.commons.codec.StringUtil;
import org.apache.niolex.commons.compress.JacksonUtil;
import org.apache.niolex.commons.download.DownloadUtil;
import org.apache.niolex.commons.util.Runme;
import org.apache.niolex.config.core.CodeMap;
import org.apache.niolex.config.handler.AuthSubscribeHandler;
import org.apache.niolex.config.handler.GroupSubscribeHandler;
import org.apache.niolex.config.handler.GroupSyncHandler;
import org.apache.niolex.network.IServer;
import org.apache.niolex.network.adapter.HeartBeatAdapter;
import org.apache.niolex.network.handler.DispatchPacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 * The central controller of config server.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-21
 */
@Controller
public class ConfigServer {

	private static final Logger LOG = LoggerFactory.getLogger(ConfigServer.class);

	/**
	 * The real server implementation.
	 */
	private final IServer server;

	/**
	 * Send out heart beat packet.
	 */
	private final HeartBeatAdapter heartBeatAdapter;

	/**
	 * Use this thread to sync configurations with DB.
	 */
	private Runme syncThread;

	private String httpServerAddress;

	private InetSocketAddress[] addresses;

	//---------------------------------------------------------------------
	// All the packet handlers here.
	//---------------------------------------------------------------------
	@Autowired
	private AuthSubscribeHandler authHandler;
	@Autowired
	private GroupSubscribeHandler subsHandler;
	@Autowired
	private GroupSyncHandler syncHandler;
	//---------------------------------------------------------------------

	@Autowired
	public ConfigServer(IServer server) {
		super();
		this.server = server;
		DispatchPacketHandler handler = new DispatchPacketHandler();

		// --- register all handlers into dispatch packet handler ---
		handler.addHandler(CodeMap.AUTH_SUBS, authHandler);
		handler.addHandler(CodeMap.GROUP_SUB, subsHandler);
		handler.addHandler(CodeMap.GROUP_SYN, syncHandler);
		// --------------- end of register --------------------------

		heartBeatAdapter = new HeartBeatAdapter(handler);
		this.server.setPacketHandler(heartBeatAdapter);

		// Sync server addresses from http server.
		if (!syncServerAddress(true)) {
    		// There is nothing we can do, we can not start client without addresses.
    		return;
    	}

		// Start clients to connect to other servers for diff.
	}


    /**
     * Sync server addresses from http server.
     */
    private final boolean syncServerAddress(boolean isStart) {
    	String json;
		try {
			json = StringUtil.utf8ByteToStr(DownloadUtil.downloadFile(httpServerAddress));
		} catch (Exception e) {
			LOG.error("Failed to download server address from remote.", e);
			return false;
		}
		String[] servers = null;
		try {
			servers = JacksonUtil.str2Obj(json, String[].class);
		} catch (Exception e) {
			LOG.error("Failed to parse server address as json.", e);
			return false;
		}
    	if (servers.length < 1) {
    		LOG.error("Server address is empty, init failed.");
    		return false;
    	}
    	addresses = new InetSocketAddress[servers.length];
    	for (int i = 0; i < servers.length; ++i) {
    		String str = servers[i];
    		String[] addrs = str.split(":");
    		addresses[i] = new InetSocketAddress(addrs[0], Integer.parseInt(addrs[1]));
    	}
    	return true;
    }

	/**
	 * Start the Server, bind to the Port. Server need to start threads internally to run. This method need to return
	 * after this server is started.
	 */
	public boolean start() {
		if (server.start()) {
			heartBeatAdapter.start();
			syncThread = new Runme() {

				@Override
				public void runMe() {
					// sync from DB
					syncWithDB();
				}

			};
			syncThread.setSleepInterval(5000);
			syncThread.setInitialSleep(true);
			syncThread.start();
		}
		return false;
	}

	private void syncWithDB() {
		LOG.info("System will try to sync with DB now.");
		// TODO Auto-generated method stub
	}

	/**
	 * Stop this server. After stop, the internal threads need to be stopped.
	 */
	public void stop() {
		server.stop();
		heartBeatAdapter.stop();
		syncThread.stopMe();
	}

}
