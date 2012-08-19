/**
 * ProtoRpcPacketHandler.java
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
package org.apache.niolex.rpc.proto;

import java.lang.reflect.Type;

import org.apache.niolex.commons.seri.ProtoUtil;
import org.apache.niolex.rpc.RpcException;
import org.apache.niolex.rpc.RpcInvoker;

import com.google.protobuf.GeneratedMessage;

/**
 * Using Google Protocol Buffer to serialize data.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-5
 */
public class ProtoRpcPacketHandler extends RpcInvoker {

	/**
	 * Override super Constructor
	 */
	public ProtoRpcPacketHandler() {
		super();
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.rpc.RpcPacketHandler#prepareParams(byte[], java.lang.reflect.Type[])
	 */
	@Override
	protected Object[] prepareParams(byte[] data, Type[] generic) throws Exception {
		return ProtoUtil.parseMulti(data, generic);
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.rpc.RpcPacketHandler#serializeReturn(java.lang.Object)
	 */
	@Override
	protected byte[] serializeReturn(Object ret) throws Exception {
		if (ret instanceof GeneratedMessage) {
			GeneratedMessage gen = (GeneratedMessage) ret;
			return gen.toByteArray();
		} else if (ret instanceof Exception) {
			LOG.warn("ProtoBuffer can not support return Exception. We just log it here.", (Exception)ret);
			return new byte[0];
		} else {
			throw new RpcException("Message is not protobuf type: " + ret.getClass(),
					RpcException.Type.ERROR_PARSE_PARAMS, null);
		}
	}

}
