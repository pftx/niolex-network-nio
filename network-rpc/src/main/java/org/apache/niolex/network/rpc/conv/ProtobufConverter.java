/**
 * ProtobufConverter.java
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
package org.apache.niolex.network.rpc.conv;

import java.lang.reflect.Type;

import org.apache.niolex.commons.seri.ProtobufUtil;
import org.apache.niolex.commons.seri.SeriUtil;
import org.apache.niolex.network.rpc.IConverter;

/**
 * Using Google Protocol Buffer to serialize data.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0, Date: 2012-11-7
 */
public class ProtobufConverter implements IConverter {

	/**
	 * Override super method
	 * @see org.apache.niolex.network.rpc.IConverter#prepareParams(byte[], java.lang.reflect.Type[])
	 */
	@Override
	public Object[] prepareParams(byte[] data, Type[] generic) throws Exception {
		return ProtobufUtil.parseMulti(data, SeriUtil.castJavaTypes(generic));
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.rpc.IConverter#serializeParams(java.lang.Object[])
	 */
	@Override
	public byte[] serializeParams(Object[] args) throws Exception {
		return ProtobufUtil.seriMulti(args);
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.rpc.IConverter#prepareReturn(byte[], java.lang.reflect.Type)
	 */
    @Override
	public Object prepareReturn(byte[] ret, Type type) throws Exception {
	    return ProtobufUtil.parseOne(ret, SeriUtil.castJavaType(type));
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.rpc.IConverter#serializeReturn(java.lang.Object)
	 */
	@Override
	public byte[] serializeReturn(Object ret) throws Exception {
	    return ProtobufUtil.seriOne(ret);
	}

}
