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

import java.io.File;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.niolex.commons.codec.StringUtil;
import org.apache.niolex.commons.compress.JacksonUtil;
import org.apache.niolex.commons.concurrent.Blocker;
import org.apache.niolex.commons.concurrent.BlockerException;
import org.apache.niolex.commons.concurrent.WaitOn;
import org.apache.niolex.commons.config.PropertiesWrapper;
import org.apache.niolex.commons.event.BaseEvent;
import org.apache.niolex.commons.event.ConcurrentEventDispatcher;
import org.apache.niolex.commons.event.Listener;
import org.apache.niolex.commons.file.DirUtil;
import org.apache.niolex.commons.file.FileUtil;
import org.apache.niolex.commons.net.DownloadUtil;
import org.apache.niolex.commons.util.Runme;
import org.apache.niolex.commons.util.SystemUtil;
import org.apache.niolex.config.bean.ConfigGroup;
import org.apache.niolex.config.bean.ConfigItem;
import org.apache.niolex.config.bean.SubscribeBean;
import org.apache.niolex.config.bean.SyncBean;
import org.apache.niolex.config.core.CodeMap;
import org.apache.niolex.config.core.ConfigException;
import org.apache.niolex.config.core.MemoryStorage;
import org.apache.niolex.config.core.PacketTranslater;
import org.apache.niolex.network.Config;
import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.client.ClientManager;
import org.apache.niolex.network.client.PacketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configer client, manage local config files, connecting to config server,
 * get config data, sync with server periodically.
 * 
 * Server will push new config items to clients with long connection.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-7-3
 */
public class ConfigClient {
	private static final Logger LOG = LoggerFactory.getLogger(ConfigClient.class);

	/**
	 * Server related properties.
	 */
    private static final String USERNAME;
    private static final String PASSWORD;
    private static final int CONNECT_TIMEOUT;
    private static final long REFRESH_INTERVAL;
    private static final String STORAGE_PATH;

    /**
     * The wait for connected lock.
     */
    private static final Lock LOCK = new ReentrantLock();

    /**
     * The real packet client do the TCP connection.
     */
    private static final PacketClient CLIENT = new PacketClient();

    /**
     * The manager to manage client connection status.
     */
    private static final ClientManager MGR = new ClientManager(CLIENT);

    /**
     * The client subscribe bean.
     */
    private static final SubscribeBean BEAN = new SubscribeBean();

	/**
	 * The help class to wait for result.
	 */
    private static final Blocker<ConfigGroup> WAITER = new Blocker<ConfigGroup>();

    /**
     * Store all the configurations in memory.
     */
    private static final MemoryStorage STORAGE = new MemoryStorage();

    /**
     * The event dispatcher.
     */
    private static final ConcurrentEventDispatcher DISPATCHER = new ConcurrentEventDispatcher();
    
    /**
     * Store all the configuration properties load from local file system.
     */
    private static final PropertiesWrapper PROP = new PropertiesWrapper();

    //------------------------------------------------------------

    /**
     * Non final fields
     */
    private static String SERVER_ADDRESS;
    private static Condition WAIT_CONNECTED;
    private static InitStatus INIT_STATUS = InitStatus.INIT;

    private static enum InitStatus {
    	INIT, TRY, CONNECTED, AUTHED, FAILED, NOAUTH;
    }

    /**
     * Init properties from config file.
     */
    static {
    	try {
    		String fileName = SystemUtil.getSystemProperty("ConfigClient.configurationFile", "config-client-properties",
    		        "config.client.property.file");
    		if (fileName != null) {
    		    PROP.load(fileName);
    			LOG.info("Config file [{}] loaded into config client.", fileName);
    		} else {
    			PROP.load("/conf-client.properties", ConfigClient.class);
    			LOG.info("Config file [conf-client.properties] loaded into config client.");
    		}
        } catch (Throwable t) {
            LOG.info("conf-client.properties not found, use default configurations instead.");
        }
    	SERVER_ADDRESS = PROP.getProperty("server.address", "http://configserver:8780/configserver/server.json");
    	USERNAME = PROP.getProperty("auth.username", "node");
    	PASSWORD = PROP.getProperty("auth.password", "nodepasswd");
    	BEAN.setUserName(USERNAME);
    	BEAN.setPassword(PASSWORD);
    	CONNECT_TIMEOUT = PROP.getInteger("server.contimeout", 30000);
    	CLIENT.setConnectTimeout(CONNECT_TIMEOUT);
    	CLIENT.setPacketHandler(new ClientHandler());
    	
    	// Default to 1 hour.
    	REFRESH_INTERVAL = PROP.getLong("server.refresh.interval", 3600000);
    	STORAGE_PATH = PROP.getProperty("local.storage.path", "/data/config-client/storage");
    	DirUtil.mkdirsIfAbsent(STORAGE_PATH);
    	// Start to init connection in this main thread.
    	initConnection();
    }

