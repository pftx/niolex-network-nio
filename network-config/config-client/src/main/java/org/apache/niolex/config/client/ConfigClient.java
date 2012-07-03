/**
 * ConfigClient.java
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
package org.apache.niolex.config.client;

import java.net.InetSocketAddress;

import org.apache.niolex.commons.codec.StringUtil;
import org.apache.niolex.commons.config.PropUtil;
import org.apache.niolex.config.bean.ConfigItem;
import org.apache.niolex.config.bean.GroupConfig;
import org.apache.niolex.config.bean.SubscribeBean;
import org.apache.niolex.config.core.CodeMap;
import org.apache.niolex.config.core.ConfigException;
import org.apache.niolex.config.core.MemoryStorage;
import org.apache.niolex.config.core.PacketTranslater;
import org.apache.niolex.network.Config;
import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.client.PacketClient;
import org.apache.niolex.network.util.BlockingWaiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-7-3
 */
public class ConfigClient {
	private static final Logger LOG = LoggerFactory.getLogger(ConfigClient.class);

    private static final String USERNAME;
    private static final String PASSWORD;
    private static final int CONNECT_TIMEOUT;

    /**
     * The real packet client do the tcp connection.
     */
    private static final PacketClient CLIENT = new PacketClient();

    /**
     * The client subscribe bean.
     */
    private static final SubscribeBean BEAN = new SubscribeBean();

	/**
	 * The help class to wait for result.
	 */
    private static final BlockingWaiter<GroupConfig> WAITER = new BlockingWaiter<GroupConfig>();

    /**
     * Store all the configurations in memory.
     */
    private static final MemoryStorage STORAGE = new MemoryStorage();

    //------------------------------------------------------------

    /**
     * Non final fields
     */
    private static String SERVER_ADDRESS;
    private static InetSocketAddress[] ADDRESSES;
    private static int SERVER_IDX;

    static {
    	try {
            PropUtil.loadConfig("/conf-client.properties", ConfigClient.class);
        } catch (Throwable t) {
            LOG.info("conf-client.properties not found, use default configurations instead.");
        }
    	SERVER_ADDRESS = PropUtil.getProperty("server.address", "localhost:8123,localhost:8181");
    	USERNAME = PropUtil.getProperty("auth.username", "node");
    	PASSWORD = PropUtil.getProperty("auth.password", "nodepasswd");
    	CONNECT_TIMEOUT = PropUtil.getInteger("server.contimeout", 30000);
    	initConnection();
    }

    /**
     * Init server properties and connect to server.
     */
    private static final void initConnection() {
    	String[] servers = SERVER_ADDRESS.split(" *, *");
    	if (servers.length < 1) {
    		LOG.error("Server address is empty, init failed.");
    		return;
    	}
    	ADDRESSES = new InetSocketAddress[servers.length];
    	for (int i = 0; i < servers.length; ++i) {
    		String str = servers[i];
    		String[] addrs = str.split(":");
    		ADDRESSES[i] = new InetSocketAddress(addrs[0], Integer.parseInt(addrs[1]));
    	}
    	SERVER_IDX = (int) (System.nanoTime() % servers.length);
    	CLIENT.setConnectTimeout(CONNECT_TIMEOUT);
    	CLIENT.setPacketHandler(new ClientHandler());
    	BEAN.setUserName(USERNAME);
    	BEAN.setPassword(PASSWORD);
    	connect();
    }

    /**
     * The client packet handler class.
     * Delegate all the methods out.
     *
     * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
     * @version 1.0.0
     * @Date: 2012-7-3
     */
    private static final class ClientHandler implements IPacketHandler {

		/**
		 * Override super method
		 * @see org.apache.niolex.network.IPacketHandler#handleRead(org.apache.niolex.network.PacketData, org.apache.niolex.network.IPacketWriter)
		 */
		@Override
		public void handleRead(PacketData sc, IPacketWriter wt) {
			doRead(sc);
		}

		/**
		 * Override super method
		 * @see org.apache.niolex.network.IPacketHandler#handleClose(org.apache.niolex.network.IPacketWriter)
		 */
		@Override
		public void handleClose(IPacketWriter wt) {
			// We will try to reconnect to server.
			connect();
		}

    }

    /**
     * Get group name by group id.
     * @param groupId
     * @return
     */
    private static final String findGroupName(ConfigItem item) {
    	return STORAGE.findGroupName(item.getGroupId());
    }

    /**
     * Do the real packet reading which is from config server.
     * @param sc
     */
    private static final void doRead(PacketData sc) {
    	switch(sc.getCode()) {
    		case CodeMap.GROUP_DAT:
    			// When group config data arrived, store it into memory storage.
    			GroupConfig conf = PacketTranslater.toGroupConfig(sc);
    			STORAGE.store(conf);
    			// Notify anyone waiting for this.
    			WAITER.release(conf.getGroupName(), conf);
    			break;
    		case CodeMap.GROUP_DIF:
    			ConfigItem item = PacketTranslater.toConfigItem(sc);
    			// Store this item into memory storage.
    			String groupName = findGroupName(item);
    			boolean b = STORAGE.updateConfigItem(groupName, item);
    			if (b) {
    				//TODO dispatch event.
    			}
    			break;
    	}
    }

    /**
     * Return the next server index.
     * @return
     */
    private static final int nextServer() {
    	return SERVER_IDX = ++SERVER_IDX % ADDRESSES.length;
    }

    /**
     * Try infinitely to get a valid connection.
     */
    private static final void connect() {
    	while (true) {
	    	try {
	    		CLIENT.setServerAddress(ADDRESSES[nextServer()]);
				CLIENT.connect();
				break;
			} catch (Exception e) {
				try {
					Thread.sleep(CONNECT_TIMEOUT / 3);
				} catch (InterruptedException e1) {}
			}
    	}
    	initSubscribe();
    }

    /**
     * Init subscribe when client is connected to server.
     */
    private static final void initSubscribe() {
    	// 1. Register heart beat.
    	CLIENT.handleWrite(new PacketData(Config.CODE_REGR_HBEAT));
    	PacketData init = PacketTranslater.translate(BEAN);
    	// 2. Register interested groups.
		CLIENT.handleWrite(init);
    }

    /**
     * Check the connection status.
     */
    private static final void checkConncetion() {
    	if (!CLIENT.isWorking()) {
    		throw new ConfigException("Connection lost from config server, please try again later.");
    	}
    }

    /**
     * Get group config by group name. If not found in local, we will try to
     * get it from config server.
     *
     * This method is synchronized.
     *
     * @param groupName
     * @return
     * @throws InterruptedException
     */
    protected static synchronized final GroupConfig getGroupConfig(String groupName) throws InterruptedException {
    	// Find data from local memory.
    	GroupConfig tmp = STORAGE.get(groupName);
    	if (tmp != null)
    		return tmp;
    	checkConncetion();
    	BlockingWaiter<GroupConfig>.WaitOn on = WAITER.initWait(groupName);
    	// Add this group name to bean.
    	BEAN.getGroupList().add(groupName);
    	// Send packet to remote server.
    	PacketData sub = new PacketData(CodeMap.GROUP_SUB, StringUtil.strToUtf8Byte(groupName));
    	CLIENT.handleWrite(sub);
    	return on.waitForResult(CONNECT_TIMEOUT);
    }
}
