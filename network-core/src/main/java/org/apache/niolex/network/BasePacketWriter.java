/**
 * BasePacketWriter.java
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
package org.apache.niolex.network;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * The base BasePacketWriter, handle attach and PacketData storage.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-5-30
 */
public abstract class BasePacketWriter implements IPacketWriter {

	protected Map<String, Object> attachMap = new HashMap<String, Object>();
    protected List<PacketData> sendPacketList = Collections.synchronizedList(new LinkedList<PacketData>());

	@Override
	public void handleWrite(PacketData sc) {
		sendPacketList.add(sc);
	}

	@Override
	public Object attachData(String key, Object value) {
		return attachMap.put(key, value);
	}

	@Override
	public Object getAttached(String key) {
		return attachMap.get(key);
	}

}
