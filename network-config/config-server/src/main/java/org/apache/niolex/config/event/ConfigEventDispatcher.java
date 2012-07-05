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
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.niolex.config.bean.ConfigItem;
import org.apache.niolex.config.core.PacketTranslater;
import org.apache.niolex.network.IPacketWriter;
import org.springframework.stereotype.Component;

/**
 * Dispatch config item changes.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-7-3
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
	private final ConcurrentHashMap<String, ConcurrentLinkedQueue<IPacketWriter>> clients =
			new ConcurrentHashMap<String, ConcurrentLinkedQueue<IPacketWriter>>();


	/**
	 * Add an event listener who care this event.
	 * @param eListener
	 */
	public void addListener(String groupName, IPacketWriter listener) {
		ConcurrentLinkedQueue<IPacketWriter> queue = clients.get(groupName);
		if (queue == null) {
			queue = new ConcurrentLinkedQueue<IPacketWriter>();
			ConcurrentLinkedQueue<IPacketWriter> tmp = clients.putIfAbsent(groupName, queue);
			if (tmp != null) {
				queue = tmp;
			}
		}
		queue.add(listener);
	}

	/**
	 * Remove the specified event listener.
	 * @param eListener
	 */
	public void removeListener(String groupName, IPacketWriter listener) {
		ConcurrentLinkedQueue<IPacketWriter> queue = clients.get(groupName);
		if (queue != null) {
			queue.remove(listener);
		}
	}

	/**
	 * Add other server under control, which is interested to all events.
	 * @param listener
	 */
	public void addOtherServer(IPacketWriter listener) {
		otherServers.add(listener);
	}

	/**
	 * Fire the specified event to all the listeners registered to this dispatcher.
	 * @param e
	 */
	public void fireEvent(String groupName, ConfigItem item) {
		// Fire to other servers.
		synchronized(otherServers) {
			for (IPacketWriter wt : otherServers) {
				wt.handleWrite(PacketTranslater.translate(item));
			}
		}
		// Fire to all clients.
		fireClientEvent(groupName, item);
	}

	/**
	 * Fire the specified event to all the listeners registered to this dispatcher.
	 * @param e
	 */
	public void fireClientEvent(String groupName, ConfigItem item) {
		ConcurrentLinkedQueue<IPacketWriter> queue = clients.get(groupName);
		if (queue != null) {
			synchronized(queue) {
				for (IPacketWriter wt : queue) {
					wt.handleWrite(PacketTranslater.translate(item));
				}
			}
		}
	}

}
