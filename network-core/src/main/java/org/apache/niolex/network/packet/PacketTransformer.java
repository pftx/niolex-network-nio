/**
 * PacketTransformer.java
 *
 * Copyright 2011 Niolex, Inc.
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
package org.apache.niolex.network.packet;

import java.util.HashMap;
import java.util.Map;

import org.apache.niolex.network.PacketData;

/**
 * Transform packet into object according to packet code.
 *
 * The packet code is a 2-bytes short integer.
 * The system will use some this code, according to the following map:
 * CODE		USAGE
 * 0		Heart Beat
 * 1-65500	User Range
 * 65500-~	System Reserved
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-5-30
 */
public class PacketTransformer {
	private static final PacketTransformer INSTANCE = new PacketTransformer();

	private final Map<Short, ISerializer<?>> serMap = new HashMap<Short, ISerializer<?>>();


	/**
	 * The private constructor, prevent user from instantiate it.
	 */
	private PacketTransformer() {
		super();
	}

	/**
	 * Get the global instance of this class.
	 *
	 * @return the instance
	 */
	public static PacketTransformer getInstance() {
		return INSTANCE;
	}

	/**
	 * Add a Serializer to the PacketTransformer.
	 * @param ser
	 */
	public void addSerializer(ISerializer<?> ser) {
		serMap.put(ser.getCode(), ser);
	}

	/**
	 * Whether this code can be handled.
	 *
	 * @param code
	 * @return true if can handle, false otherwise
	 */
	public boolean canHandle(Short code) {
		return serMap.containsKey(code);
	}

	/**
	 * Translate PacketData to Object.
	 *
	 * @param sc
	 * @return the result
	 * @throws IllegalStateException if we can not translate this packet
	 */
	@SuppressWarnings("unchecked")
	public <T> T getDataObject(PacketData sc) {
		ISerializer<?> ser = serMap.get(sc.getCode());
		if (ser == null) {
			throw new IllegalStateException("No Serializer found for Packet Code " + sc.getCode());
		}
		return (T) ser.data2Obj(sc);
	}

	/**
	 * Translate Object to PacketData.
	 *
	 * @param code
	 * @param o
	 * @return the result
	 * @throws IllegalStateException if we can not translate this packet
	 */
	public PacketData getPacketData(Short code, Object o) {
		ISerializer<?> ser = serMap.get(code);
		if (ser == null) {
			throw new IllegalStateException("No Serializer found for Packet Code " + code);
		}
		return ser.obj2Data(code, o);
	}
}
