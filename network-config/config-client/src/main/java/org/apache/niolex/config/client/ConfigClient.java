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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.niolex.commons.codec.StringUtil;
import org.apache.niolex.commons.compress.JacksonUtil;
import org.apache.niolex.commons.config.PropUtil;
import org.apache.niolex.commons.download.DownloadUtil;
import org.apache.niolex.commons.file.FileUtil;
import org.apache.niolex.commons.util.Runme;
import org.apache.niolex.config.bean.ConfigItem;
import org.apache.niolex.config.bean.GroupConfig;
import org.apache.niolex.config.bean.SubscribeBean;
import org.apache.niolex.config.bean.SyncBean;
import org.apache.niolex.config.core.CodeMap;
import org.apache.niolex.config.core.ConfigException;
import org.apache.niolex.config.core.MemoryStorage;
import org.apache.niolex.config.core.PacketTranslater;
import org.apache.niolex.config.event.ConfigEventDispatcher;
import org.apache.niolex.config.event.ConfigListener;
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
    private static final long REFRESH_INTERVAL;
    private static final String STORAGE_PATH;

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

    /**
     * Store all the event dispatcher.
     */
    private static final ConcurrentHashMap<String, ConfigEventDispatcher> DISPATCHER = new ConcurrentHashMap<String, ConfigEventDispatcher>();

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
    	SERVER_ADDRESS = PropUtil.getProperty("server.address", "http://configserver:8780/configserver/server.json");
    	USERNAME = PropUtil.getProperty("auth.username", "node");
    	PASSWORD = PropUtil.getProperty("auth.password", "nodepasswd");
    	CONNECT_TIMEOUT = PropUtil.getInteger("server.contimeout", 30000);
    	// Default to 6 hours.
    	REFRESH_INTERVAL = PropUtil.getLong("server.refresh.interval", 21600000);
    	STORAGE_PATH = PropUtil.getProperty("local.storage.path", "/data/follower/config/storage");
    	FileUtil.mkdirsIfAbsent(STORAGE_PATH);
    	initConnection();
    }

    /**
     * Init server properties and connect to server.
     */
    private static final void initConnection() {
    	if (!syncServerAddress(true)) {
    		// There is nothing we can do, we can not start client without addresses.
    		return;
    	}
    	SERVER_IDX = (int) (System.nanoTime() % ADDRESSES.length);
    	CLIENT.setConnectTimeout(CONNECT_TIMEOUT);
    	CLIENT.setPacketHandler(new ClientHandler());
    	BEAN.setUserName(USERNAME);
    	BEAN.setPassword(PASSWORD);
    	// Connect with server.
    	connect();
    	// Sync with server periodically.
    	Runme me = new Runme() {

			@Override
			public void runMe() {
				syncWithServer();
			}

		};
		me.setSleepInterval(REFRESH_INTERVAL);
		me.setInitialSleep(true);
		me.start();
    }

    /**
     * Sync server addresses from http server.
     */
    private static final boolean syncServerAddress(boolean isStart) {
    	String json;
		try {
			json = StringUtil.utf8ByteToStr(DownloadUtil.downloadFile(SERVER_ADDRESS));
			FileUtil.setCharacterFileContentToFileSystem(STORAGE_PATH + "/server.json",
					json, Config.SERVER_ENCODING);
		} catch (Exception e) {
			LOG.error("Failed to download server address from remote.", e);
			if (isStart) {
				// Try to reload address list from backup config file.
				json = FileUtil.getCharacterFileContentFromFileSystem(
						STORAGE_PATH + "/server.json", Config.SERVER_ENCODING);
			} else {
				return false;
			}
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
    	ADDRESSES = new InetSocketAddress[servers.length];
    	for (int i = 0; i < servers.length; ++i) {
    		String str = servers[i];
    		String[] addrs = str.split(":");
    		ADDRESSES[i] = new InetSocketAddress(addrs[0], Integer.parseInt(addrs[1]));
    	}
    	return true;
    }

    /**
     * Sync all the local groups with server.
     */
    private static final void syncWithServer() {
    	LOG.info("Start to sync local groups with server.");
    	List<SyncBean> list = new ArrayList<SyncBean>();
    	for (GroupConfig tmp : STORAGE.getAll()) {
    		SyncBean bean = new SyncBean(tmp);
    		list.add(bean);
    	}
    	if (list.size() != 0) {
    		// Send local sync bean to config server.
    		PacketData syn = PacketTranslater.translate(list);
        	CLIENT.handleWrite(syn);
    	}
    	// Sync server addresses.
    	syncServerAddress(false);
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
    			List<ConfigItem> list = STORAGE.store(conf);
    			// dispatch event.
    			ConfigEventDispatcher disp = DISPATCHER.get(conf.getGroupName());
    			if (list != null && disp != null) {
    				for (ConfigItem item : list) {
    					disp.fireEvent(item);
    				}
    			}
    			// Notify anyone waiting for this.
    			WAITER.release(conf.getGroupName(), conf);
    			// Store this config to local disk.
    			storeGroupConfigToLocakDisk(STORAGE.get(conf.getGroupName()));
    			break;
    		case CodeMap.GROUP_DIF:
    			ConfigItem item = PacketTranslater.toConfigItem(sc);
    			// Store this item into memory storage.
    			String groupName = findGroupName(item);
    			updateConfigItem(groupName, item);
    			break;
    		default:
    			LOG.warn("Packet received for code [{}] have no handler, just ignored.", sc.getCode());
				break;
    	}
    }

    /**
     * Store the group config into local disk.
     * @param conf
     */
    private static final void storeGroupConfigToLocakDisk(GroupConfig conf) {
    	FileUtil.setBinaryFileContentToFileSystem(STORAGE_PATH + "/" + conf.getGroupName(),
    			PacketTranslater.translate(conf).getData());
    }

    /**
     * Store this item into memory, and dispatch event if necessary.
     * @param groupName
     * @param item
     */
    private static final void updateConfigItem(String groupName, ConfigItem item) {
    	boolean b = STORAGE.updateConfigItem(groupName, item);
		if (b) {
			// dispatch event.
			ConfigEventDispatcher disp = DISPATCHER.get(groupName);
			if (disp != null) {
				disp.fireEvent(item);
			}
		}
    }

    /**
     * Register an event listener.
     *
     * @param groupName
     * @param key
     * @param listener
     */
    protected static final ConfigListener registerEventHandler(String groupName, String key, ConfigListener listener) {
    	ConfigEventDispatcher disp = DISPATCHER.get(groupName);
		if (disp == null) {
			ConfigEventDispatcher tmp = new ConfigEventDispatcher();
			disp = DISPATCHER.putIfAbsent(groupName, tmp);
			if (disp != null) {
				return disp.addListener(key, listener);
			} else {
				return tmp.addListener(key, listener);
			}
		} else {
			return disp.addListener(key, listener);
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
    	InetSocketAddress serverAddress = null;
    	while (true) {
	    	try {
	    		serverAddress = ADDRESSES[nextServer()];
	    		CLIENT.setServerAddress(serverAddress);
				CLIENT.connect();
				break;
			} catch (Exception e) {
				LOG.info("Error occured when connect to address: {}, client will retry. {}",
						serverAddress, e.toString());
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

    private static final GroupConfig getGroupConfigFromLocakDisk(String groupName) {
    	try {
    		byte[] arr = FileUtil.getBinaryFileContentFromFileSystem(STORAGE_PATH + "/" + groupName);
    		if (arr != null) {
    			return JacksonUtil.str2Obj(StringUtil.utf8ByteToStr(arr), GroupConfig.class);
    		}
		} catch (Exception e) {}
    	return null;
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
    	// Try to load config from local disk.
    	tmp = getGroupConfigFromLocakDisk(groupName);
    	if (tmp != null) {
    		STORAGE.store(tmp);
    		// Add this group name to bean.
        	BEAN.getGroupList().add(groupName);
        	// Send packet to remote server.
        	PacketData sub = new PacketData(CodeMap.GROUP_SUB, StringUtil.strToUtf8Byte(groupName));
        	CLIENT.handleWrite(sub);
    		return tmp;
    	}
    	// Add this group name to bean.
    	BEAN.getGroupList().add(groupName);
    	checkConncetion();
    	BlockingWaiter<GroupConfig>.WaitOn on = WAITER.initWait(groupName);
    	// Send packet to remote server.
    	PacketData sub = new PacketData(CodeMap.GROUP_SUB, StringUtil.strToUtf8Byte(groupName));
    	CLIENT.handleWrite(sub);
    	return on.waitForResult(CONNECT_TIMEOUT);
    }
}
