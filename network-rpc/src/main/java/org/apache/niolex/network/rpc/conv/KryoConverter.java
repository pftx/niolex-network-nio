/**
 * KryoConverter.java
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

import org.apache.niolex.commons.stream.KryoInstream;
import org.apache.niolex.commons.stream.KryoOutstream;
import org.apache.niolex.network.rpc.IConverter;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0, $Date: 2012-12-2$
 */
public class KryoConverter implements IConverter {

    /**
     * This is the override of super method.
     * @see org.apache.niolex.network.rpc.IConverter#prepareParams(byte[], java.lang.reflect.Type[])
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object[] prepareParams(byte[] data, Type[] generic) throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        KryoInstream kin = new KryoInstream(in);
        Object[] rr = new Object[generic.length];
        for (int i = 0; i < generic.length; ++i) {
            Type t = generic[i];
            rr[i] = kin.readObject((Class<Object>) t);
        }
        return rr;
    }

    /**
     * This is the override of super method.
     * @see org.apache.niolex.network.rpc.IConverter#serializeParams(java.lang.Object[])
     */
    @Override
    public byte[] serializeParams(Object[] args) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
        KryoOutstream kout = new KryoOutstream(out);
        for (Object ret : args)
            kout.writeObject(ret);
        kout.close();
        return out.toByteArray();
    }

    /**
     * This is the override of super method.
     * @see org.apache.niolex.network.rpc.IConverter#prepareReturn(byte[], java.lang.reflect.Type)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object prepareReturn(byte[] ret, Type type) throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(ret);
        KryoInstream kin = new KryoInstream(in);
        return kin.readObject((Class<Object>) type);
    }

    /**
     * This is the override of super method.
     * @see org.apache.niolex.network.rpc.IConverter#serializeReturn(java.lang.Object)
     */
    @Override
    public byte[] serializeReturn(Object ret) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
        KryoOutstream kout = new KryoOutstream(out);
        kout.writeObject(ret);
        kout.close();
        return out.toByteArray();
    }

}
