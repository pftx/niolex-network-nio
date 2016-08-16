/**
 * RpcExposeTest.java
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
package org.apache.niolex.address.rpc.svr;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.niolex.address.rpc.DemoService;
import org.apache.niolex.address.rpc.RpcInterface;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2014-1-24
 */
public class RpcExposeTest {

    @Test
    public void testBasicQueueOp() throws Exception {
        LinkedBlockingQueue<Integer> readyQueue = new LinkedBlockingQueue<Integer>();
        Integer i = 838839;
        readyQueue.offer(i);
        readyQueue.offer(i);
        readyQueue.offer(i);
        assertEquals(3, readyQueue.size());
        assertEquals(i, readyQueue.take());
        assertEquals(2, readyQueue.size());
        assertEquals(i, readyQueue.take());
        assertEquals(1, readyQueue.size());
    }

    @Test
    public void testRpcExposeObject() throws Exception {
        RpcExpose exp = new RpcExpose("bsp");
        assertEquals("bsp", exp.getTarget());
    }

    @Test
    public void testBuild() throws Exception {
        RpcExpose exp = new RpcExpose("bsp");
        assertFalse(exp.build());
        assertEquals(Serializable.class, exp.getInterfaze());
        assertEquals(1, exp.getWeight());
    }

    @Test
    public void testBuildPartial() throws Exception {
        RpcExpose exp = new RpcExpose(DemoService.class, "bsp", "abc", 3);
        assertTrue(exp.build());
        assertEquals(DemoService.class, exp.getInterfaze());
        assertEquals("abc", exp.getState());
        assertEquals(3, exp.getWeight());
        assertEquals("org.apache.niolex.address.rpc.DemoService", exp.getServiceName());
        assertEquals("network/json", exp.getServiceType());
        assertEquals(10000001, exp.getVersion());
    }

    @Test
    public void testBuildFull() throws Exception {
        RpcExpose exp = new RpcExpose(DemoService.class, "bsp", "abc", 3);
        exp.setServiceType("mockmock");
        exp.serviceName = "ticktick";
        exp.version = 666;
        assertTrue(exp.build());
        assertEquals(DemoService.class, exp.getInterfaze());
        assertEquals("abc", exp.getState());
        assertEquals(3, exp.getWeight());
        assertEquals("ticktick", exp.getServiceName());
        assertEquals("mockmock", exp.getServiceType());
        assertEquals(666, exp.getVersion());
    }

    @Test
    public void testGetInterfaze() throws Exception {
        RpcExpose exp = new RpcExpose("bsp");
        exp.setInterfaze(String.class);
        assertEquals("not yet implemented".getClass(), exp.getInterfaze());
    }

    @Test
    public void testSetInterfaze() throws Exception {
        RpcExpose exp = new RpcExpose(String.class, "bsp", RpcExpose.DFT_STATE, 3);
        assertEquals("not yet implemented".getClass(), exp.getInterfaze());
    }

    @Test
    public void testGetTarget() throws Exception {
        RpcExpose exp = new RpcExpose(String.class, "bqp", RpcExpose.DFT_STATE, 3);
        assertEquals("bqp", exp.getTarget());
    }

    @Test
    public void testSetTarget() throws Exception {
        RpcExpose exp = new RpcExpose("bsp");
        exp.setTarget(345);
        assertEquals(new Integer(345), exp.getTarget());
    }

    @Test
    public void testGetState() throws Exception {
        RpcExpose exp = new RpcExpose("bsp");
        exp.setState("sa1");
        assertEquals("sa1", exp.getState());
    }

    @Test
    public void testSetState() throws Exception {
        RpcExpose exp = new RpcExpose(String.class, "bqp", "lex", 3);
        assertEquals("lex", exp.getState());
    }

    @Test
    public void testGetWeight() throws Exception {
        RpcExpose exp = new RpcExpose("bsp");
        exp.setWeight(64);
        assertEquals(64, exp.getWeight());
    }

    @Test
    public void testSetWeight() throws Exception {
        RpcExpose exp = new RpcExpose(String.class, "bqp", "lex", 3);
        assertEquals(3, exp.getWeight());
    }

    @Test
    public void testGetServiceName() throws Exception {
        RpcExpose exp = new RpcExpose("bsp");
        exp.setServiceName("org.apache.GoodService");
        assertEquals("org.apache.GoodService", exp.getServiceName());
    }

    @Test
    public void testSetServiceName() throws Exception {
        RpcExpose exp = new RpcExpose(Coverage.class, "bsp", "abc", 3);
        exp.setServiceType("mockmock");
        exp.version = 666;
        assertTrue(exp.build());
        assertEquals(Coverage.class, exp.getInterfaze());
        assertEquals("abc", exp.getState());
        assertEquals(3, exp.getWeight());
        assertEquals("tick", exp.getServiceName());
        assertEquals("mockmock", exp.getServiceType());
        assertEquals(666, exp.getVersion());
    }

    @Test
    public void testGetServiceType() throws Exception {
        RpcExpose exp = new RpcExpose(Coverage.class, "bsp", "abc", 3);
        exp.version = 666;
        assertTrue(exp.build());
        assertEquals(Coverage.class, exp.getInterfaze());
        assertEquals("abc", exp.getState());
        assertEquals(3, exp.getWeight());
        assertEquals("tick", exp.getServiceName());
        assertEquals("joke", exp.getServiceType());
        assertEquals(666, exp.getVersion());
    }

    @Test
    public void testSetServiceType() throws Exception {
        RpcExpose exp = new RpcExpose("bsp");
        exp.setServiceType("network/app");
        assertEquals("network/app", exp.getServiceType());
    }

    @Test
    public void testGetVersion() throws Exception {
        RpcExpose exp = new RpcExpose("bsp");
        exp.setVersion(6765);
        assertEquals(6765, exp.getVersion());
    }

    @Test
    public void testSetVersion() throws Exception {
        System.out.println("not yet implemented");
    }

}

@RpcInterface(serviceName="tick", serviceType="joke")
interface Coverage {
    public int tick();
}
