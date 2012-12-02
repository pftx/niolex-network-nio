/**
 * SmileConverter.java
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
package org.apache.niolex.network.rpc.conv;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;
import java.util.List;

import org.apache.niolex.commons.seri.SmileUtil;
import org.apache.niolex.commons.stream.SmileProxy;
import org.apache.niolex.network.rpc.IConverter;
import org.apache.niolex.network.rpc.util.RpcUtil;
import org.apache.niolex.network.rpc.util.RpcUtil.TypeRe;

/**
 * Using Smile / Jackson to serialize data.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0, $Date: 2012-12-2$
 */
public class SmileConverter implements IConverter {

    /**
     * This is the override of super method.
     * @see org.apache.niolex.network.rpc.IConverter#prepareParams(byte[], java.lang.reflect.Type[])
     */
    @Override
    public Object[] prepareParams(byte[] data, Type[] generic) throws Exception {
        List<TypeRe<?>> list = RpcUtil.decodeParams(generic);
        Object[] ret = new Object[list.size()];
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        SmileProxy proxy = new SmileProxy(in);
        for (int i = 0; i < ret.length; ++i) {
            ret[i] = proxy.readObject(list.get(i));
        }
        return ret;
    }

    /**
     * This is the override of super method.
     * @see org.apache.niolex.network.rpc.IConverter#serializeParams(java.lang.Object[])
     */
    @Override
    public byte[] serializeParams(Object[] args) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (Object o : args) {
            SmileUtil.writeObj(out, o);
        }
        return out.toByteArray();
    }

    /**
     * This is the override of super method.
     * @see org.apache.niolex.network.rpc.IConverter#prepareReturn(byte[], java.lang.reflect.Type)
     */
    @Override
    public Object prepareReturn(byte[] ret, Type type) throws Exception {
        return SmileUtil.bin2Obj(ret, new TypeRe<Object>(type));
    }

    /**
     * This is the override of super method.
     * @see org.apache.niolex.network.rpc.IConverter#serializeReturn(java.lang.Object)
     */
    @Override
    public byte[] serializeReturn(Object ret) throws Exception {
        return SmileUtil.obj2bin(ret);
    }

}
