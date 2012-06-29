/**
 * NameClient.java
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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

import org.apache.niolex.network.Config;
import org.apache.niolex.network.IClient;
import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.client.PacketClient;
import org.apache.niolex.network.name.bean.AddressListSerializer;
import org.apache.niolex.network.name.bean.AddressRecord;
import org.apache.niolex.network.name.bean.AddressRecordSerializer;
import org.apache.niolex.network.name.bean.AddressRegiSerializer;
import org.apache.niolex.network.packet.PacketTransformer;
import org.apache.niolex.network.packet.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-20
 */
public class NameClient implements IPacketHandler {
	private static final Logger LOG = LoggerFactory.getLogger(NameClient.class);

	/**
	 * The real client implementation.
	 */
	protected final IClient client;

	/**
	 * The time to sleep between retry.
	 */
	private int sleepBetweenRetryTime = Config.SERVER_HEARTBEAT_INTERVAL;

	/**
	 * Times to retry get connected.
	 */
	private int connectRetryTimes = Integer.MAX_VALUE;

	/**
	 * Transform packets.
	 */
	protected final PacketTransformer transformer;


	/**
	 * Constructor.
	 *
	 * @param serverAddress
	 * @throws IOException
	 */
	public NameClient(String serverAddress) throws IOException {
		super();
		String[] addrs = serverAddress.split(":");
		client = new PacketClient(new InetSocketAddress(addrs[0], Integer.parseInt(addrs[1])));
		client.setPacketHandler(this);
		client.connect();
		client.handleWrite(new PacketData(Config.CODE_REGR_HBEAT));
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
	 * Override super method
	 * @see org.apache.niolex.network.IPacketHandler#handleRead(org.apache.niolex.network.PacketData, org.apache.niolex.network.IPacketWriter)
	 */
	@Override
	public void handleRead(PacketData sc, IPacketWriter wt) {
		// Dispatch package
		switch(sc.getCode()) {
			// 发布服务地址信息全量
			case Config.CODE_NAME_DATA:
				List<String> list = transformer.getDataObject(sc);
				handleRefresh(list);
				break;
			// 发布服务地址信息增量
			case Config.CODE_NAME_DIFF:
				AddressRecord bean = transformer.getDataObject(sc);
				// 由子类去处理增量
				handleDiff(bean);
				break;
			default:
				LOG.warn("Packet received for code [{}] have no handler, just ignored.", sc.getCode());
				break;
		}
	}

	/**
	 * 由子类去处理增量，这里什么都不做
	 * @param bean
	 */
	protected void handleDiff(AddressRecord bean) {}

	/**
	 * 由子类去处理全量返回的地址列表，这里什么都不做
	 * @param list
	 */
	protected void handleRefresh(List<String> list) {}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.IPacketHandler#handleClose(org.apache.niolex.network.IPacketWriter)
	 */
	@Override
	public void handleClose(IPacketWriter wt) {
		// We will retry to connect in this method.
		client.stop();
		if (!retryConnect()) {
			LOG.error("Exception occured when try to re-connect to server.");
			// Try to shutdown this Client, inform all the threads.
		} else {
			client.handleWrite(new PacketData(Config.CODE_REGR_HBEAT));
			reconnected();
		}
	}

	/**
	 * Stop this client.
	 */
	public void stop() {
		client.stop();
	}

	/**
	 * 由子类去处理重连之后的修复，这里什么都不做
	 */
	protected void reconnected() {}

	private boolean retryConnect() {
		for (int i = 0; i < connectRetryTimes; ++i) {
			try {
				Thread.sleep(sleepBetweenRetryTime);
			} catch (InterruptedException e1) {
				// It's OK.
			}
			LOG.info("RPC Client try to reconnect to server round {} ...", i);
			try {
				client.connect();
				return true;
			} catch (IOException e) {
				// Not connected.
				LOG.info("Try to re-connect to server failed. {}", e.toString());
			}
		}
		return false;
	}

	public void setSleepBetweenRetryTime(int sleepBetweenRetryTime) {
		this.sleepBetweenRetryTime = sleepBetweenRetryTime;
	}

	public void setConnectRetryTimes(int connectRetryTimes) {
		this.connectRetryTimes = connectRetryTimes;
	}

}
