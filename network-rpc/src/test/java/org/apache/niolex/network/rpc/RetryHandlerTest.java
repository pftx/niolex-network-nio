/**
 * RetryHandlerTest.java
 *
 * Copyright 2011 Niolex, Inc.
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
package org.apache.niolex.network.rpc;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.apache.niolex.commons.reflect.MethodUtil;
import org.junit.Test;


/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 *
 * @version @version@, $Date: 2011-9-15$
 *
 */
public class RetryHandlerTest {
    private static RetryHandler a = new RetryHandler(RpcServiceHandlerTest.listHandlers, 3, 10);

    @Test
    public void testName() {
        Assert.assertEquals("[5, 6, 7, 8]", a.toString());
        Assert.assertEquals(RpcServiceHandlerTest.listHandlers, a.getHandlers());
    }

    @Test
    public void testInvokeBlance() throws Throwable {
        Map<String, Integer> m = new HashMap<String, Integer>();
        for (int i = 0; i < 10000; ++i) {
            String name = a.invoke(a, null, null).toString();
            Integer t = m.get(name);
            if (t == null)
                t = 0;
            t = t + 1;
            m.put(name, t);
        }
        System.out.println(m);
        for (Integer k : m.values()) {
            Assert.assertTrue("Must relative in 100", k > 2400);
            Assert.assertTrue("Must relative in 100", k < 2600);
        }
    }

    @Test
    public void testErrorBlance() throws Throwable {
        RpcServiceHandlerTest.listHandlers.get(3).notReady(new IOException("For test"));
        Map<String, Integer> m = new HashMap<String, Integer>();
        for (int i = 0; i < 7500; ++i) {
            String name = a.invoke(a, null, null).toString();
            Integer t = m.get(name);
            if (t == null)
                t = 0;
            t = t + 1;
            m.put(name, t);
        }
        System.out.println(m);
        for (Integer k : m.values()) {
            Assert.assertTrue("Must relative in 100", k > 2400);
            Assert.assertTrue("Must relative in 100", k < 2600);
        }
    }

    @Test
    public void testErrorRetry() throws Throwable {
        List<IServiceHandler> listHandlers = new ArrayList<IServiceHandler>();
        listHandlers.add(new RpcServiceHandler("5", new B("5"), 100, true));
        listHandlers.add(new RpcServiceHandler("6", new B("6"), 100, true));
        listHandlers.add(new RpcServiceHandler("8", new A("8"), 5000, true));
        RetryHandler a = new RetryHandler(listHandlers, 3, 10);
        Method m = MethodUtil.getMethods(B.class, "invoke")[0];
        for (int i = 0; i < 200; ++i) {
            String name = a.invoke(a, m, null).toString();
            System.out.println(name);
        }
    }

    @Test(expected=RpcInvokeException.class)
    public void testErrorException() throws Throwable {
    	List<IServiceHandler> listHandlers = new ArrayList<IServiceHandler>();
        listHandlers.add(new RpcServiceHandler("5", new C(), 1000, true));
        listHandlers.add(new RpcServiceHandler("6", new C(), 1000, true));
        listHandlers.add(new RpcServiceHandler("8", new C(), 1000, true));
        RetryHandler a = new RetryHandler(listHandlers, 2, 10);
        Method m = MethodUtil.getMethods(B.class, "invoke")[0];
        String name = a.invoke(a, m, null).toString();
        System.out.println(name);
    }

    @Test(expected=RpcInvokeException.class)
    public void testFail() throws Throwable {
    	List<IServiceHandler> listHandlers = new ArrayList<IServiceHandler>();
        listHandlers.add(new RpcServiceHandler("5", new B("5"), 100, false));
        listHandlers.add(new RpcServiceHandler("6", new B("6"), 100, false));
        listHandlers.add(new RpcServiceHandler("8", new A("8"), 5000, false));
        RetryHandler a = new RetryHandler(listHandlers, 3, 10);
        Method m = MethodUtil.getMethods(B.class, "invoke")[0];
        String name = a.invoke(a, m, null).toString();
        System.out.println(name);
    }

    @Test(expected=RpcInvokeException.class)
    public void testFail2() throws Throwable {
    	List<IServiceHandler> listHandlers = new ArrayList<IServiceHandler>();
    	listHandlers.add(new RpcServiceHandler("5", new B("5"), 100, false));
    	listHandlers.add(new RpcServiceHandler("6", new B("6"), 100, false));
    	listHandlers.add(new RpcServiceHandler("8", new A("8"), 5000, false));
    	RetryHandler a = new RetryHandler(listHandlers, 2, 10);
    	Method m = MethodUtil.getMethods(B.class, "invoke")[0];
    	String name = a.invoke(a, m, null).toString();
    	System.out.println(name);
    }
}

class B implements InvocationHandler {
    private final String name;

    public B(String name) {
        super();
        this.name = name;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("B invoke for: " + name);
        if (System.currentTimeMillis() % 2 == 0)
            throw new Exception("B", new SocketException("IDJD"));
        return name;
    }

}

class C implements InvocationHandler {

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		throw new Exception("B", new SocketException("IDJD"));
	}

}