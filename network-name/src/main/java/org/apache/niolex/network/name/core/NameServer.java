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

import org.apache.niolex.network.Config;
import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.IServer;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.adapter.HeartBeatAdapter;
import org.apache.niolex.network.name.bean.AddressListSerializer;
import org.apache.niolex.network.name.bean.AddressRecord;
import org.apache.niolex.network.name.bean.AddressRecord.Status;
import org.apache.niolex.network.name.bean.AddressRecordSerializer;
import org.apache.niolex.network.name.bean.AddressRegiBean;
import org.apache.niolex.network.name.bean.AddressRegiSerializer;
import org.apache.niolex.network.name.bean.RecordStorage;
import org.apache.niolex.network.name.event.IDispatcher;
import org.apache.niolex.network.packet.PacketTransformer;
import org.apache.niolex.network.packet.StringSerializer;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-21
 */
public class NameServer implements IPacketHandler {

	/**
	 * The real server implementation.
	 */
	private final IServer server;

	private final HeartBeatAdapter ada;

	private final PacketTransformer transformer;

	private RecordStorage storage;

	private IDispatcher dispatcher;

	public NameServer(IServer server) {
		super();
		this.server = server;
		ada = new HeartBeatAdapter(this);
		this.server.setPacketHandler(ada);
		transformer = PacketTransformer.getInstance();
		// 获取地址信息只传一个字符串，表达服务的key
		transformer.addSerializer(new StringSerializer(Config.CODE_NAME_OBTAIN));
		// 反向传输整个地址列表，表达地址
		transformer.addSerializer(new AddressListSerializer(Config.CODE_NAME_DATA));
		// 传输增量
		transformer.addSerializer(new AddressRecordSerializer(Config.CODE_NAME_DIFF));
		// 注册服务
		transformer.addSerializer(new AddressRegiSerializer(Config.CODE_NAME_PUBLISH));
	}

	/**
	 * Start the Server, bind to the Port. Server need to start threads internally to run. This method need to return
	 * after this server is started.
	 */
	public boolean start() {
		if (server.start()) {
			ada.start();
		}
		return false;
	}

	/**
	 * Stop this server. After stop, the internal threads need to be stopped.
	 */
	public void stop() {
		server.stop();
		ada.stop();
	}

	/**
	 * Override super method
	 *
	 * @see org.apache.niolex.network.IPacketHandler#handleRead(org.apache.niolex.network.PacketData,
	 *      org.apache.niolex.network.IPacketWriter)
	 */
	@Override
	public void handleRead(PacketData sc, IPacketWriter wt) {
		switch (sc.getCode()) {
			// 获取服务地址信息
			case Config.CODE_NAME_OBTAIN:
				String addressKey = transformer.getDataObject(sc);
				// 监听后续地址的变化
				dispatcher.register(addressKey, wt);
				// 将AddressKey附加到IPacketWriter里面，供后续使用
				attachData(wt, addressKey);
				// Return list
				List<String> list = storage.getAddress(addressKey);
				PacketData rc = transformer.getPacketData(Config.CODE_NAME_DATA, list);
				wt.handleWrite(rc);
				break;
			// 发布服务地址信息
			case Config.CODE_NAME_PUBLISH:
				AddressRegiBean bean = transformer.getDataObject(sc);
				AddressRecord rec = storage.store(bean);
				// 将AddressRecord附加到IPacketWriter里面，供后续使用
				attachData(wt, rec);
				//Fire event
				dispatcher.fireEvent(rec);
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
		}
		List<String> addrList = wt.getAttached(Config.ATTACH_KEY_OBTAIN_ADDR);
		if (addrList != null) {
			// 将监听器移除掉
			for (String addressKey : addrList) {
				dispatcher.handleClose(addressKey, wt);
			}
		}
	}

	public void setStorage(RecordStorage storage) {
		this.storage = storage;
	}

	public void setDispatcher(IDispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}

}
