/**
 * OldRetryHandlerTest.java
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
package org.apache.niolex.network.cli;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.niolex.commons.concurrent.ConcurrentUtil;
import org.apache.niolex.commons.reflect.MethodUtil;
import org.apache.niolex.commons.util.Runner;
import org.apache.niolex.network.rpc.RpcException;
import org.junit.Assert;
import org.junit.Test;


/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 *
 * @version @version@, $Date: 2011-9-15$
 *
 */
public class RetryHandlerBalanceTest {
    private static RetryHandler a;
    private static List<IServiceHandler> listHandlers = new ArrayList<IServiceHandler>(5);

    static {
    	listHandlers.addAll(RpcServiceHandlerTest.listHandlers);
    	a = new RetryHandler(listHandlers, 3, 10);
    	a.logDebug = false;
    }

    @Test
    public void testName() {
        System.out.println(" => " + a.toString());
        Assert.assertEquals(listHandlers, a.getHandlers());
    }

    @Test
    public void testInvokeBlance() throws Throwable {
    	ConcurrentHashMap<String, AtomicInteger> m = new ConcurrentHashMap<String, AtomicInteger>();
    	Thread t[] = new Thread[5];
        t[0] = Runner.run(this, "invokeBlance", m);
        t[1] = Runner.run(this, "invokeBlance", m);
        t[2] = Runner.run(this, "invokeBlance", m);
        t[3] = Runner.run(this, "invokeBlance", m);
        t[4] = Runner.run(this, "invokeBlance", m);
        for (int i = 0; i < 5; ++i) {
        	t[i].join();
        }
        System.out.println(m);
        for (AtomicInteger k : m.values()) {
            Assert.assertTrue("Must relative in 100", k.intValue() > 2400);
            Assert.assertTrue("Must relative in 100", k.intValue() < 2600);
        }
    }

    public void invokeBlance(ConcurrentHashMap<String, AtomicInteger> m) throws Throwable {
    	for (int i = 0; i < 2000; ++i) {
            AtomicInteger t = ConcurrentUtil.initMap(m, a.invoke(a, null, null).toString(), new AtomicInteger(0));
            t.incrementAndGet();
        }
    }

    @Test
    public void testErrorBlance() throws Throwable {
    	for (int i = 0; i < 4; ++i) {
    		if (listHandlers.get(i).toString().equals("8")) {
    			listHandlers.get(i).notReady(new IOException("For test"));
    			break;
    		}
    	}
        Map<String, Integer> m = new HashMap<String, Integer>();
        for (int i = 0; i < 7500; ++i) {
            String name = a.invoke(a, null, null).toString();
            Integer t = m.get(name);
            if (t == null)
                t = 0;
            t = t + 1;
            m.put(name, t);
        }
        System.out.println("ErrorBlance: " + m);
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
            a.invoke(a, m, null);
        }
    }

    @Test(expected=NoSuchMethodException.class)
    public void testErrorException() throws Throwable {
    	List<IServiceHandler> listHandlers = new ArrayList<IServiceHandler>();
        listHandlers.add(new RpcServiceHandler("5", new C(), 1000, true));
        listHandlers.add(new RpcServiceHandler("6", new C(), 1000, true));
        listHandlers.add(new RpcServiceHandler("8", new D(), 1000, true));
        RetryHandler a = new RetryHandler(listHandlers, 3, 10);
        Method m = MethodUtil.getMethods(B.class, "invoke")[0];
        String name = a.invoke(a, m, null).toString();
        System.out.println(name);
    }

    @Test(expected=RpcException.class)
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

    @Test(expected=RpcException.class)
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
    private final boolean pr;

    public B(String name) {
        this(name, false);
    }

    public B(String name, boolean pr) {
        super();
        this.name = name;
        this.pr = pr;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (pr) System.out.println("B invoke for: " + name);
        if (System.currentTimeMillis() % 2 == 0)
            throw new Exception("B", new SocketException("Sock"));
        return name;
    }

}

class C implements InvocationHandler {

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		throw new Exception("C", new SocketException("Sock"));
	}

}

class D implements InvocationHandler {

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		throw new NoSuchMethodException("D");
	}

}