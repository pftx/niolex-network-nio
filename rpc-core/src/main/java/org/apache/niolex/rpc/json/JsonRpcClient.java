/**
 * JsonRpcClient.java
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
package org.apache.niolex.rpc.json;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;

import org.apache.niolex.commons.compress.JacksonUtil;
import org.apache.niolex.network.IClient;
import org.apache.niolex.rpc.RpcClient;
import org.codehaus.jackson.map.type.TypeFactory;

/**
 * The Json Rpc Client.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-2
 */
public class JsonRpcClient extends RpcClient {

	/**
	 * Implements super Constructor
	 * @param client
	 */
	public JsonRpcClient(IClient client) {
		super(client);
	}

	/**
	 * This is the override of super method.
	 * @see org.apache.niolex.network.rpc.RpcClient#serializeParams(java.lang.Object[])
	 */
	@Override
	protected byte[] serializeParams(Object[] args) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		for (Object o : args) {
			JacksonUtil.writeObj(out, o);
		}
		return out.toByteArray();
	}

	/**
	 * This is the override of super method.
	 * @see org.apache.niolex.network.rpc.RpcClient#prepareReturn(byte[], java.lang.reflect.Type, int)
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected Object prepareReturn(byte[] ret, Type type) throws Exception {
		ByteArrayInputStream in = new ByteArrayInputStream(ret);
		Object r = JacksonUtil.readObj(in, TypeFactory.type(type));
		return r;
	}

}