    /**
     * Init server properties and connect to server.
     */
    private static final void initConnection() {
    	// Sync with server at startup, if can not connect to http server,
    	// system will try local file at this time.
    	if (!syncServerAddress(true)) {
    	    LOG.error("Can not get config server address, config client will not work!!!!");
    	    return;
    	}

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
     * If parameter is true, we will try to load server addresses from local disk if http server is unavailable.
     *
     * @param isStart is starting this client or not
     */
    private static final boolean syncServerAddress(boolean isStart) {
    	String json;
		try {
			json = StringUtil.utf8ByteToStr(DownloadUtil.downloadFile(SERVER_ADDRESS));
			// Store json to local disk.
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
    		// There is nothing we can do, we can not start client without addresses.
    		LOG.error("Server address is empty, init failed.");
    		return false;
    	}
    	
    	final List<InetSocketAddress> tmp = new ArrayList<InetSocketAddress>(servers.length);
    	for (int i = 0; i < servers.length; ++i) {
    		String str = servers[i];
    		String[] addrs = str.split(":");
    		tmp.add(new InetSocketAddress(addrs[0], Integer.parseInt(addrs[1])));
    	}
    	
    	// Update manager with the new address list.
    	MGR.setAddressList(tmp);
    	if (isStart) {
    		// Connect with server.
    	    firstConnect();
    	}
    	return true;
    }

    /**
     * Sync all the local groups with server.
     */
    private static final void syncWithServer() {
    	// Sync server addresses.
    	syncServerAddress(false);
    	if (INIT_STATUS == InitStatus.AUTHED) {
	    	LOG.info("Start to sync local groups with server.");
	    	List<SyncBean> list = new ArrayList<SyncBean>();
	    	for (ConfigGroup tmp : STORAGE.getAll()) {
	    		SyncBean bean = new SyncBean(tmp);
	    		list.add(bean);
	    	}
	    	if (list.size() != 0) {
	    		// Send local sync bean to config server.
	    		PacketData syn = PacketTranslater.translate(list);
	        	CLIENT.handleWrite(syn);
	    	}
    	}
    }

    /**
     * Try infinitely to get a valid connection.
     */
    private static final void firstConnect() {
        MGR.setConnectRetryTimes(Integer.MAX_VALUE);
        if (MGR.connect()) {
            connected();
        } else {
            INIT_STATUS = InitStatus.TRY;
            // Wait for client get connected.
            new Thread() { public void run() {connected();} }.start();
        }
    }

    /**
     * Try infinitely to get a valid connection.
     */
    private static final void reConnect() {
        MGR.setConnectRetryTimes(Integer.MAX_VALUE);
        if (MGR.retryConnect()) {
            connected();
        } else {
            LOG.error("Failed to retry connect to config server: exceeds max retry times.");
        }
    }

    /**
     * Init connection when connected.
     */
    private static final void connected() {
        try {
            if (MGR.waitForConnected()) {
                INIT_STATUS = InitStatus.CONNECTED;
                initSubscribe();
                // Client connected, notify all the waiters.
                notifyAllWaiter();
            }
        } catch (InterruptedException e) {
            LOG.error("Error occured when wait for connect to config server.", e);
        }
    }

    /**
     * Init subscribe when client is connected to server.
     */
    private static final void initSubscribe() {
        // 1. Register heart beat.
        CLIENT.handleWrite(new PacketData(Config.CODE_REGR_HBEAT));
        
        PacketData init = PacketTranslater.translate(BEAN);
        // 2. Register interested groups and authenticate this client.
        CLIENT.handleWrite(init);
    }

    /**
     * The client packet handler class.
     * Delegate all the methods out.
     *
     * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
     * @version 1.0.0
     * @since 2012-7-3
     */
    private static final class ClientHandler implements IPacketHandler {

		/**
		 * Override super method
		 * @see org.apache.niolex.network.IPacketHandler#handlePacket(org.apache.niolex.network.PacketData, org.apache.niolex.network.IPacketWriter)
		 */
		@Override
		public void handlePacket(PacketData sc, IPacketWriter wt) {
			doRead(sc);
		}

		/**
		 * Override super method
		 * @see org.apache.niolex.network.IPacketHandler#handleClose(org.apache.niolex.network.IPacketWriter)
		 */
		@Override
		public void handleClose(IPacketWriter wt) {
			// We will try to reconnect to server.
			INIT_STATUS = InitStatus.FAILED;
			reConnect();
		}

    }

    /**
     * Get group name by group id.
     * 
     * @param groupId the group ID
     * @return the group name
     */
    private static final String findGroupName(ConfigItem item) {
    	return STORAGE.findGroupName(item.getGroupId());
    }

    /**
     * Do the real packet reading which is from config server.
     * 
     * @param sc the packet data from config server
     */
    private static final void doRead(PacketData sc) {
    	switch(sc.getCode()) {
    		case CodeMap.GROUP_DAT:
    			// When group config data arrived, store it into memory storage.
    			ConfigGroup conf = PacketTranslater.toConfigGroup(sc);
    			List<ConfigItem> list = STORAGE.store(conf);
    			
    			// dispatch event.
    			if (list != null) {
    				for (ConfigItem item : list) {
    					fireEvent(conf.getGroupName(), item);
    				}
    			}
    			
    			// Notify anyone waiting for this.
    			WAITER.release(conf.getGroupName(), conf);
    			// Store this config to local disk.
    			storeConfigGroupToLocakDisk(STORAGE.get(conf.getGroupName()));
    			break;
    		case CodeMap.GROUP_DIF:
    			ConfigItem item = PacketTranslater.toConfigItem(sc);
    			
    			// Store this item into memory storage.
    			String groupName = findGroupName(item);
    			if (groupName != null) {
    				updateConfigItem(groupName, item);
    				// Store this config to local disk.
        			storeConfigGroupToLocakDisk(STORAGE.get(groupName));
    			}
    			break;
    		case CodeMap.GROUP_NOA:
    			groupName = StringUtil.utf8ByteToStr(sc.getData());
    			ConfigGroup group = STORAGE.get(groupName);
    			if (group != null) {
    				// If the group authentication has been removed, we need to delete it from local disk.
    				group.getGroupData().clear();
    				BEAN.getGroupSet().remove(groupName);
    				deleteConfigGroupFromLocakDisk(group);
    			}
    			
    			// Notify anyone waiting for this.
    			boolean b = WAITER.release(groupName, new BlockerException("You are not authorised to read this group."));
    			if (!b) {
    				// Nobody waiting for this, we will write a log.
    				LOG.error("You are not authorised to read this group: {}.", groupName);
    			}
    			break;
    		case CodeMap.GROUP_NOF:
    			groupName = StringUtil.utf8ByteToStr(sc.getData());
    			// Notify anyone waiting for this.
    			WAITER.release(groupName, new BlockerException("Group not found."));
    			break;
    		case CodeMap.AUTH_FAIL:
    			LOG.error("Authentication failure, config client will stop.");
    			CLIENT.stop();
    			INIT_STATUS = InitStatus.NOAUTH;
    			break;
    		case CodeMap.AUTH_SUCC:
    		    // Do not care about auth success.
                break;
    		default:
    			LOG.warn("Packet received for code [{}] have no handler, just ignored.", sc.getCode());
				break;
    	}
    }

    /**
     * Store this item into memory, and dispatch event if necessary.
     * 
     * @param groupName the config group name
     * @param item the config item
     */
    private static final void updateConfigItem(String groupName, ConfigItem item) {
    	boolean b = STORAGE.updateConfigItem(groupName, item);
		if (b) {
			// dispatch event.
		    fireEvent(groupName, item);
		}
    }
    
    /**
     * Fire the event of config item updated to all the event listeners.
     * 
     * @param groupName the config group name
     * @param item the config item
     */
    protected static final void fireEvent(String groupName, ConfigItem item) {
        BaseEvent<ConfigItem> event = new BaseEvent<ConfigItem>(groupName + "/*[^]&%L@(#)*/" + item.getKey(), item);
        DISPATCHER.fireEvent(event);
    }

    /**
     * Register an event listener.
     *
     * @param groupName the config group name
     * @param key the config item key
     * @param listener the config item change listener
     */
    protected static final void registerEventHandler(String groupName, String key, Listener<ConfigItem> listener) {
        DISPATCHER.addListener(groupName + "/*[^]&%L@(#)*/" + key, listener);
    }

    /**
     * Notify all the waiters that connection is ready now.
     */
    private static final void notifyAllWaiter() {
    	LOCK.lock();
    	try {
    		INIT_STATUS = InitStatus.AUTHED;
    		if (WAIT_CONNECTED != null) {
    			WAIT_CONNECTED.signalAll();
    			WAIT_CONNECTED = null;
    		}
    	} finally {
    		LOCK.unlock();
    	}
    }

    /**
     * Wait for this client to connect to any server.
     * 
     * @throws InterruptedException if interrupted when wait
     */
    private static final void waitForConnected() throws InterruptedException {
    	LOCK.lock();
    	try {
    		if (INIT_STATUS == InitStatus.AUTHED || INIT_STATUS == InitStatus.NOAUTH) {
    			return;
    		}
    		if (WAIT_CONNECTED == null) {
    			WAIT_CONNECTED = LOCK.newCondition();
    		}
    		WAIT_CONNECTED.await(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS);
    	} finally {
    		LOCK.unlock();
    	}
    }

    /**
     * Check the connection status.
     */
    private static final void checkConncetion() {
    	try {
			waitForConnected();
		} catch (Exception e) {}
    	if (INIT_STATUS != InitStatus.AUTHED) {
    		throw new ConfigException("Connection lost from config server, please try again later.");
    	}
    }

    /**
     * Load group config from local disk file.
     * 
     * @param groupName the config group name
     * @return the config group
     */
    private static final ConfigGroup getConfigGroupFromLocakDisk(String groupName) {
    	try {
    		byte[] arr = FileUtil.getBinaryFileContentFromFileSystem(STORAGE_PATH + "/" + groupName);
    		if (arr != null) {
    			return JacksonUtil.str2Obj(StringUtil.utf8ByteToStr(arr), ConfigGroup.class);
    		}
		} catch (Exception e) {
			LOG.warn("Error occured when load config from local storage - {}", e.toString());
		}
    	return null;
    }

    /**
     * Delete the group config from local disk.
     * 
     * @param conf the config group
     */
    private static final void deleteConfigGroupFromLocakDisk(ConfigGroup conf) {
        File f = new File(STORAGE_PATH + "/" + conf.getGroupName());
        if (f.exists()) {
            f.delete();
        }
    }

    /**
     * Store the group config into local disk.
     * 
     * @param conf the config group
     */
    private static final void storeConfigGroupToLocakDisk(ConfigGroup conf) {
        FileUtil.setBinaryFileContentToFileSystem(STORAGE_PATH + "/" + conf.getGroupName(),
                PacketTranslater.translate(conf).getData());
    }

    /**
     * Get config group by group name. If not found in local, we will try to
     * get it from config server. We prefer remote result if connection is ready.
     * 
     * This method can be invoked from multi-threads.
     *
     * @param groupName the config group name
     * @return the config group
     */
    protected static  final ConfigGroup getConfigGroup(String groupName) {
    	// Find data from local memory.
    	ConfigGroup tmp = STORAGE.get(groupName);
    	if (tmp != null)
    		return tmp;
    	
    	// 1. First, we prefer remote config group, it's up-to-date.
    	if (INIT_STATUS == InitStatus.AUTHED) {
    	    // Already connected to server.
    	    return getConfigGroupFromRemote(groupName);
    	}
    	
    	// 2. Try to load config from local disk.
    	tmp = getConfigGroupFromLocakDisk(groupName);
    	if (tmp != null) {
    		// OK, we find config from disk, we store it into memory immediately.
    		STORAGE.store(tmp);
    		// Add this group name to bean.
    		BEAN.getGroupSet().add(groupName);
    		return tmp;
    	}
    	
    	// 3. At this condition, we can not found it from disk, and connection is not ready.
    	// It's so sad, we must wait for connection.
    	checkConncetion();
    	// Already connected to server.
        return getConfigGroupFromRemote(groupName);
    }

    /**
     * Try to get config group from remote directly.
     * 
     * This method is synchronized by group name.
     *
     * @param groupName the config group name
     * @return the config group
     */
    private static final ConfigGroup getConfigGroupFromRemote(String groupName) {
        // Synchronize with group name.
        groupName = groupName.intern();
        
        synchronized (groupName) {
            // Check it again from local storage.
            ConfigGroup cg = STORAGE.get(groupName);
            if (cg != null)
                return cg;
            
            // Add this group name to bean.
            BEAN.getGroupSet().add(groupName);
            WaitOn<ConfigGroup> on = WAITER.init(groupName);
            
            // Send packet to remote server.
            PacketData sub = new PacketData(CodeMap.GROUP_SUB, StringUtil.strToUtf8Byte(groupName));
            CLIENT.handleWrite(sub);
            
            try {
                return on.waitForResult(CONNECT_TIMEOUT);
            } catch (InterruptedException e) {
                throw new ConfigException("Interruped when wait from result from server.");
            } catch (BlockerException e) {
                throw new ConfigException(e.getMessage());
            }
        }
    }
    
}
