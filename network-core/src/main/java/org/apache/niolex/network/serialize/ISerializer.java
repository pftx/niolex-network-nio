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
package org.apache.niolex.network.serialize;


/**
 * Handle the Packet serialization and deserialization.
 * {@link PacketTransformer} use this interface to manage the user serializer to do
 * byte array and object transformation.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-5-30
 */
public interface ISerializer {

	/**
	 * Get the packet code this Serializer is capable of.
	 *
	 * @return the packet code
	 */
	public short getCode();

	/**
	 * Translate object to byte array.
	 *
	 * @param o the object to be transformed
	 * @return the result
	 */
	public byte[] obj2Bytes(Object o);

	/**
	 * Translate byte array to object.
	 *
	 * @param array the packet data
	 * @return the result
	 */
	public Object bytes2Obj(byte[] array);

}
