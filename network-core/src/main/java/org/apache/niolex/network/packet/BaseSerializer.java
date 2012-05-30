/**
 * BaseSerializer.java
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

import org.apache.niolex.network.PacketData;

/**
 * The base class of ISerializer, deal with PacketData and dirty object cast.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-5-30
 */
public abstract class BaseSerializer<T> implements ISerializer<T> {

	@Override
	public PacketData obj2Data(short code, Object o) {
		@SuppressWarnings("unchecked")
		byte[] arr = serObj((T)o);
		return new PacketData(code, arr);
	}

	@Override
	public T data2Obj(PacketData sc) {
		return deserObj(sc.getData());
	}

	/**
	 * serialize object to byte array.
	 * @param t
	 * @return
	 */
	public abstract byte[] serObj(T t);

	/**
	 * deserialize byte array to object.
	 * @param arr
	 * @return
	 */
	public abstract T deserObj(byte[] arr);
}
