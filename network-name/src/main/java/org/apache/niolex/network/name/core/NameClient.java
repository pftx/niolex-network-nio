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

import java.util.ArrayList;
import java.util.List;

import org.apache.niolex.network.Config;
import org.apache.niolex.network.IClient;
import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.client.ClientManager;
import org.apache.niolex.network.client.PacketClient;
import org.apache.niolex.network.name.bean.AddressRecord;
import org.apache.niolex.network.serialize.PacketTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The name client wrap a real client and handle packets inside.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-6-20
 */
public class NameClient implements IPacketHandler {
	private static final Logger LOG = LoggerFactory.getLogger(NameClient.class);

	/**
     * Store all the requests, retry them after reconnection.
     */
    private final List<PacketData> internalPacketList = new ArrayList<PacketData>();

	/**
	 * Manage the client retry and fail over.
	 */
	private final ClientManager clientManager = new ClientManager(new PacketClient());

	/**
	 * Transform packets.
	 */
	protected final PacketTransformer transformer = Context.getTransformer();

	/**
     * Create a new name client and connect it to the server address.
     *
     * @param serverAddress the server address
     */
    public NameClient(String serverAddress) {
		super();
		clientManager.setAddressList(serverAddress);
		clientManager.setPacketHandler(this);
		clientManager.setConnectRetryTimes(Integer.MAX_VALUE);
		connect();
		clientManager.handleWrite(new PacketData(Config.CODE_REGR_HBEAT));
	}

	/**
     * Try to connect to one server.
     *
     * @return true if connected, false otherwise.
     */
    public boolean connect() {
        clientManager.connect();
        try {
            return clientManager.waitForConnected();
        } catch (Exception e) {
            LOG.error("Failed to connect to name server.", e);
            return false;
        }
    }

	/**
	 * Override super method
	 * @see org.apache.niolex.network.IPacketHandler#handlePacket(PacketData, IPacketWriter)
	 */
	@Override
	public void handlePacket(PacketData sc, IPacketWriter wt) {
        // Dispatch package according to packet code.
		switch(sc.getCode()) {
            // Server published address list.
			case Config.CODE_NAME_DATA:
				List<String> list = transformer.getDataObject(sc);
                // Let sub classes to deal with it.
				handleRefresh(list);
				break;
            // Server published new address record.
			case Config.CODE_NAME_DIFF:
				AddressRecord bean = transformer.getDataObject(sc);
                // Let sub classes to deal with it.
				handleDiff(bean);
				break;
			default:
				LOG.warn("Packet received for code [{}] have no handler, just ignored.", sc.getCode());
				break;
		}
	}

	/**
	 * @return the internal client
	 */
	protected IClient client() {
	    return clientManager.client();
	}

	/**
     * The new address record published. Let sub class to deal with it.
     * 
     * @param bean the address record
     */
	protected void handleDiff(AddressRecord bean) {}

	/**
     * Refresh the address list. Let sub class to deal with it.
     * 
     * @param list the address list
     */
	protected void handleRefresh(List<String> list) {}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.IPacketHandler#handleClose(IPacketWriter)
	 */
	@Override
	public void handleClose(IPacketWriter wt) {
		// We will retry to connect in this method.
		if (!clientManager.retryConnect()) {
			LOG.error("Exception occured when try to re-connect to server.");
            // TODO: Try to shutdown this Client, inform all the threads.
		} else {
			clientManager.handleWrite(new PacketData(Config.CODE_REGR_HBEAT));
			reconnected();
		}
	}

	/**
	 * Stop this client.
	 */
	public void stop() {
	    clientManager.close();
	}

	/**
	 * Re-send all the packets to server after re-connected.
	 */
	protected synchronized void reconnected() {
	    for (PacketData data : internalPacketList) {
            client().handleWrite(data);
        }
	}

	/**
     * Save the packet into internal list.
     *
     * @param sent the sent packet
     */
    protected synchronized void savePacket(PacketData sent) {
        internalPacketList.add(sent);
    }

	public void setSleepBetweenRetryTime(int sleepBetweenRetryTime) {
	    clientManager.setSleepBetweenRetryTime(sleepBetweenRetryTime);
	}

	public void setConnectRetryTimes(int connectRetryTimes) {
	    clientManager.setConnectRetryTimes(connectRetryTimes);
	}

	/**
	 * @return the internal packet list size
	 */
    public int internalPacketSize() {
        return internalPacketList.size();
    }

}
