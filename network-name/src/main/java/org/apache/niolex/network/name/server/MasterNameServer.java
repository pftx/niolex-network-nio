/**
 * MasterNameServer.java
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
package org.apache.niolex.network.name.server;

import org.apache.niolex.network.name.bean.RecordStorage;
import org.apache.niolex.network.name.core.NameServer;
import org.apache.niolex.network.name.event.ConcurrentDispatcher;
import org.apache.niolex.network.name.event.IDispatcher;
import org.apache.niolex.network.server.MultiNioServer;

/**
 * The master name server.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-29
 */
public class MasterNameServer {

	private final NameServer nameServer;
	private final MultiNioServer iServer;
	private final RecordStorage storage;

	public MasterNameServer() {
		super();
		iServer = new MultiNioServer();
		nameServer = new NameServer(iServer);
		storage = new RecordStorage();
		nameServer.setStorage(storage);
		IDispatcher dispatcher = new ConcurrentDispatcher();
		nameServer.setDispatcher(dispatcher);
		storage.setDispatcher(dispatcher);
	}

	public boolean start() {
		return nameServer.start();
	}

	public void stop() {
		nameServer.stop();
	}

	public void setPort(int port) {
		iServer.setPort(port);
	}

	public void setDeleteTime(int deleteTime) {
		storage.setDeleteTime(deleteTime);
	}

}