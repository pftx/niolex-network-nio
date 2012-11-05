/**
 * JsonRpcPacketHandler.java
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
package org.apache.niolex.network.rpc.json;

import java.lang.reflect.Type;

import org.apache.niolex.commons.compress.JacksonUtil;
import org.apache.niolex.network.Config;
import org.apache.niolex.network.rpc.RpcPacketHandler;
import org.apache.niolex.network.rpc.RpcUtil;

/**
 * Use Json to serialize objects.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-1
 */
public class JsonRpcPacketHandler extends RpcPacketHandler {


	/**
	 * Override super Constructor
	 */
	public JsonRpcPacketHandler() {
		super();
	}

	public JsonRpcPacketHandler(int threadsNumber) {
		super(threadsNumber);
	}

	/**
	 * This is the override of super method.
	 * @see org.apache.niolex.network.rpc.RpcPacketHandler#prepareParams(byte[], java.lang.reflect.Type[])
	 */
	@Override
	protected Object[] prepareParams(byte[] data, Type[] generic) throws Exception {
		return RpcUtil.prepareParams(data, generic);
	}

	/**
	 * This is the override of super method.
	 * @see org.apache.niolex.network.rpc.RpcPacketHandler#serializeReturn(java.lang.Object)
	 */
	@Override
	protected byte[] serializeReturn(Object ret) throws Exception {
		return JacksonUtil.obj2Str(ret).getBytes(Config.SERVER_ENCODING);
	}

}
