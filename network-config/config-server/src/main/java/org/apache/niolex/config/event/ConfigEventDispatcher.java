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

import org.apache.niolex.config.bean.ConfigItem;
import org.apache.niolex.config.core.PacketTranslater;
import org.apache.niolex.network.IPacketWriter;

/**
 * Dispatch config item changes.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-7-3
 */
public class ConfigEventDispatcher {


	/**
	 * Add an event listener who care this event.
	 * @param eListener
	 */
	public void addListener(String groupName, IPacketWriter listener) {

	}

	/**
	 * Remove the specified event listener.
	 * @param eListener
	 */
	public void removeListener(String groupName, IPacketWriter listener) {

	}

	/**
	 * Fire the specified event to all the listeners registered to this dispatcher.
	 * @param e
	 */
	public void fireEvent(ConfigItem item) {
		;
		//wt.handleWrite(PacketTranslater.translate(item));
	}

}
