/**
 * UpdaterClient.java
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
package org.apache.niolex.config.admin;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.niolex.commons.codec.StringUtil;
import org.apache.niolex.commons.concurrent.Blocker;
import org.apache.niolex.commons.concurrent.WaitOn;
import org.apache.niolex.config.bean.ConfigGroup;
import org.apache.niolex.config.bean.ConfigItem;
import org.apache.niolex.config.bean.SubscribeBean;
import org.apache.niolex.config.bean.UserInfo;
import org.apache.niolex.config.core.CodeMap;
import org.apache.niolex.config.core.MemoryStorage;
import org.apache.niolex.config.core.PacketTranslater;
import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.client.PacketClient;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-7-9
 */
public class UpdaterClient implements Updater, IPacketHandler {

	/**
	 * The internal managed packet client.
	 */
	protected final PacketClient client = new PacketClient();

	protected final Blocker<String> waiter = new Blocker<String>();

	protected final MemoryStorage storage = new MemoryStorage();

	final int waitForTimeout = 10000;


	/**
	 * Create a new updater client connect to this server.
	 * 
	 * @param serverAddress the config server address list
	 * @throws IOException if necessary
	 */
	public UpdaterClient(String serverAddress) throws IOException {
		super();
		String[] addrs = serverAddress.split(":");
		client.setServerAddress(new InetSocketAddress(addrs[0], Integer.parseInt(addrs[1])));
		client.setPacketHandler(this);
		client.connect();
	}


	/**
	 * Override super method
	 * @throws Exception if necessary
	 * @see org.apache.niolex.config.admin.Updater#subscribeAuthInfo(java.lang.String, java.lang.String)
	 */
	@Override
	public String subscribeAuthInfo(String username, String password) throws Exception {
		SubscribeBean bean = new SubscribeBean();
		bean.setUserName(username);
		bean.setPassword(password);
		PacketData p = PacketTranslater.translate(bean);
		WaitOn<String> on = waiter.init("auth");
        client.handleWrite(p);
        return on.waitForResult(waitForTimeout);
	}

