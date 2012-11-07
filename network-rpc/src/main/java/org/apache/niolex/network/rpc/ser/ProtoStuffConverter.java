/**
 * ProtoStuffConverter.java
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
package org.apache.niolex.network.rpc.ser;

import java.lang.reflect.Type;

import org.apache.niolex.commons.seri.ProtoStuffUtil;
import org.apache.niolex.network.rpc.IConverter;

/**
 * Using god like man protostuff protocol to serialize data.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-11-7
 */
public class ProtoStuffConverter implements IConverter {

	/**
	 * Override super method
	 * @see org.apache.niolex.network.rpc.IConverter#prepareParams(byte[], java.lang.reflect.Type[])
	 */
	@Override
	public Object[] prepareParams(byte[] data, Type[] generic) throws Exception {
		return ProtoStuffUtil.parseMulti(data, generic);
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.rpc.IConverter#serializeParams(java.lang.Object[])
	 */
	@Override
	public byte[] serializeParams(Object[] args) throws Exception {
		return ProtoStuffUtil.seriMulti(args);
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.rpc.IConverter#prepareReturn(byte[], java.lang.reflect.Type)
	 */
	@Override
	public Object prepareReturn(byte[] ret, Type type) throws Exception {
		return ProtoStuffUtil.parseOne(ret, type);
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.rpc.IConverter#serializeReturn(java.lang.Object)
	 */
	@Override
	public byte[] serializeReturn(Object ret) throws Exception {
		return ProtoStuffUtil.seriOne(ret);
	}

}
