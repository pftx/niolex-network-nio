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
package org.apache.niolex.network.serialize;

import org.apache.niolex.network.PacketData;

/**
 * The base class of ISerializer, deal with {@link PacketData} and dirty object cast.
 * User can extend this class to implement their own serializer for convenience.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-5-30
 */
public abstract class BaseSerializer<T> implements ISerializer {

    private short code;

    /**
     * The Constructor.
     *
     * @param code the packet code this Serializer is handling
     */
    public BaseSerializer(short code) {
        super();
        this.code = code;
    }

    /**
     * This is the override of super method.
     * @see org.apache.niolex.network.serialize.ISerializer#obj2Bytes(java.lang.Object)
     */
    @Override
    @SuppressWarnings("unchecked")
    public byte[] obj2Bytes(Object o) {
        return toBytes((T) o);
    }

    /**
     * This is the override of super method.
     * @see org.apache.niolex.network.serialize.ISerializer#bytes2Obj(byte[])
     */
    @Override
    public Object bytes2Obj(byte[] array) {
        return toObj(array);
    }

    /**
	 * serialize object to byte array.
	 *
	 * @param t the object
	 * @return the result
	 */
	public abstract byte[] toBytes(T t);

	/**
	 * deserialize byte array to object.
	 *
	 * @param arr the byte array
	 * @return the object
	 */
	public abstract T toObj(byte[] arr);

    /**
     * This is the override of super method.
     * @see org.apache.niolex.network.serialize.ISerializer#getCode()
     */
    @Override
    public short getCode() {
        return code;
    }

    /**
     * @param code the packet code to set
     */
    public void setCode(short code) {
        this.code = code;
    }

}
