/**
 * ISerializer.java
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
 * Handle the Packet serialization and de-serialization.
 * #PacketTransformer use this interface to manage the user serializer to do
 * data and object translation.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-5-30
 */
public interface ISerializer<T> {

	/**
	 * Get the code this Serializer is capable of.
	 *
	 * @return the code
	 */
	public short getCode();

	/**
	 * Translate object to byte array.
	 *
	 * @param code
	 * @param o
	 * @return the result
	 */
	public PacketData obj2Data(short code, Object o);

	/**
	 * Translate byte array to object.
	 *
	 * @param sc
	 * @return the result
	 */
	public T data2Obj(PacketData sc);

}
