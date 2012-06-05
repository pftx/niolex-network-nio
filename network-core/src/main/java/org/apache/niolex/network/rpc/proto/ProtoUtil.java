/**
 * ProtoUtil.java
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

import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.apache.niolex.commons.reflect.MethodUtil;
import org.apache.niolex.network.rpc.RpcException;

/**
 * Common Utility to do protocol buffer serialization.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-5
 */
public abstract class ProtoUtil {

	/**
	 * Parse one object of type <code>type</code> from the byte array.
	 *
	 * @param ret
	 * @param type
	 * @return
	 */
	public static Object parseOne(byte[] ret, Type type) {
		if (type instanceof Class<?>) {
			try {
				Method method = MethodUtil.getMethod((Class<?>) type, "parseFrom", byte[].class);
				return method.invoke(null, ret);
			} catch (Exception e) {
				throw new RpcException("Return type is not protobuf type.",
						RpcException.Type.ERROR_PARSE_RETURN, e);
			}
		}
		throw new RpcException("Return type is not protobuf type.",
				RpcException.Type.ERROR_PARSE_RETURN, null);
	}

	/**
	 * Parse one object of type <code>type</code> from the input stream.
	 *
	 * @param in
	 * @param type
	 * @return
	 */
	public static Object parseOne(InputStream in, Type type) {
		if (type instanceof Class<?>) {
			try {
				Method method = MethodUtil.getMethod((Class<?>) type, "parseFrom", InputStream.class);
				return method.invoke(null, in);
			} catch (Exception e) {
				throw new RpcException("Parameter type is not protobuf type.",
						RpcException.Type.ERROR_PARSE_RETURN, e);
			}
		}
		throw new RpcException("Parameter type is not protobuf type.",
				RpcException.Type.ERROR_PARSE_RETURN, null);
	}

}
