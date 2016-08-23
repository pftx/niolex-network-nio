/**
 * RpcServiceHandlerTest.java
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
package org.apache.niolex.network.cli.handler;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.niolex.commons.concurrent.ThreadUtil;
import org.apache.niolex.commons.reflect.FieldUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 *
 * @version @version@, $Date: 2011-9-15$
 *
 */
public class RpcServiceHandlerTest {
	protected static List<IServiceHandler> listHandlers = new ArrayList<IServiceHandler>();

	static {
        listHandlers.add(new RpcServiceHandler("5", new A("5"), 40, true));
		listHandlers.add(new RpcServiceHandler("6", new A("6"), 28, true));
		listHandlers.add(new RpcServiceHandler("7", new A("7"), 17, true));
		listHandlers.add(new RpcServiceHandler("8", new A("8"), 5000, true));
	}

	@Test
	public void testRpcServiceHandlerName() {
		Assert.assertEquals("5", listHandlers.get(0).getServiceUrl());
		Assert.assertTrue(listHandlers.get(0).getHandler() instanceof A);
	}

    @Test
    public void testGetServiceUrlInitReady() {
        IServiceHandler a = new RpcServiceHandler("6", new A("67"), 20, false);
        Assert.assertFalse(a.isReady());
        FieldUtil.setValue(a, "nextWorkTime", System.currentTimeMillis() - 1);
        Assert.assertTrue(a.isReady());
        Assert.assertEquals("6", a.toString());
    }

	@Test
	public void testNotReady() {
		IServiceHandler a = listHandlers.get(0);
		Assert.assertTrue(a.isReady());
		a.notReady(new IOException("Lex"));
		Assert.assertFalse(a.isReady());
		ThreadUtil.sleep(50);
		Assert.assertTrue(a.isReady());
	}

	@Test
	public void testIsReady() {
		IServiceHandler a = new RpcServiceHandler("6", new A("67"), 20, false);
		Assert.assertFalse(a.isReady());
		FieldUtil.setValue(a, "nextWorkTime", System.currentTimeMillis() - 1);
		Assert.assertTrue(a.isReady());
		Assert.assertEquals("6", a.toString());
	}

	@Test
	public void testInvoke() throws Throwable {
		RpcServiceHandler a = (RpcServiceHandler)listHandlers.get(2);
		String s = a.invoke(a, null, null).toString();
		Assert.assertEquals("7", s);
	}

}

class A implements InvocationHandler {
	private final String name;
	private final boolean pr;

	public A(String name) {
	    this(name, false);
	}

	public A(String name, boolean pr) {
        super();
        this.name = name;
        this.pr = pr;
    }


    @Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (pr) System.out.println("A invoke for: " + name);
		return name;
	}

}
