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
import java.lang.reflect.Proxy;
import java.util.ArrayList;

import org.apache.niolex.commons.reflect.ItemNotFoundException;
import org.apache.niolex.commons.reflect.MethodUtil;
import org.apache.niolex.commons.util.Runner;
import org.apache.niolex.commons.util.SystemUtil;
import org.apache.niolex.network.rpc.PoolableInvocationHandler;
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

    public static final Method method = MethodUtil.getMethod(PoolHandlerTest.class, "thisMethod");

    ArrayList<IServiceHandler> col;

    public void thisMethod() {}

    @Before
    public void set() throws Throwable {
        col = new ArrayList<IServiceHandler>();
        for (int i = 0; i < 100; ++i) {
            IServiceHandler c = mock(IServiceHandler.class);
            when(c.getServiceUrl()).thenReturn("Id " + i);
            if (i % 3 == 0) {
                when(c.isReady()).thenReturn(false);
            } else {
                when(c.isReady()).thenReturn(true);
            }
            if (i == 23) {
                doThrow(new RpcException("Rpcc", RpcException.Type.ERROR_INVOKE, null)).when(c)
                .invoke(any(), any(Method.class), any(Object[].class));
            } else if (i == 46) {
                doThrow(new ItemNotFoundException("Root", null)).when(c)
                .invoke(any(), any(Method.class), any(Object[].class));
            } else {
                switch (i % 9) {
                    case 1:
                        doThrow(new RpcException("Conn1", RpcException.Type.CONNECTION_CLOSED, null)).when(c)
                        .invoke(any(), any(Method.class), any(Object[].class));
                        break;
                    case 3:
                        doThrow(new RpcException("Conn2", RpcException.Type.TIMEOUT, null)).when(c)
                        .invoke(any(), any(Method.class), any(Object[].class));
                        break;
                    case 5:
                        doThrow(new IOException("Tain", new IOException("Last"))).when(c)
                        .invoke(any(), any(Method.class), any(Object[].class));
                        break;
                    case 7:
                        doThrow(new Exception("Ex", new IOException("Last"))).when(c)
                        .invoke(any(), any(Method.class), any(Object[].class));
                        break;
                }
            }
            col.add(c);
        }
    }

    /**
     * Test method for {@link org.apache.niolex.network.cli.PoolHandler#PoolHandler(int, java.util.Collection)}.
     * @throws Throwable
     */
    @Test
    public void testPoolHandler() throws Throwable {
        col.remove(23);
        col.remove(45);
        PoolHandler<IServiceHandler> pool = new PoolHandler<IServiceHandler>(3, col);
        pool.offer(mock(IServiceHandler.class));
        for (int i = 0; i < 500; ++i) {
            pool.logDebug = i % 2 == 0;
            pool.invoke(pool, method, new Object[0]);
        }
    }

    /**
     * Test method for {@link org.apache.niolex.network.cli.PoolHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])}.
     * @throws Throwable
     */
    @Test(expected=RpcException.class)
    public void testInvokeRpcE() throws Throwable {
        PoolHandler<IServiceHandler> pool = new PoolHandler<IServiceHandler>(3, col);
        try {
            for (int i = 0; i < 200; ++i) {
                pool.invoke(pool, method, new Object[0]);
            }
        } catch (RpcException e) {
            assertEquals(RpcException.Type.ERROR_INVOKE, e.getType());
            throw e;
        }
    }

    /**
     * Test method for {@link org.apache.niolex.network.cli.PoolHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])}.
     * @throws Throwable
     */
    @Test(expected=ItemNotFoundException.class)
    public void testInvokeINFE() throws Throwable {
        col.remove(23);
        PoolHandler<IServiceHandler> pool = new PoolHandler<IServiceHandler>(3, col);
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
        PoolHandler<IServiceHandler> pool = new PoolHandler<IServiceHandler>(2, col);
        pool.setWaitTimeout(2);
        try {
            pool.invoke(pool, method, new Object[0]);
        } catch (RpcException e) {
            assertEquals(RpcException.Type.NO_SERVER_READY, e.getType());
            throw e;
        }
    }

    /**
     * Test method for {@link org.apache.niolex.network.cli.PoolHandler#setWaitTimeout(int)}.
     * @throws Throwable
     */
    @Test(expected=RpcException.class)
    public void testInvokeNoItem() throws Throwable {
        col.clear();
        PoolHandler<IServiceHandler> pool = new PoolHandler<IServiceHandler>(2, col);
        pool.setWaitTimeout(2);
        PoolableInvocationHandler hand = (PoolableInvocationHandler) Proxy.newProxyInstance(IServiceHandler.class.getClassLoader(),
                new Class[] {PoolableInvocationHandler.class}, pool);
        hand.getRemoteName();
    }

    /**
     * Test method for {@link org.apache.niolex.network.cli.PoolHandler#setWaitTimeout(int)}.
     * @throws Throwable
     */
    @Test(expected=RpcException.class)
    public void testExceedsRetry() throws Throwable {
        PoolHandler<IServiceHandler> pool = new PoolHandler<IServiceHandler>(2, col);
        try {
            for (int i = 0; i < 200; ++i) {
                pool.invoke(pool, method, new Object[0]);
            }
        } catch (RpcException e) {
            assertEquals(RpcException.Type.ERROR_EXCEED_RETRY, e.getType());
            throw e;
        }
    }

    /**
     * Test method for {@link org.apache.niolex.network.cli.PoolHandler#take()}.
     */
    @Test
    public void testTake() {
        col.clear();
        col.add(mock(IServiceHandler.class));
        col.add(mock(IServiceHandler.class));
        col.add(mock(IServiceHandler.class));
        col.add(mock(IServiceHandler.class));
        PoolHandler<IServiceHandler> pool = new PoolHandler<IServiceHandler>(2, col);
        assertNull(pool.take());
    }

    /**
     * Test method for {@link org.apache.niolex.network.cli.PoolHandler#take()}.
     */
    @Test
    public void testTakeNull() {
        col.clear();
        PoolHandler<IServiceHandler> pool = new PoolHandler<IServiceHandler>(2, col);
        pool.setWaitTimeout(2);
        assertNull(pool.take());
    }

    /**
     * Test method for {@link org.apache.niolex.network.cli.PoolHandler#takeOne(int)}.
     */
    @Test
    public void testTakeOne() {
        col.clear();
        col.add(mock(IServiceHandler.class));
        col.add(mock(IServiceHandler.class));
        col.add(mock(IServiceHandler.class));
        col.add(mock(IServiceHandler.class));
        PoolHandler<IServiceHandler> pool = new PoolHandler<IServiceHandler>(2, col);
        assertNotNull(pool.takeOne(0));
    }

    /**
     * Test method for {@link org.apache.niolex.network.cli.PoolHandler#takeOne(int)}.
     */
    @Test
    public void testTakeOneNull() {
        col.clear();
        PoolHandler<IServiceHandler> pool = new PoolHandler<IServiceHandler>(2, col);
        assertNull(pool.takeOne(2));
    }

    /**
     * Test method for {@link org.apache.niolex.network.cli.PoolHandler#takeOne(int)}.
     */
    @Test
    public void testTakeOneE() {
        col.clear();
        PoolHandler<IServiceHandler> pool = new PoolHandler<IServiceHandler>(2, col);
        Thread t = Runner.run(pool, "takeOne", 1000);
        SystemUtil.sleep(2);
        t.interrupt();
    }

    /**
     * Test method for {@link org.apache.niolex.network.cli.PoolHandler#getWaitTimeout()}.
     */
    @Test
    public void testGetWaitTimeout() {
        PoolHandler<IServiceHandler> pool = new PoolHandler<IServiceHandler>(2, col);
        pool.setWaitTimeout(2345);
        assertEquals(2345, pool.getWaitTimeout());
    }

    /**
     * Test method for {@link org.apache.niolex.network.cli.PoolHandler#getRetryTimes()}.
     */
    @Test
    public void testGetRetryTimes() {
        PoolHandler<IServiceHandler> pool = new PoolHandler<IServiceHandler>(54, col);
        assertEquals(54, pool.getRetryTimes());
    }

}
