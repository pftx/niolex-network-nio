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
 * User can extend this class to implement their own serializer.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-5-30
 */
public abstract class BaseSerializer<T> implements ISerializer {

    private final Class<T> clazz;
    private short code;

    /**
     * The Constructor.
     *
     * @param clazz the class this Serializer is handling
     * @param code the packet code this Serializer is handling
     */
    public BaseSerializer(Class<T> clazz, short code) {
        super();
        this.clazz = clazz;
        this.code = code;
    }

    /**
     * This is the override of super method.
     * @see org.apache.niolex.network.serialize.ISerializer#obj2Bytes(java.lang.Object)
     */
    @Override
    public byte[] obj2Bytes(Object o) {
        return serialize(clazz.cast(o));
    }

    /**
	 * serialize object to byte array.
	 *
	 * @param t the object
	 * @return the result
	 */
	public abstract byte[] serialize(T t);

    /**
     * This is the override of super method.
     * @see org.apache.niolex.network.serialize.ISerializer#getCode()
     */
    @Override
    public short getCode() {
        return code;
    }

    /**
     * @param code the code to set
     */
    public void setCode(short code) {
        this.code = code;
    }

}
