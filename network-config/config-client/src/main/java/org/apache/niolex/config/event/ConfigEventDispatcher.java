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

import java.util.concurrent.ConcurrentHashMap;

import org.apache.niolex.config.bean.ConfigItem;

/**
 * This is the class to manage listeners listening to config changes.
 * KEEP IN MIND!!!!!
 *
 * One Listener Per Key!!!
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-7-3
 */
public class ConfigEventDispatcher {

	/**
	 * The total listener storage.
	 */
	private final ConcurrentHashMap<String, ConfigListener> mapStorage = new ConcurrentHashMap<String, ConfigListener>();

	/**
	 * Add an event listener who care this event.
	 * Attention! We can only manage one listener for one key.
	 *
	 * @param eListener
	 * @return the old listener. NULL if this is the only one listen to this key.
	 */
	public ConfigListener addListener(String key, ConfigListener listener) {
		return mapStorage.put(key, listener);
	}

	/**
	 * Remove the specified event listener.
	 * Attention! We will only remove if the current listener is the specified one.
	 *
	 * @param eListener
	 * @return whether the specified listener is removed.
	 */
	public boolean removeListener(String key, ConfigListener listener) {
		return mapStorage.remove(key, listener);
	}

	/**
	 * Fire the specified event to the listener registered to this dispatcher.
	 * @param e
	 */
	public void fireEvent(ConfigItem item) {
		ConfigListener listener = mapStorage.get(item.getKey());
		if (listener != null) {
			listener.configChanged(item.getValue(), item.getUpdateTime());
		}
	}

}
