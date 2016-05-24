/**
 * ConfigEventDispatcher.java
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
package org.apache.niolex.config.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.niolex.commons.concurrent.ConcurrentUtil;
import org.apache.niolex.config.bean.ConfigItem;
import org.apache.niolex.config.core.CodeMap;
import org.apache.niolex.config.core.PacketTranslater;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;
import org.springframework.stereotype.Component;

/**
 * Dispatch config item changes.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-7-3
 */
@Component
public class ConfigEventDispatcher {

	/**
	 * Store other servers.
	 */
	private final List<IPacketWriter> otherServers = Collections.synchronizedList(
			new ArrayList<IPacketWriter>(0));

	/**
	 * Store clients.
	 */
	private final ConcurrentHashMap<String, ConcurrentHashMap<IPacketWriter, String>> clients =
			new ConcurrentHashMap<String, ConcurrentHashMap<IPacketWriter, String>>();


	/**
	 * Add an event listener who care this event.
	 * 
	 * @param groupName the config group name
	 * @param listener the listener
	 */
	public void addListener(String groupName, IPacketWriter listener) {
		ConcurrentHashMap<IPacketWriter, String> map = clients.get(groupName);
		if (map == null) {
		    map = ConcurrentUtil.initMap(clients, groupName, new ConcurrentHashMap<IPacketWriter, String>());
		}
		map.put(listener, "");
	}

	/**
	 * Remove the specified event listener.
	 * 
	 * @param groupName the config group name
	 * @param listener the listener
	 */
	public void removeListener(String groupName, IPacketWriter listener) {
		ConcurrentHashMap<IPacketWriter, String> map = clients.get(groupName);
		if (map != null) {
			map.remove(listener);
		}
	}

	/**
	 * Add other server under control, which is interested to all events.
	 * 
	 * @param listener the listener
	 */
	public void addOtherServer(IPacketWriter listener) {
		otherServers.add(listener);
	}

	/**
	 * Remove other server under control.
	 * 
	 * @param listener the listener
	 */
	public void removeOtherServer(IPacketWriter listener) {
		otherServers.remove(listener);
	}

	/**
	 * Fire the specified event to all the listeners registered to this dispatcher.
	 * 
	 * @param groupName the config group name
	 * @param item the config item
	 */
	public void fireEvent(String groupName, ConfigItem item) {
		PacketData data = PacketTranslater.translate(item);
		// Fire to other servers.
		synchronized(otherServers) {
			for (IPacketWriter wt : otherServers) {
				wt.handleWrite(data);
			}
		}
		// Fire to all clients.
		fireClientEvent(groupName, data);
	}

	/**
	 * Fire the specified event to all the listeners registered to this dispatcher.
	 * 
	 * @param groupName the config group name
	 * @param item the config item
	 */
	public void fireClientEvent(String groupName, ConfigItem item) {
		fireClientEvent(groupName, PacketTranslater.translate(item));
	}

	/**
	 * Fire the specified event to all the listeners registered to this dispatcher.
	 * 
	 * @param groupName the config group name
	 * @param data the packet data
	 */
	public void fireClientEvent(String groupName, PacketData data) {
		ConcurrentHashMap<IPacketWriter, String> map = clients.get(groupName);
		if (map != null) {
			for (IPacketWriter wt : map.keySet()) {
				wt.handleWrite(data);
			}
		}
	}

	/**
	 * Fire event to other servers to indicate that a new config group is added.
	 * 
	 * @param groupName the config group name
	 */
	public void fireAddEvent(String groupName) {
		PacketData data = new PacketData(CodeMap.GROUP_ADD, groupName);
		// Fire to other servers.
		synchronized(otherServers) {
			for (IPacketWriter wt : otherServers) {
				wt.handleWrite(data);
			}
		}
	}

}
