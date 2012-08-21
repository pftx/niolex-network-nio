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
package org.apache.niolex.rpc.proto;

import java.lang.reflect.Type;

import org.apache.niolex.commons.seri.ProtoUtil;
import org.apache.niolex.rpc.core.ClientProtocol;

/**
 * Using Google Protocol Buffer to serialize data.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-5
 */
public class ProtobufProtocol implements ClientProtocol {

	/**
	 * Override super method
	 * @see org.apache.niolex.network.rpc.RpcClient#serializeParams(java.lang.Object[])
	 */
	@Override
	public byte[] serializeParams(Object[] args) throws Exception {
		return ProtoUtil.seriMulti(args);
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.rpc.RpcClient#prepareReturn(byte[], java.lang.reflect.Type, int)
	 */
	@Override
	public Object prepareReturn(byte[] ret, Type type) throws Exception {
		return ProtoUtil.parseOne(ret, type);
	}

}