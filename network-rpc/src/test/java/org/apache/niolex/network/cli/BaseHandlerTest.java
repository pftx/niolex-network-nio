/**
 * BaseHandlerTest.java
 *
 * Copyright 2014 the original author or authors.
 *
 * We licenses this file to you under the Apache License, version 2.0
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
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeoutException;

import org.apache.niolex.commons.reflect.MethodUtil;
import org.apache.niolex.network.rpc.RpcException;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2014-1-3
 */
public class BaseHandlerTest extends BaseHandler {

    @Test
    public void testLogInvoke() throws Exception {
        IServiceHandler s = mock(IServiceHandler.class);
        when(s.getServiceUrl()).thenReturn("^_^");
        Method method = MethodUtil.getMethod(getClass(), "testLogInvoke");
        String e = "GID Succeed to invoke handler on [^_^] time {6}, method {public void org.apache.niolex.network.cli.BaseHandlerTest.testLogInvoke() throws java.lang.Exception}";
        assertEquals(e, logInvoke(s, method, 6));
    }

    @Test
    public void testLogError() throws Exception {
        IServiceHandler s = mock(IServiceHandler.class);
        when(s.getServiceUrl()).thenReturn("^_^");
        Method method = MethodUtil.getMethod(getClass(), "testLogInvoke");
        String e = "GID Failed to invoke handler on [^_^] time {6}, method {public void org.apache.niolex.network.cli.BaseHandlerTest.testLogInvoke() throws java.lang.Exception} RETRY 3 ERRMSG: java.lang.Exception: JDIID";
        assertEquals(e, logError(s, method, 6, 3, new Exception("JDIID")));
    }

    @Test
    public void testProcessException() throws Throwable {
        IServiceHandler handler = mock(IServiceHandler.class);
        RpcException e = new RpcException();
        e.setType(RpcException.Type.TIMEOUT);
        assertTrue(processException(e, handler));
    }

    @Test(expected=RpcException.class)
    public void testProcessRpcException() throws Throwable {
        IServiceHandler handler = mock(IServiceHandler.class);
        RpcException e = new RpcException();
        e.setType(RpcException.Type.ERROR_EXCEED_RETRY);
        assertTrue(processException(e, handler));
    }

    @Test(expected=TimeoutException.class)
    public void testProcessTimeoutException() throws Throwable {
        IServiceHandler handler = mock(IServiceHandler.class);
        TimeoutException e = new TimeoutException();
        assertTrue(processException(e, handler));
    }

    @Test
    public void testProcessSubTimeoutException() throws Throwable {
        IServiceHandler handler = mock(IServiceHandler.class);
        TimeoutException e = new TimeoutException();
        Exception e2 = new Exception(e);
        assertFalse(processException(e2, handler));
        verify(handler, times(0)).notReady(any(IOException.class));
    }

    @Test
    public void testProcessIOException() throws Throwable {
        IServiceHandler handler = mock(IServiceHandler.class);
        IOException e = new IOException();
        assertFalse(processException(e, handler));
        verify(handler, times(1)).notReady(e);
    }

    @Test
    public void testProcessSubIOException() throws Throwable {
        IServiceHandler handler = mock(IServiceHandler.class);
        IOException e = new IOException();
        Exception e2 = new Exception(e);
        assertFalse(processException(e2, handler));
        verify(handler, times(1)).notReady(e);
    }

    @Test
    public void testIsLogDebug() throws Exception {
        assertEquals(false, isLogDebug());
    }

    @Test
    public void testSetLogDebug() throws Exception {
        assertEquals(false, isLogDebug());
        setLogDebug(true);
        assertEquals(true, isLogDebug());
        setLogDebug(false);
        assertEquals(false, isLogDebug());
    }

}
