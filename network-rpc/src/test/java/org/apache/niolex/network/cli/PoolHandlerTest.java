/**
 * PoolHandlerTest.java
 *
 * Copyright 2012 The original author or authors.
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
package org.apache.niolex.network.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.apache.niolex.commons.reflect.MethodUtil;
import org.apache.niolex.commons.util.Runner;
import org.apache.niolex.commons.util.SystemUtil;
import org.apache.niolex.network.rpc.RpcClient;
import org.apache.niolex.network.rpc.RpcException;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5
 * @since 2012-12-6
 */
public class PoolHandlerTest {

    ArrayList<RpcClient> col;
    Method method;

    public void thisMethod() {}

    @Before
    public void set() throws Throwable {
        col = new ArrayList<RpcClient>();
        for (int i = 0; i < 100; ++i) {
            RpcClient c = mock(RpcClient.class);
            when(c.getRemoteName()).thenReturn("Id " + i);
            if (i % 3 == 0) {
                when(c.isValid()).thenReturn(false);
            } else {
                when(c.isValid()).thenReturn(true);
            }
            if (i == 23) {
                doThrow(new RpcException("Rpcc", RpcException.Type.ERROR_INVOKE, null)).when(c)
                .invoke(any(), any(Method.class), any(Object[].class));
            } else if (i == 46) {
                doThrow(new IOException("Root")).when(c)
                .invoke(any(), any(Method.class), any(Object[].class));
            } else {
            switch (i % 7) {
                case 1:
                    doThrow(new RpcException("Conn1", RpcException.Type.CONNECTION_CLOSED, null)).when(c)
                    .invoke(any(), any(Method.class), any(Object[].class));
                    break;
                case 3:
                    doThrow(new RpcException("Conn2", RpcException.Type.TIMEOUT, null)).when(c)
                    .invoke(any(), any(Method.class), any(Object[].class));
                    break;
                case 6:
                    doThrow(new IOException("Tain", new IOException("Last"))).when(c)
                    .invoke(any(), any(Method.class), any(Object[].class));
                    break;
            }
            }
            col.add(c);
        }
        method = MethodUtil.getMethods(PoolHandlerTest.class, "thisMethod")[0];
    }

    /**
     * Test method for {@link org.apache.niolex.network.cli.PoolHandler#PoolHandler(int, java.util.Collection)}.
     * @throws Throwable
     */
    @Test
    public void testPoolHandler() throws Throwable {
        col.remove(23);
        col.remove(45);
        PoolHandler pool = new PoolHandler(3, col);
        pool.offer(mock(RpcClient.class));
        for (int i = 0; i < 500; ++i) {
            pool.invoke(pool, method, new Object[0]);
        }
    }

    /**
     * Test method for {@link org.apache.niolex.network.cli.PoolHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])}.
     * @throws Throwable
     */
    @Test(expected=RpcException.class)
    public void testInvokeE() throws Throwable {
        PoolHandler pool = new PoolHandler(3, col);
        for (int i = 0; i < 200; ++i) {
            pool.invoke(pool, method, new Object[0]);
        }
    }

    /**
     * Test method for {@link org.apache.niolex.network.cli.PoolHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])}.
     * @throws Throwable
     */
    @Test(expected=IOException.class)
    public void testInvokeR() throws Throwable {
        col.remove(23);
        PoolHandler pool = new PoolHandler(3, col);
        for (int i = 0; i < 200; ++i) {
            pool.invoke(pool, method, new Object[0]);
        }
    }

    /**
     * Test method for {@link org.apache.niolex.network.cli.PoolHandler#setWaitTimeout(int)}.
     * @throws Throwable
     */
    @Test(expected=RpcException.class)
    public void testNoReady() throws Throwable {
        col.clear();
        PoolHandler pool = new PoolHandler(2, col);
        pool.setWaitTimeout(2);
        pool.invoke(pool, method, new Object[0]);
    }

    /**
     * Test method for {@link org.apache.niolex.network.cli.PoolHandler#setWaitTimeout(int)}.
     * @throws Throwable
     */
    @Test(expected=RpcException.class)
    public void testExceedsRetry() throws Throwable {
        PoolHandler pool = new PoolHandler(2, col);
        for (int i = 0; i < 200; ++i) {
            pool.invoke(pool, method, new Object[0]);
        }
    }

    /**
     * Test method for {@link org.apache.niolex.network.cli.PoolHandler#take()}.
     */
    @Test
    public void testTake() {
        col.clear();
        col.add(mock(RpcClient.class));
        col.add(mock(RpcClient.class));
        col.add(mock(RpcClient.class));
        col.add(mock(RpcClient.class));
        PoolHandler pool = new PoolHandler(2, col);
        assertNull(pool.take());
    }

    /**
     * Test method for {@link org.apache.niolex.network.cli.PoolHandler#take()}.
     */
    @Test
    public void testTakeNull() {
        col.clear();
        PoolHandler pool = new PoolHandler(2, col);
        pool.setWaitTimeout(2);
        assertNull(pool.take());
    }

    /**
     * Test method for {@link org.apache.niolex.network.cli.PoolHandler#takeOne(int)}.
     */
    @Test
    public void testTakeOne() {
        col.clear();
        col.add(mock(RpcClient.class));
        col.add(mock(RpcClient.class));
        col.add(mock(RpcClient.class));
        col.add(mock(RpcClient.class));
        PoolHandler pool = new PoolHandler(2, col);
        assertNotNull(pool.takeOne(0));
    }

    /**
     * Test method for {@link org.apache.niolex.network.cli.PoolHandler#takeOne(int)}.
     */
    @Test
    public void testTakeOneNull() {
        col.clear();
        PoolHandler pool = new PoolHandler(2, col);
        assertNull(pool.takeOne(2));
    }

    /**
     * Test method for {@link org.apache.niolex.network.cli.PoolHandler#takeOne(int)}.
     */
    @Test
    public void testTakeOneE() {
        col.clear();
        PoolHandler pool = new PoolHandler(2, col);
        Thread t = Runner.run(pool, "takeOne", 1000);
        SystemUtil.sleep(2);
        t.interrupt();
    }

    /**
     * Test method for {@link org.apache.niolex.network.cli.PoolHandler#getWaitTimeout()}.
     */
    @Test
    public void testGetWaitTimeout() {
        PoolHandler pool = new PoolHandler(2, col);
        pool.setWaitTimeout(2345);
        assertEquals(2345, pool.getWaitTimeout());
    }

    /**
     * Test method for {@link org.apache.niolex.network.cli.PoolHandler#getRetryTimes()}.
     */
    @Test
    public void testGetRetryTimes() {
        PoolHandler pool = new PoolHandler(54, col);
        assertEquals(54, pool.getRetryTimes());
    }

}
