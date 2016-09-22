/**
 * ProtobufProtocol.java
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

import java.io.IOException;
import java.lang.reflect.Type;

import org.apache.niolex.commons.seri.ProtobufUtil;
import org.apache.niolex.commons.seri.SeriUtil;
import org.apache.niolex.rpc.RpcException;
import org.apache.niolex.rpc.util.RpcUtil;

import com.google.protobuf.GeneratedMessage;

/**
 * Using Google Protocol Buffer to serialize data.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-6-5
 */
public class ProtobufProtocol implements IClientProtocol, IServerProtocol {

	/**
	 * Override super method
	 * @see org.apache.niolex.rpc.protocol.IServerProtocol#prepareParams(byte[], java.lang.reflect.Type[])
	 */
	@Override
    public Object[] prepareParams(byte[] data, Type[] generic) throws IOException {
	    return ProtobufUtil.parseMulti(data, SeriUtil.castJavaTypes(generic));
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.rpc.protocol.IServerProtocol#serializeReturn(java.lang.Object)
	 */
	@Override
    public byte[] serializeReturn(Object ret) throws IOException {
		if (ret instanceof GeneratedMessage) {
			GeneratedMessage gen = (GeneratedMessage) ret;
			return gen.toByteArray();
		} else if (ret instanceof RpcException) {
			return RpcUtil.serializeRpcException((RpcException) ret);
		} else {
			throw new RpcException("Message is not protobuf type: " + ret.getClass(),
					RpcException.Type.ERROR_SER_RETURN, null);
		}
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.rpc.protocol.IClientProtocol#serializeParams(java.lang.Object[])
	 */
	@Override
    public byte[] serializeParams(Object[] args) throws IOException {
		return ProtobufUtil.seriMulti(args);
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.rpc.protocol.IClientProtocol#prepareReturn(byte[], java.lang.reflect.Type)
	 */
    @Override
    public Object prepareReturn(byte[] ret, Type type) throws IOException {
		if (type.equals(RpcException.class)) {
			return RpcUtil.parseRpcException(ret);
		} else {
		    return ProtobufUtil.parseOne(ret, SeriUtil.castJavaType(type));
		}
	}

}
