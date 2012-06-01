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
package org.apache.niolex.network.rpc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.apache.niolex.commons.compress.JacksonUtil;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.type.JavaType;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-1
 */
public abstract class RpcUtil {

	@SuppressWarnings("deprecation")
	public static final List<JavaType> decodeParams(Type[] generic) {
		List<JavaType> list = new ArrayList<JavaType>(generic.length);
		for (Type tp : generic) {
			list.add(TypeFactory.type(tp));
		}
		return list;
	}

	public static final Object[] prepareParams(byte[] data, Type[] generic) throws IOException {
		List<JavaType> list = decodeParams(generic);
		Object[] ret = new Object[list.size()];
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		for (int i = 0; i < ret.length; ++i) {
			ret[i] = JacksonUtil.readObj(in, list.get(i));
		}
		return ret;
	}
}
