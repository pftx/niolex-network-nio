/**
 * JsonProtocol.java
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
package org.apache.niolex.rpc.protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;

import org.apache.niolex.commons.compress.JacksonUtil;
import org.apache.niolex.network.Config;
import org.codehaus.jackson.map.type.TypeFactory;

/**
 * The Json Rpc Protocol. We implement both side protocol just in one class.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-6-2
 */
public class JsonProtocol implements IClientProtocol, IServerProtocol {

	/**
	 * This is the override of super method.
	 * @see org.apache.niolex.rpc.protocol.IClientProtocol#serializeParams(java.lang.Object[])
	 */
	@Override
	public byte[] serializeParams(Object[] args) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		for (Object o : args) {
			JacksonUtil.writeObj(out, o);
		}
		return out.toByteArray();
	}

	/**
	 * This is the override of super method.
	 * @see org.apache.niolex.rpc.protocol.IClientProtocol#prepareReturn(byte[], java.lang.reflect.Type)
	 */
	@SuppressWarnings("deprecation")
	@Override
	public Object prepareReturn(byte[] ret, Type type) throws Exception {
		ByteArrayInputStream in = new ByteArrayInputStream(ret);
		Object r = JacksonUtil.readObj(in, TypeFactory.type(type));
		return r;
	}

	/**
	 * This is the override of super method.
	 * @see org.apache.niolex.rpc.protocol.IServerProtocol#prepareParams(byte[], java.lang.reflect.Type[])
	 */
	@Override
	public Object[] prepareParams(byte[] data, Type[] generic) throws Exception {
		return JsonUtil.prepareParams(data, generic);
	}

	/**
	 * This is the override of super method.
	 * @see org.apache.niolex.rpc.protocol.IServerProtocol#serializeReturn(java.lang.Object)
	 */
	@Override
	public byte[] serializeReturn(Object ret) throws Exception {
		return JacksonUtil.obj2Str(ret).getBytes(Config.SERVER_ENCODING);
	}

}