	/**
	 * Override super method
	 * @throws Exception if necessary
	 * @see org.apache.niolex.config.admin.Updater#addGroup(java.lang.String)
	 */
	@Override
	public String addGroup(String groupName) throws Exception {
		PacketData p = new PacketData(CodeMap.ADMIN_ADD_GROUP, groupName);
		WaitOn<String> on = waiter.init(groupName);
		client.handleWrite(p);
		return on.waitForResult(waitForTimeout);
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.config.admin.Updater#refreshGroup(java.lang.String)
	 */
	@Override
	public String refreshGroup(String groupName) throws Exception {
		PacketData p = new PacketData(CodeMap.ADMIN_REFRESH_GROUP, groupName);
		WaitOn<String> on = waiter.init(groupName);
		client.handleWrite(p);
		return on.waitForResult(waitForTimeout);
	}

	/**
	 * Get the config group with this specified group name.
	 *
	 * @param groupName the config group name
	 * @return the message from server.
	 * @throws Exception if necessary
	 */
	public String getGroup(String groupName) throws Exception {
	    WaitOn<String> on = waiter.init(groupName);
        // Send packet to remote server.
        PacketData sub = new PacketData(CodeMap.GROUP_SUB, StringUtil.strToUtf8Byte(groupName));
        client.handleWrite(sub);
        String s = on.waitForResult(waitForTimeout);
        return s;
	}

	/**
	 * Override super method
	 * @throws Exception if necessary
	 * @see org.apache.niolex.config.admin.Updater#addItem(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public String addItem(String groupName, String key, String value) throws Exception {
	    String s = getGroup(groupName);
	    if (s.length() > 0) {
            return s;
        }
    	if (storage.get(groupName).getGroupData().get(key) != null) {
    		return "The key you want to add already exist.";
    	}
    	int groupId = storage.get(groupName).getGroupId();
    	ConfigItem item = new ConfigItem();
    	item.setGroupId(groupId);
    	item.setKey(key);
    	item.setValue(value);
    	PacketData p = PacketTranslater.translate(item);
    	p.setCode(CodeMap.ADMIN_ADD_CONFIG);
    	WaitOn<String> on = waiter.init(key);
    	client.handleWrite(p);
		return on.waitForResult(waitForTimeout);
	}

	/**
	 * Override super method
	 * @throws Exception if necessary
	 * @see org.apache.niolex.config.admin.Updater#updateItem(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public String updateItem(String groupName, String key, String value) throws Exception {
	    String s = getGroup(groupName);
    	if (s.length() > 0) {
    		return s;
    	}
    	ConfigItem item = storage.get(groupName).getGroupData().get(key);
    	if (item == null) {
    		return "The key you want to update do not exist.";
    	}
    	item.setValue(value);
    	PacketData p = PacketTranslater.translate(item);
    	p.setCode(CodeMap.ADMIN_UPDATE_CONFIG);
    	WaitOn<String> on = waiter.init(key);
    	client.handleWrite(p);
		return on.waitForResult(waitForTimeout);
	}

	/**
	 * Override super method
	 * @throws Exception if necessary
	 * @see org.apache.niolex.config.admin.Updater#getItem(java.lang.String, java.lang.String)
	 */
	@Override
	public String getItem(String groupName, String key) throws Exception {
		ConfigGroup config = storage.get(groupName);
		ConfigItem item = null;
		if (config != null) {
			item = config.getGroupData().get(key);
		}
    	if (item == null) {
			WaitOn<String> on = waiter.init(groupName);
	    	// Send packet to remote server.
	    	PacketData sub = new PacketData(CodeMap.GROUP_SUB, StringUtil.strToUtf8Byte(groupName));
	    	client.handleWrite(sub);
	    	String s = on.waitForResult(waitForTimeout);
	    	if (s.length() > 0) {
	    		return s;
	    	}
	    	item = storage.get(groupName).getGroupData().get(key);
	    	if (item == null) {
	    		return "The key you want to get do not exist.";
	    	}
    	}
    	return "The item: " + item;
	}

	/**
	 * Override super method
	 * @throws Exception if necessary
	 * @see org.apache.niolex.config.admin.Updater#addUser(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public String addUser(String username, String password, String userRole) throws Exception {
		UserInfo info = new UserInfo();
		info.setUserName(username);
		info.setPassword(password);
		info.setUserRole(userRole);
		PacketData p = PacketTranslater.translate(info);
		WaitOn<String> on = waiter.init("adduser");
		client.handleWrite(p);
		return on.waitForResult(waitForTimeout);
	}

	@Override
    public String getUser(String username) throws Exception {
	    WaitOn<String> on = waiter.init("getuser");
	    // Send packet to remote server.
        PacketData sub = new PacketData(CodeMap.ADMIN_QUERY_USER, StringUtil.strToUtf8Byte(username));
        client.handleWrite(sub);
        return on.waitForResult(waitForTimeout);
    }


    /**
	 * Override super method
	 * @see org.apache.niolex.config.admin.Updater#changePassword(java.lang.String, java.lang.String)
	 */
	@Override
	public String changePassword(String username, String password) throws Exception {
		UserInfo info = new UserInfo();
		info.setUserName(username);
		info.setPassword(password);
		PacketData p = PacketTranslater.translate(info);
		p.setCode(CodeMap.ADMIN_UPDATE_USER);
		WaitOn<String> on = waiter.init("updateuser");
		client.handleWrite(p);
		return on.waitForResult(waitForTimeout);
	}

	/**
	 * Override super method
	 * @throws Exception if necessary
	 * @see org.apache.niolex.config.admin.Updater#updateUser(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public String updateUser(String username, String password, String userRole) throws Exception {
		UserInfo info = new UserInfo();
		info.setUserName(username);
		info.setPassword(password);
		info.setUserRole(userRole);
		PacketData p = PacketTranslater.translate(info);
		p.setCode(CodeMap.ADMIN_UPDATE_USER);
		WaitOn<String> on = waiter.init("updateuser");
		client.handleWrite(p);
		return on.waitForResult(waitForTimeout);
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.IPacketHandler#handlePacket(org.apache.niolex.network.PacketData, org.apache.niolex.network.IPacketWriter)
	 */
	@Override
	public void handlePacket(PacketData sc, IPacketWriter wt) {
		switch(sc.getCode()) {
		case CodeMap.RES_ADD_GROUP:
			// Notify anyone waiting for this.
			String[] s = StringUtil.utf8ByteToStr(sc.getData()).split(",");
			waiter.release(s[0], s[1]);
			break;
		case CodeMap.RES_REFRESH_GROUP:
			// Notify anyone waiting for this.
			s = StringUtil.utf8ByteToStr(sc.getData()).split(",");
			waiter.release(s[0], s[1]);
			break;
		case CodeMap.RES_ADD_ITEM:
			// Notify anyone waiting for this.
			s = StringUtil.utf8ByteToStr(sc.getData()).split(",");
			waiter.release(s[0], s[1]);
			break;
		case CodeMap.RES_UPDATE_ITEM:
			// Notify anyone waiting for this.
			s = StringUtil.utf8ByteToStr(sc.getData()).split(",");
			waiter.release(s[0], s[1]);
			break;
		case CodeMap.RES_ADD_USER:
			// Notify anyone waiting for this.
			String str = StringUtil.utf8ByteToStr(sc.getData());
			waiter.release("adduser", str);
			break;
		case CodeMap.RES_QUERY_USER:
		    // Notify anyone waiting for this.
		    str = StringUtil.utf8ByteToStr(sc.getData());
		    waiter.release("getuser", str);
		    break;
		case CodeMap.RES_UPDATE_USER:
			// Notify anyone waiting for this.
			str = StringUtil.utf8ByteToStr(sc.getData());
			waiter.release("updateuser", str);
			break;
		case CodeMap.RES_ADD_AUTH:
			// Notify anyone waiting for this.
			str = StringUtil.utf8ByteToStr(sc.getData());
			waiter.release("addAuth", str);
			break;
		case CodeMap.RES_REMOVE_AUTH:
			// Notify anyone waiting for this.
			str = StringUtil.utf8ByteToStr(sc.getData());
			waiter.release("removeAuth", str);
			break;
		case CodeMap.GROUP_DAT:
			// When group config data arrived, store it into memory storage.
			ConfigGroup conf = PacketTranslater.toConfigGroup(sc);
			storage.store(conf);

			// Notify anyone waiting for this.
			waiter.release(conf.getGroupName(), "");
			break;
		case CodeMap.GROUP_DIF:
			ConfigItem item = PacketTranslater.toConfigItem(sc);
			// Store this item into memory storage.
			String groupName = storage.findGroupName(item.getGroupId());
			storage.updateConfigItem(groupName, item);
			break;
		case CodeMap.GROUP_NOA:
			groupName = StringUtil.utf8ByteToStr(sc.getData());
			// Notify anyone waiting for this.
			waiter.release(groupName, "You are not authorised to read this group.");
			break;
		case CodeMap.GROUP_NOF:
			groupName = StringUtil.utf8ByteToStr(sc.getData());
			// Notify anyone waiting for this.
			waiter.release(groupName, "Group not found.");
			break;
		case CodeMap.AUTH_SUCC:
		    waiter.release("auth", "SUCC");
		    break;
		case CodeMap.AUTH_FAIL:
			client.stop();
			waiter.release("auth", "User Name or Password invalid.");
			break;
		}
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.IPacketHandler#handleClose(org.apache.niolex.network.IPacketWriter)
	 */
	@Override
	public void handleClose(IPacketWriter wt) {
		System.out.println("Connection to server is broken.");
		System.exit(-1);
	}


	/**
	 * Override super method
	 * @see org.apache.niolex.config.admin.Updater#addAuth(java.lang.String, java.lang.String)
	 */
	@Override
	public String addAuth(String username, String groupName) throws Exception {
		PacketData p = new PacketData(CodeMap.ADMIN_ADD_AUTH,
				StringUtil.concat(",", username, groupName));
		WaitOn<String> on = waiter.init("addAuth");
		client.handleWrite(p);
		return on.waitForResult(waitForTimeout);
	}


	/**
	 * Override super method
	 * @see org.apache.niolex.config.admin.Updater#removeAuth(java.lang.String, java.lang.String)
	 */
	@Override
	public String removeAuth(String username, String groupName) throws Exception {
		PacketData p = new PacketData(CodeMap.ADMIN_REMOVE_AUTH,
				StringUtil.concat(",", username, groupName));
		WaitOn<String> on = waiter.init("removeAuth");
		client.handleWrite(p);
		return on.waitForResult(waitForTimeout);
	}


    /**
     * Override super method
     * @see org.apache.niolex.config.admin.Updater#stop()
     */
    @Override
    public void stop() {
        client.stop();
        System.out.println("Client stoped. Please exit.");
    }

}
