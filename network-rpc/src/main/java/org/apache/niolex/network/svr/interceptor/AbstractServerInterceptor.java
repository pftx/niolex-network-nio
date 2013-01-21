/**
 * AbstractServerInterceptor.java
 *
 * Copyright 2013 The original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.niolex.network.svr.interceptor;

import java.lang.reflect.Method;

import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;

/**
 * The basic implementation of {@link ServerInterceptor}. We do nothing here, just
 * return true all the times. User may extend this class to override the method(s)
 * they want to deal with and left the others here.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5
 * @since 2013-1-21
 */
public abstract class AbstractServerInterceptor implements ServerInterceptor {

    /**
     * Override super method
     * @see org.apache.niolex.network.svr.interceptor.ServerInterceptor#beforePrepareParams(java.lang.Object, java.lang.reflect.Method, org.apache.niolex.network.PacketData, org.apache.niolex.network.IPacketWriter)
     */
    @Override
    public boolean beforePrepareParams(Object host, Method method, PacketData sc, IPacketWriter wt) {
        return true;
    }

    /**
     * Override super method
     * @see org.apache.niolex.network.svr.interceptor.ServerInterceptor#beforeInvoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[], org.apache.niolex.network.IPacketWriter)
     */
    @Override
    public boolean beforeInvoke(Object host, Method method, Object[] args, IPacketWriter wt) {
        return true;
    }

    /**
     * Override super method
     * @see org.apache.niolex.network.svr.interceptor.ServerInterceptor#afterInvoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[], java.lang.Object, org.apache.niolex.network.IPacketWriter)
     */
    @Override
    public boolean afterInvoke(Object host, Method method, Object[] args, Object ret, IPacketWriter wt) {
        return true;
    }

    /**
     * Override super method
     * @see org.apache.niolex.network.svr.interceptor.ServerInterceptor#beforeSend(org.apache.niolex.network.PacketData, org.apache.niolex.network.IPacketWriter, java.lang.Object, int)
     */
    @Override
    public boolean beforeSend(PacketData rc, IPacketWriter wt, Object ret, int exception) {
        return true;
    }

}
