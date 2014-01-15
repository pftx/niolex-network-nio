/**
 * RetryHandlerTest.java
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
package org.apache.niolex.network.cli;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.niolex.commons.reflect.FieldUtil;
import org.apache.niolex.commons.reflect.MethodUtil;
import org.apache.niolex.network.rpc.RpcException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5, $Date: 2012-11-13$
 */
@RunWith(MockitoJUnitRunner.class)
public class RetryHandlerTest {

    private static final Method method = MethodUtil.getMethod(PoolHandlerTest.class, "thisMethod");

	private List<IServiceHandler> handlers;

	@Mock
	private IServiceHandler handler1;

	@Mock
	private IServiceHandler handler2;

	@Mock
	private IServiceHandler handler3;

	private int intervalBetweenRetry = 5;

	private int retryTimes = 3;

	private RetryHandler retryHandler;

	@Before
	public void setUp() {
		handlers = new ArrayList<IServiceHandler>();
		handlers.add(handler1);
		handlers.add(handler2);
		handlers.add(handler3);
		when(handler1.getServiceUrl()).thenReturn("1abc");
		when(handler2.getServiceUrl()).thenReturn("2abc");
		when(handler3.getServiceUrl()).thenReturn("3abc");
		when(handler1.toString()).thenReturn("abccba");
		when(handler1.isReady()).thenReturn(true);
		retryHandler = new RetryHandler(handlers, retryTimes, intervalBetweenRetry);
	}

	/**
	 * Test method for {@link org.apache.niolex.network.cli.RetryHandler#RetryHandler(java.util.List, int, int)}.
	 * @throws Throwable
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testRetryHandlerInit() throws Throwable {
	    List<IServiceHandler> list = Collections.emptyList();
	    new RetryHandler(list, retryTimes, intervalBetweenRetry);
	}

	/**
	 * Test method for {@link org.apache.niolex.network.cli.RetryHandler#RetryHandler(java.util.List, int, int)}.
	 * @throws Throwable
	 */
	@Test
	public void testInvokeSuccess() throws Throwable {
		retryHandler.invoke(handler1, method, null);
		retryHandler.logDebug = false;
		retryHandler.invoke(handler1, method, null);
	}

	/**
     * Test method for {@link org.apache.niolex.network.cli.RetryHandler#RetryHandler(java.util.List, int, int)}.
     * @throws Throwable
     */
    @Test
    public void testRetryHandlerCrossMaxValue() throws Throwable {
        retryHandler.invoke(handler1, method, null);
        FieldUtil.setValue(retryHandler, "idx", new AtomicInteger(Integer.MAX_VALUE - 5));
        retryHandler.invoke(handler1, method, null);
        AtomicInteger idx = FieldUtil.getValue(retryHandler, "idx");
        assertTrue(4 > idx.get());
    }

	/**
	 * Test method for {@link org.apache.niolex.network.cli.RetryHandler#RetryHandler(java.util.List, int, int)}.
	 * @throws Throwable
	 */
	@Test(expected=RpcException.class)
	public void testInvokeErr() throws Throwable {
	    doThrow(new RpcException("Conn1", RpcException.Type.ERROR_INVOKE, null)).when(handler1)
	    .invoke(any(), any(Method.class), any(Object[].class));
	    testInvokeSuccess();
	}

	/**
	 * Test method for {@link org.apache.niolex.network.cli.RetryHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])}.
	 * @throws Throwable
	 */
	@Test
	public void testThrowException() throws Throwable {
		RuntimeException er = new RuntimeException("cde");
		when(handler2.getServiceUrl()).thenThrow(er);
		when(handler2.isReady()).thenReturn(true);
		boolean flag = false;
		try {
			retryHandler.invoke(handler1, method, null);
			retryHandler.invoke(handler1, method, null);
		} catch (RuntimeException f) {
			assertEquals(er, f);
			flag = true;
		}
		assertTrue(flag);
	}

