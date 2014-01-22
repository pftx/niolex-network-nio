/**
 * PoolHandlerBalanceTest.java
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.niolex.commons.concurrent.ConcurrentUtil;
import org.apache.niolex.commons.util.Runner;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2014-1-14
 */
public class PoolHandlerBalanceTest {

    private PoolHandler<RpcServiceHandler> a;

    public void setup1() throws Throwable {
        List<RpcServiceHandler> listHandlers = new ArrayList<RpcServiceHandler>();
        listHandlers.add(new RpcServiceHandler("1", new A("1"), 20, true));
        listHandlers.add(new RpcServiceHandler("2", new A("2"), 28, true));
        listHandlers.add(new RpcServiceHandler("3", new A("3"), 17, true));
        listHandlers.add(new RpcServiceHandler("4", new A("4"), 5000, true));
        listHandlers.add(new RpcServiceHandler("5", new A("5"), 20, true));
        listHandlers.add(new RpcServiceHandler("6", new A("6"), 28, true));
        listHandlers.add(new RpcServiceHandler("7", new A("7"), 17, true));
        listHandlers.add(new RpcServiceHandler("8", new A("8"), 5000, true));
        a = new PoolHandler<RpcServiceHandler>(listHandlers, 2);
        listHandlers.clear();
        a.logDebug = false;
    }

    public void setup2() throws Throwable {
        List<RpcServiceHandler> listHandlers = new ArrayList<RpcServiceHandler>();
        listHandlers.add(new RpcServiceHandler("1", new A("1"), 20, true));
        listHandlers.add(new RpcServiceHandler("2", new A("2"), 28, true));
        listHandlers.add(new RpcServiceHandler("3", new A("3"), 17, true));
        listHandlers.add(new RpcServiceHandler("4", new A("4"), 5000, true));
        listHandlers.add(new RpcServiceHandler("5", new A("5"), 20, true));
        listHandlers.add(new RpcServiceHandler("6", new A("6"), 28, true));
        listHandlers.add(new RpcServiceHandler("7", new A("7"), 17, true));
        listHandlers.add(new RpcServiceHandler("8", new A("8"), 5000, true));
        listHandlers.add(new RpcServiceHandler("9", new A("9"), 10000, false));
        listHandlers.add(new RpcServiceHandler("0", new A("0"), 10000, false));
        a = new PoolHandler<RpcServiceHandler>(listHandlers, 2);
        listHandlers.clear();
        a.logDebug = false;
    }

    @Test
    public void testInvokeBlance() throws Throwable {
        setup1();
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
            Assert.assertTrue("Must relative in 100", k.intValue() > 1000);
            Assert.assertTrue("Must relative in 100", k.intValue() < 4000);
        }
    }

    @Test
    public void testErrorBlance() throws Throwable {
        setup2();
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
            Assert.assertTrue("Must relative in 100", k.intValue() > 1000);
            Assert.assertTrue("Must relative in 100", k.intValue() < 4000);
        }
    }

    public void invokeBlance(ConcurrentHashMap<String, AtomicInteger> m) throws Throwable {
        for (int i = 0; i < 4000; ++i) {
            AtomicInteger t = ConcurrentUtil.initMap(m, a.invoke(a, null, null).toString(), new AtomicInteger(0));
            t.incrementAndGet();
        }
    }

}
