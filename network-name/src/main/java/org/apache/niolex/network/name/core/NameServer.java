/**
 * NameServer.java
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
package org.apache.niolex.network.name.core;

import java.util.ArrayList;
import java.util.List;

import org.apache.niolex.commons.event.BaseEvent;
import org.apache.niolex.commons.event.ConcurrentEventDispatcher;
import org.apache.niolex.commons.event.Dispatcher;
import org.apache.niolex.network.Config;
import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.IServer;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.adapter.HeartBeatAdapter;
import org.apache.niolex.network.name.bean.AddressRecord;
import org.apache.niolex.network.name.bean.AddressRecord.Status;
import org.apache.niolex.network.name.bean.AddressRegiBean;
import org.apache.niolex.network.name.bean.RecordStorage;
import org.apache.niolex.network.name.event.WriteEventListener;
import org.apache.niolex.network.serialize.PacketTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The name server wrap a network server and provide name service.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-6-21
 */
public class NameServer implements IPacketHandler {

	private static final Logger LOG = LoggerFactory.getLogger(NameServer.class);

	/**
	 * Used to manage all the events.
	 */
	private final Dispatcher dispatcher = new ConcurrentEventDispatcher();

	/**
	 * Used to transform packets from and to java beans.
	 */
	private final PacketTransformer transformer = Context.getTransformer();

	/**
	 * Store all the address records.
	 */
	private final RecordStorage storage = new RecordStorage(dispatcher, 2 * Config.RPC_HANDLE_TIMEOUT);

	/**
	 * The real server implementation.
	 */
	private final IServer server;

	private final HeartBeatAdapter ada;


	/**
	 * Create a name server.
	 *
	 * @param server the network server
	 */
	public NameServer(IServer server) {
		super();
		this.server = server;
		ada = new HeartBeatAdapter(this);
		this.server.setPacketHandler(ada);
	}

	/**
	 * Start the Server, bind to the Port. Server need to start threads internally to run.
	 * This method need to return after this server is started.
	 */
	public boolean start() {
		if (server.start()) {
			ada.start();
			storage.start();
			return true;
		}
		return false;
	}

	/**
	 * Stop this server. After stop, the internal threads need to be stopped.
	 */
	public void stop() {
		server.stop();
		ada.stop();
		storage.stopMe();
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.IPacketHandler#handlePacket(PacketData, IPacketWriter)
	 */
	@Override
	public void handlePacket(PacketData sc, IPacketWriter wt) {
		switch (sc.getCode()) {
			// 获取服务地址信息
			case Config.CODE_NAME_OBTAIN:
				String addressKey = transformer.getDataObject(sc);
				// 监听后续地址的变化
				dispatcher.addListener(addressKey, WriteEventListener.obtain(wt));
				// 将AddressKey附加到IPacketWriter里面，供后续使用
				attachData(wt, addressKey);
				// Return list
				List<String> list = storage.getAddress(addressKey);
				list.add(addressKey);
				PacketData rc = transformer.getPacketData(Config.CODE_NAME_DATA, list);
				wt.handleWrite(rc);
				LOG.info("Client {} try to subscribe address {}.", wt.getRemoteName(), addressKey);
				break;
			// 发布服务地址信息
			case Config.CODE_NAME_PUBLISH:
				AddressRegiBean bean = transformer.getDataObject(sc);
				AddressRecord rec = storage.store(bean);
				// 将AddressRecord附加到IPacketWriter里面，供后续使用
				attachData(wt, rec);
				//Fire event
				PacketData sent = transformer.getPacketData(Config.CODE_NAME_DIFF, rec);
				dispatcher.fireEvent(new BaseEvent<PacketData>(rec.getAddressKey(), sent));
				LOG.info("Client {} try to publish address {}.", wt.getRemoteName(), rec.toString());
				break;
			default:
				wt.handleWrite(new PacketData(Config.CODE_NOT_RECOGNIZED));
				break;
		}
	}

	private void attachData(IPacketWriter wt, String addressKey) {
		List<String> addrList = wt.getAttached(Config.ATTACH_KEY_OBTAIN_ADDR);
		if (addrList == null) {
			addrList = new ArrayList<String>();
			wt.attachData(Config.ATTACH_KEY_OBTAIN_ADDR, addrList);
		}
		addrList.add(addressKey);
	}

	private void attachData(IPacketWriter wt, AddressRecord rec) {
		List<AddressRecord> recList = wt.getAttached(Config.ATTACH_KEY_REGIST_ADDR);
		if (recList == null) {
			recList = new ArrayList<AddressRecord>();
			wt.attachData(Config.ATTACH_KEY_REGIST_ADDR, recList);
		}
		recList.add(rec);
	}

	/**
	 * Override super method
	 *
	 * @see org.apache.niolex.network.IPacketHandler#handleClose(org.apache.niolex.network.IPacketWriter)
	 */
	@Override
	public void handleClose(IPacketWriter wt) {
		List<AddressRecord> recList = wt.getAttached(Config.ATTACH_KEY_REGIST_ADDR);
		if (recList != null) {
			// 将服务地址标记为已断线
			for (AddressRecord rec : recList) {
				rec.setStatus(Status.DISCONNECTED);
			}
			wt.attachData(Config.ATTACH_KEY_REGIST_ADDR, null);
		}
		List<String> addrList = wt.getAttached(Config.ATTACH_KEY_OBTAIN_ADDR);
		if (addrList != null) {
		    WriteEventListener tmp = WriteEventListener.obtain(wt);
			// 将监听器移除掉
			for (String addressKey : addrList) {
				dispatcher.removeListener(addressKey, tmp);
			}
			wt.attachData(Config.ATTACH_KEY_OBTAIN_ADDR, null);
		}
	}

    /**
     * @param deleteTime the time to wait after server disconnected
     * @see org.apache.niolex.network.name.bean.RecordStorage#setDeleteTime(int)
     */
    public void setDeleteTime(int deleteTime) {
        storage.setDeleteTime(deleteTime);
    }

}
