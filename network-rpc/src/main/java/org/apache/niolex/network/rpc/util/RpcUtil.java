/**
 * RpcUtil.java
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
package org.apache.niolex.network.rpc.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.apache.niolex.commons.stream.JsonProxy;
import org.apache.niolex.network.PacketData;
import org.codehaus.jackson.type.TypeReference;

/**
 * Common utils for Rpc.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-1
 */
public abstract class RpcUtil {

	/**
	 * This is the class to return a type.
	 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
	 * @version 1.0.0
	 * @Date: 2012-7-24
	 */
	private static class TypeRe<T> extends TypeReference<T> {
		private Type type;

		public TypeRe(Type type) {
			super();
			this.type = type;
		}

		@Override
		public Type getType() {
			return type;
		}

	}

	/**
	 * Decode parameters to JavaType.
	 *
	 * @param generic
	 * @return
	 */
	public static final List<TypeRe<?>> decodeParams(Type[] generic) {
		List<TypeRe<?>> list = new ArrayList<TypeRe<?>>(generic.length);
		for (Type tp : generic) {
			list.add(new TypeRe<String>(tp));
		}
		return list;
	}

	/**
	 * prepare parameters, read them from the data, as the type specified by the second parameter.
	 *
	 * @param data
	 * @param generic
	 * @return
	 * @throws IOException
	 */
	public static final Object[] prepareParams(byte[] data, Type[] generic) throws IOException {
		List<TypeRe<?>> list = decodeParams(generic);
		Object[] ret = new Object[list.size()];
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		JsonProxy proxy = new JsonProxy(in);
		for (int i = 0; i < ret.length; ++i) {
			ret[i] = proxy.readObject(list.get(i));
		}
		return ret;
	}

	/**
	 * Generate Key for this PacketData.
	 *
	 * @param rc
	 * @return
	 */
	public static final int generateKey(PacketData rc) {
		byte r = rc.getReserved();
		if (r % 2 == 0) {
			--r;
		}
		return generateKey(rc.getCode(), rc.getVersion(), r);
	}

	/**
	 * Generate Key from a short and two bytes.
	 *
	 * @param a
	 * @param b
	 * @param c
	 * @return
	 */
	public static final int generateKey(short a, byte b, byte c) {
		int l = a << 16;
		l += ((b & 0xFF) << 8) + (c & 0xFF);
		return l;
	}
}
