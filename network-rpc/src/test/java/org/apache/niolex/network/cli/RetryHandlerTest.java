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
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.niolex.commons.reflect.FieldUtil;
import org.apache.niolex.commons.reflect.MethodUtil;
import org.apache.niolex.commons.util.Runner;
import org.apache.niolex.commons.util.SystemUtil;
import org.apache.niolex.network.rpc.RpcException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5, $Date: 2012-11-13$
 */
public class RetryHandlerTest {

	private List<IServiceHandler> handlers;

	@Mock
	private IServiceHandler handler1;

	@Mock
	private IServiceHandler handler2;

	@Mock
	private IServiceHandler handler3;

	private int intervalBetweenRetry = 10;

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
	@Test
	public void testInvokeSuccess() throws Throwable {
	    Method method = MethodUtil.getMethods(PoolHandlerTest.class, "thisMethod")[0];
		retryHandler.invoke(handler1, method, null);
		retryHandler.invoke(handler1, method, null);
	}

	/**
     * Test method for {@link org.apache.niolex.network.cli.RetryHandler#RetryHandler(java.util.List, int, int)}.
     * @throws Throwable
     */
    @Test
    public void testRetryHandler() throws Throwable {
        Method method = MethodUtil.getMethods(PoolHandlerTest.class, "thisMethod")[0];
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
	@Test
	public void testInvokeSleep() throws Throwable {
	    when(handler2.isReady()).thenReturn(true);
	    retryHandler = new RetryHandler(handlers, retryTimes, 1000);
	    doThrow(new RpcException("Conn1", RpcException.Type.TIMEOUT, null)).when(handler1)
        .invoke(any(), any(Method.class), any(Object[].class));
	    Thread t = Runner.run(this, "testInvokeSuccess");
        SystemUtil.sleep(2);
        t.interrupt();
        SystemUtil.sleep(10);
        t.interrupt();
        t.join();
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
	public void testInvoke() throws Throwable {
		RuntimeException er = new RuntimeException("cde");
		Method m = MethodUtil.getMethods(getClass())[0];
		when(handler2.getServiceUrl()).thenThrow(er);
		when(handler2.isReady()).thenReturn(true);
		boolean flag = false;
		try {
			retryHandler.invoke(handler1, m, null);
			retryHandler.invoke(handler1, m, null);
		} catch (RuntimeException f) {
			assertEquals(er, f);
			flag = true;
		}
		assertTrue(flag);
	}

	@Test
	public void testInvokeOverMax() throws Throwable {
		Field idxField = FieldUtil.getField(RetryHandler.class, "idx");
		idxField.setAccessible(true);
		AtomicInteger idx = (AtomicInteger)idxField.get(retryHandler);
		idx.set(Integer.MAX_VALUE - 1000);
		retryHandler.logDebug = false;
		retryHandler.invoke(handler1, null, null);
		retryHandler.invoke(handler1, null, null);
		retryHandler.invoke(handler1, null, null);
		System.out.println("DKDKDK " + idx.get());
		assertTrue(idx.get() < 8);
	}

	@Test
	public void testInvokeNothing() throws Throwable {
		retryHandler = new RetryHandler(new ArrayList<IServiceHandler>(), retryTimes, intervalBetweenRetry);
		Method m = MethodUtil.getMethods(getClass())[0];
		try {
			retryHandler.invoke(handler1, m, null);
			assertTrue(false);
		} catch (RpcException e) {
			assertEquals(RpcException.Type.NO_SERVER_READY, e.getType());
		}
	}

	@Test
	public void testInvokeAllBad() throws Throwable {
		handlers = new ArrayList<IServiceHandler>();
		when(handler2.isReady()).thenReturn(true);
		when(handler3.isReady()).thenReturn(true);
		RuntimeException e = new RuntimeException("cde", new Throwable("efg"));
		Method m = MethodUtil.getMethods(getClass())[0];

		when(handler2.invoke(handler1, m, null)).thenThrow(e);
		when(handler3.invoke(handler1, m, null)).thenThrow(e);
		handlers.add(handler2);
		handlers.add(handler3);
		handlers.add(handler2);
		handlers.add(handler3);

		retryHandler = new RetryHandler(handlers, 2, intervalBetweenRetry);
		for (int i = 0; i < 4; ++i) {
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
		Method m = MethodUtil.getMethods(getClass())[0];

		when(handler2.invoke(handler1, m, null)).thenThrow(e);
		when(handler3.invoke(handler1, m, null)).thenThrow(e);
		handlers.add(handler2);
		handlers.add(handler3);

		retryHandler = new RetryHandler(handlers, 2, intervalBetweenRetry);
		try {
			retryHandler.invoke(handler1, m, null);
			assertTrue(false);
		} catch (RuntimeException f) {
			assertEquals(e, f);
		}
	}

	@Test
	public void testInvokeBadNotReady() throws Throwable {
		when(handler2.isReady()).thenReturn(true);
		when(handler3.isReady()).thenReturn(true);
		IOException ioe = new IOException("ioio");
		RuntimeException e = new RuntimeException("cde", ioe);
		Method m = MethodUtil.getMethods(getClass())[0];

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
		assertEquals(handlers, retryHandler.getHandlers());
	}

}
