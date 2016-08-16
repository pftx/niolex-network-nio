/**
 * MultiplexPoolHandlerTest.java
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
package org.apache.niolex.address.rpc.cli.pool;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.niolex.commons.reflect.FieldUtil;
import org.apache.niolex.network.IClient;
import org.apache.niolex.network.rpc.cli.BaseInvoker;
import org.apache.niolex.network.rpc.cli.RpcStub;
import org.apache.niolex.network.rpc.conv.JsonConverter;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2014-1-22
 */
public class MultiplexPoolHandlerTest {

    @Test
    public void testMultiplexPoolHandler() throws Exception {
        RpcStub handler = makeHandler();
        MultiplexPoolHandler han = new MultiplexPoolHandler(Collections.singleton(handler), 2, 2);
        han.setWaitTimeout(1);
        assertEquals(handler, han.take());
        assertEquals(handler, han.take());
        assertNull(han.take());
    }

    @Test
    public void testTake() throws Exception {
        RpcStub handler = makeHandler();
        MultiplexPoolHandler han = new MultiplexPoolHandler(Collections.singleton(handler), 2, 1);
        han.setWaitTimeout(1);
        assertEquals(handler, han.take());
        assertNull(han.take());
    }

    @Test
    public void testAddMultiplex() throws Exception {
        RpcStub handler = makeHandler(0);
        MultiplexPoolHandler han = new MultiplexPoolHandler(Collections.singleton(handler), 2, 3);
        han.addMultiplex();//2
        han.addMultiplex();//3
        han.addMultiplex();//3, return immediately
        Integer currentMultiplex = FieldUtil.getValue(han, "currentMultiplex");
        assertEquals(3, currentMultiplex.intValue());
        LinkedBlockingQueue<RpcStub> readyQueue = FieldUtil.getValue(han, "readyQueue");
        assertEquals(1, readyQueue.size());
    }

    @Test
    public void testRepair() throws Exception {
        RpcStub handler1 = makeHandler(2);
        RpcStub handler2 = makeHandler(0);

        MultiplexPoolHandler han = new MultiplexPoolHandler(Collections.singleton(handler2), 2, 2);
        han.repair(handler1);
        han.repair(handler2);
        LinkedBlockingQueue<RpcStub> readyQueue = FieldUtil.getValue(han, "readyQueue");
        assertEquals(2, readyQueue.size());
        assertEquals(handler2, readyQueue.take());
        assertEquals(handler1, readyQueue.take());
    }

    @Test
    public void testOffer() throws Exception {
        RpcStub handler1 = makeHandler(2);
        RpcStub handler2 = makeHandler(0);

        MultiplexPoolHandler han = new MultiplexPoolHandler(Collections.singleton(handler1), 2, 2);
        han.offer(handler1);
        han.offer(handler2);
        LinkedBlockingQueue<RpcStub> readyQueue = FieldUtil.getValue(han, "readyQueue");
        assertEquals(2, readyQueue.size());
        assertEquals(handler1, readyQueue.take());
        assertEquals(handler1, readyQueue.take());
    }

    public void travelPool(MultiplexPoolHandler han) {
        RpcStub handler = han.take();
        if (handler == null) return;
        han.offer(handler);
        for (int i = 0; i < 100; ++i) {
            RpcStub h = han.take();
            if (h == null) return;
            han.offer(h);
            if (h == handler) {
                break;
            }
        }
    }

    public RpcStub makeHandler() {
        return makeHandler(3);
    }

    public RpcStub makeHandler(int retry) {
        BaseInvoker cli = mock(BaseInvoker.class);
        when(cli.getConnectRetryTimes()).thenReturn(retry);
        when(cli.isReady()).thenReturn(true);
        return new RpcStub(cli, new JsonConverter());
    }

    @Test
    public void testAddNew() throws Exception {
        RpcStub handler = makeHandler();
        MultiplexPoolHandler han = new MultiplexPoolHandler(Collections.singleton(handler), 2, 3);
        han.setWaitTimeout(1);
        travelPool(han);
        ArrayList<RpcStub> list = new ArrayList<RpcStub>();
        list.add(makeHandler());
        list.add(makeHandler());
        list.add(makeHandler());
        han.addNew(list);
        travelPool(han);
        ArrayList<RpcStub> backupHandlers = FieldUtil.getValue(han, "backupHandlers");
        assertEquals(4, backupHandlers.size());
        LinkedBlockingQueue<RpcStub> readyQueue = FieldUtil.getValue(han, "readyQueue");
        assertEquals(4, readyQueue.size());
        RpcStub h1 = han.take();
        RpcStub h2 = han.take();
        RpcStub h3 = han.take();
        RpcStub h4 = han.take();
        travelPool(han);
        han.offer(h1);
        han.offer(h2);
        han.offer(h3);
        han.offer(h4);
        travelPool(han);
        ArrayList<RpcStub> list2 = new ArrayList<RpcStub>();
        list2.add(makeHandler());
        han.addNew(list2);
        assertEquals(5, backupHandlers.size());
        assertEquals(10, readyQueue.size());
        han.destroy();
    }

    public RpcStub makeHandler2() throws IOException {
        BaseInvoker cli = new BaseInvoker(mock(IClient.class));
        cli.connect();
        cli.setConnectRetryTimes(5);
        return new RpcStub(cli, new JsonConverter());
    }

    @Test
    public void testDestroy() throws Exception {
        ArrayList<RpcStub> list = new ArrayList<RpcStub>();
        list.add(makeHandler2());
        list.add(makeHandler2());
        list.add(makeHandler2());
        MultiplexPoolHandler han = new MultiplexPoolHandler(list, 2, 3);
        han.setWaitTimeout(1);
        LinkedBlockingQueue<RpcStub> readyQueue = FieldUtil.getValue(han, "readyQueue");
        ArrayList<RpcStub> backupHandlers = FieldUtil.getValue(han, "backupHandlers");
        travelPool(han);
        han.destroy();
        travelPool(han);
        assertNull(han.take());
        assertNull(han.take());
        assertEquals(3, backupHandlers.size());
        assertEquals(0, readyQueue.size());
    }

}
