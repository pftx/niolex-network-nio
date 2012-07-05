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

import org.apache.niolex.commons.util.Runme;
import org.apache.niolex.config.core.CodeMap;
import org.apache.niolex.config.handler.AuthSubscribeHandler;
import org.apache.niolex.config.handler.GroupSubscribeHandler;
import org.apache.niolex.network.IServer;
import org.apache.niolex.network.adapter.HeartBeatAdapter;
import org.apache.niolex.network.handler.DispatchPacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
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

	//---------------------------------------------------------------------
	// All the packet handlers here.
	//---------------------------------------------------------------------
	@Autowired
	private AuthSubscribeHandler authHandler;
	@Autowired
	private GroupSubscribeHandler subsHandler;
	//---------------------------------------------------------------------

	@Autowired
	public ConfigServer(IServer server) {
		super();
		this.server = server;
		DispatchPacketHandler handler = new DispatchPacketHandler();

		// --- register all handlers into dispatch packet handler ---
		handler.addHandler(CodeMap.AUTH_SUBS, authHandler);
		handler.addHandler(CodeMap.GROUP_SUB, subsHandler);
		// --------------- end of register --------------------------

		heartBeatAdapter = new HeartBeatAdapter(handler);
		this.server.setPacketHandler(heartBeatAdapter);
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
