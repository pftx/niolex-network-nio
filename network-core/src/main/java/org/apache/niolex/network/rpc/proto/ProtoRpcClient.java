/**
 * ProtoRpcClient.java
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
package org.apache.niolex.network.rpc.proto;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;

import org.apache.niolex.network.IClient;
import org.apache.niolex.network.rpc.RpcClient;
import org.apache.niolex.network.rpc.RpcException;
import org.apache.niolex.network.rpc.RpcInvoker;

import com.google.protobuf.GeneratedMessage;

/**
 * Using Google Protocol Buffer to serialize data.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-5
 */
public class ProtoRpcClient extends RpcClient {

	/**
	 * Implements super constructor
	 * @param client
	 * @param invoker
	 */
	public ProtoRpcClient(IClient client, RpcInvoker invoker) {
		super(client, invoker);
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.rpc.RpcClient#serializeParams(java.lang.Object[])
	 */
	@Override
	protected byte[] serializeParams(Object[] args) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		for (Object o : args) {
			if (o instanceof GeneratedMessage) {
				GeneratedMessage gen = (GeneratedMessage)o;
				gen.writeTo(out);
			} else {
				throw new RpcException("Message is not protobuf type: " + o.getClass(),
						RpcException.Type.ERROR_PARSE_PARAMS, null);
			}
		}
		return out.toByteArray();
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.rpc.RpcClient#prepareReturn(byte[], java.lang.reflect.Type, int)
	 */
	@Override
	protected Object prepareReturn(byte[] ret, Type type) throws Exception {
		return ProtoUtil.parseOne(ret, type);
	}

}