	@Test
	public void testInvokeNoServerReady() throws Throwable {
	    ArrayList<IServiceHandler> list = new ArrayList<IServiceHandler>();
	    list.add(new RpcServiceHandler("5", new C(), 1000, true));
	    list.add(new RpcServiceHandler("5", new C(), 1000, true));
	    list.add(new RpcServiceHandler("5", new C(), 1000, true));
		retryHandler = new RetryHandler(list, 5, intervalBetweenRetry);
		Method m = MethodUtil.getMethods(getClass()).get(0);
		try {
			retryHandler.invoke(handler1, m, null);
			assertTrue(false);
		} catch (RpcException e) {
			assertEquals(RpcException.Type.NO_SERVER_READY, e.getType());
		}
	}

	@Test
	public void testInvokeExceedsMaxRetry() throws Throwable {
		handlers = new ArrayList<IServiceHandler>();
		when(handler2.isReady()).thenReturn(true);
		when(handler3.isReady()).thenReturn(true);
		RuntimeException e = new RuntimeException("cde", new Throwable("efg"));
		Method m = MethodUtil.getMethods(getClass()).get(0);

		when(handler2.invoke(handler1, m, null)).thenThrow(e);
		when(handler3.invoke(handler1, m, null)).thenThrow(e);
		handlers.add(handler2);
		handlers.add(handler3);
		handlers.add(handler2);
		handlers.add(handler3);

		retryHandler = new RetryHandler(handlers, 2, intervalBetweenRetry);
		for (int i = 0; i < 8; ++i) {
			try {
				retryHandler.invoke(handler1, m, null);
				assertTrue(false);
			} catch (RpcException f) {
				assertEquals(RpcException.Type.ERROR_EXCEED_RETRY, f.getType());
			}
		}
	}

	@Test
	public void testInvokeBadNoRetry() throws Throwable {
		handlers = new ArrayList<IServiceHandler>();
		when(handler2.isReady()).thenReturn(true);
		when(handler3.isReady()).thenReturn(true);
		RuntimeException e = new RuntimeException("cde");
		Method m = MethodUtil.getMethods(getClass()).get(0);

		when(handler2.invoke(handler1, m, null)).thenThrow(e);
		when(handler3.invoke(handler1, m, null)).thenThrow(e);
		handlers.add(handler2);
		handlers.add(handler3);

		retryHandler = new RetryHandler(handlers, 2, intervalBetweenRetry);
		for (int i = 0; i < 8; ++i) {
    		try {
    			retryHandler.invoke(handler1, m, null);
    			assertTrue(false);
    		} catch (RuntimeException f) {
    			assertEquals(e, f);
    		}
		}
	}

	@Test
	public void testInvokeBadNotReady() throws Throwable {
		when(handler2.isReady()).thenReturn(true);
		when(handler3.isReady()).thenReturn(true);
		IOException ioe = new IOException("ioio");
		RuntimeException e = new RuntimeException("cde", ioe);
		Method m = MethodUtil.getMethods(getClass()).get(0);

		when(handler2.invoke(handler1, m, null)).thenThrow(e);
		when(handler3.invoke(handler1, m, null)).thenThrow(e);

		retryHandler = new RetryHandler(handlers, 3, intervalBetweenRetry);
		for (int i = 0; i < 4; ++i) {
			try {
				retryHandler.invoke(handler1, m, null);
			} catch (RuntimeException f) {
				assertTrue(false);
			}
		}
		verify(handler2, atLeastOnce()).notReady(ioe);
		verify(handler3, atLeastOnce()).notReady(ioe);
	}

	/**
	 * Test method for {@link org.apache.niolex.network.cli.RetryHandler#toString()}.
	 */
	@Test
	public void testToString() {
		assertTrue(retryHandler.toString().contains("abccba"));
	}

	/**
	 * Test method for {@link org.apache.niolex.network.cli.RetryHandler#getHandlers()}.
	 */
	@Test
	public void testGetHandlers() {
		assertEquals(3, retryHandler.getHandlers().size());
	}

}
