/**
 * AbstractServerInterceptorTest.java
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

import org.junit.Test;

/**
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5
 * @since 2013-1-21
 */
public class AbstractServerInterceptorTest {

    AbstractServerInterceptor i = new AbstractServerInterceptor(){};

    /**
     * Test method for {@link org.apache.niolex.network.svr.interceptor.AbstractServerInterceptor#beforePrepareParams(java.lang.Object, java.lang.reflect.Method, org.apache.niolex.network.PacketData, org.apache.niolex.network.IPacketWriter)}.
     */
    @Test
    public void testBeforePrepareParams() {
        i.beforePrepareParams(i, null, null, null);
    }

    /**
     * Test method for {@link org.apache.niolex.network.svr.interceptor.AbstractServerInterceptor#beforeInvoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[], org.apache.niolex.network.IPacketWriter)}.
     */
    @Test
    public void testBeforeInvoke() {
        i.beforeInvoke(i, null, null, null);
    }

    /**
     * Test method for {@link org.apache.niolex.network.svr.interceptor.AbstractServerInterceptor#afterInvoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[], java.lang.Object, org.apache.niolex.network.IPacketWriter)}.
     */
    @Test
    public void testAfterInvoke() {
        i.afterInvoke(i, null, null, i, null);
    }

    /**
     * Test method for {@link org.apache.niolex.network.svr.interceptor.AbstractServerInterceptor#beforeSend(org.apache.niolex.network.PacketData, org.apache.niolex.network.IPacketWriter, java.lang.Object, int)}.
     */
    @Test
    public void testBeforeSend() {
        i.beforeSend(null, null, i, 0);
    }

}
