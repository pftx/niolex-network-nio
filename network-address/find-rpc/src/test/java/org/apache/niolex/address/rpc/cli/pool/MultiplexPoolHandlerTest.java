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


import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.niolex.commons.reflect.FieldUtil;
import org.apache.niolex.network.IClient;
import org.apache.niolex.network.cli.RpcClientAdapter;
import org.apache.niolex.network.rpc.RpcClient;
import org.apache.niolex.network.rpc.SingleInvoker;
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
        RpcClientAdapter handler = makeHandler();
        MultiplexPoolHandler han = new MultiplexPoolHandler(Collections.singleton(handler), 2, 2);
        han.setWaitTimeout(1);
        assertEquals(handler, han.take());
        assertNotEquals(handler, han.take());
        assertNull(han.take());
    }

    @Test
    public void testTake() throws Exception {
        RpcClientAdapter handler = makeHandler();
        MultiplexPoolHandler han = new MultiplexPoolHandler(Collections.singleton(handler), 2, 1);
        han.setWaitTimeout(1);
        assertEquals(handler, han.take());
        assertNull(han.take());
    }

    @Test
    public void testAddMultiplex() throws Exception {
        RpcClient cli = mock(RpcClient.class);
        when(cli.getConnectRetryTimes()).thenReturn(0);
        when(cli.isValid()).thenReturn(true);
        RpcClientAdapter handler = new RpcClientAdapter("test", cli);
        MultiplexPoolHandler han = new MultiplexPoolHandler(Collections.singleton(handler), 2, 3);
        han.addMultiplex();//2
        han.addMultiplex();//3
        han.addMultiplex();//3, return immediately
        Integer currentMultiplex = FieldUtil.getValue(han, "currentMultiplex");
        assertEquals(3, currentMultiplex.intValue());
        LinkedBlockingQueue<RpcClientAdapter> readyQueue = FieldUtil.getValue(han, "readyQueue");
        assertEquals(1, readyQueue.size());
    }

    @Test
    public void testRepair() throws Exception {
        RpcClient cli1 = mock(RpcClient.class);
        when(cli1.getConnectRetryTimes()).thenReturn(2);
        when(cli1.isValid()).thenReturn(true);
        RpcClientAdapter handler1 = new RpcClientAdapter("test1", cli1);
        RpcClient cli2 = mock(RpcClient.class);
        when(cli2.getConnectRetryTimes()).thenReturn(0);
        when(cli2.isValid()).thenReturn(true);
        RpcClientAdapter handler2 = new RpcClientAdapter("test2", cli2);
        MultiplexPoolHandler han = new MultiplexPoolHandler(Collections.singleton(handler1), 2, 2);
        han.repair(handler1);
        han.repair(handler2);
        LinkedBlockingQueue<RpcClientAdapter> readyQueue = FieldUtil.getValue(han, "readyQueue");
        assertEquals(2, readyQueue.size());
    }

    @Test
    public void testOffer() throws Exception {
        RpcClient cli1 = mock(RpcClient.class);
        when(cli1.getConnectRetryTimes()).thenReturn(2);
        when(cli1.isValid()).thenReturn(true);
        RpcClientAdapter handler1 = new RpcClientAdapter("test1", cli1);
        RpcClient cli2 = mock(RpcClient.class);
        when(cli2.getConnectRetryTimes()).thenReturn(0);
        when(cli2.isValid()).thenReturn(true);
        RpcClientAdapter handler2 = new RpcClientAdapter("test2", cli2);
        MultiplexPoolHandler han = new MultiplexPoolHandler(Collections.singleton(handler1), 2, 2);
        han.offer(handler1);
        han.offer(handler2);
        LinkedBlockingQueue<RpcClientAdapter> readyQueue = FieldUtil.getValue(han, "readyQueue");
        assertEquals(2, readyQueue.size());
    }

    @Test
    public void testIsClosed() throws Exception {
        RpcClient cli1 = mock(RpcClient.class);
        when(cli1.getConnectRetryTimes()).thenReturn(2);
        when(cli1.isValid()).thenReturn(true);
        RpcClientAdapter handler1 = new RpcClientAdapter("test1", cli1);
        RpcClient cli2 = mock(RpcClient.class);
        when(cli2.getConnectRetryTimes()).thenReturn(0);
        when(cli2.isValid()).thenReturn(true);
        MultiplexPoolHandler han = new MultiplexPoolHandler(Collections.singleton(handler1), 2, 2);
        assertTrue(han.isClosed(cli2));
        assertFalse(han.isClosed(cli1));
    }

    @Test
    public void testTranslate() throws Exception {
        RpcClient cli1 = mock(RpcClient.class);
        List<RpcClientAdapter> list = MultiplexPoolHandler.translate(Collections.singleton(cli1), "http://go");
        assertEquals(1, list.size());
        assertEquals(cli1, list.get(0).getHandler());
    }

    public void travelPool(MultiplexPoolHandler han) {
        RpcClientAdapter handler = han.take();
        if (handler == null) return;
        han.offer(handler);
        for (int i = 0; i < 100; ++i) {
            RpcClientAdapter h = han.take();
            if (h == null) return;
            han.offer(h);
            if (h == handler) {
                break;
            }
        }
    }

    public RpcClientAdapter makeHandler() {
        RpcClient cli = mock(RpcClient.class);
        when(cli.getConnectRetryTimes()).thenReturn(3);
        when(cli.isValid()).thenReturn(true);
        return new RpcClientAdapter("test", cli);
    }

    @Test
    public void testAddNew() throws Exception {
        RpcClientAdapter handler = makeHandler();
        MultiplexPoolHandler han = new MultiplexPoolHandler(Collections.singleton(handler), 2, 3);
        han.setWaitTimeout(1);
        travelPool(han);
        ArrayList<RpcClientAdapter> list = new ArrayList<RpcClientAdapter>();
        list.add(makeHandler());
        list.add(makeHandler());
        list.add(makeHandler());
        han.addNew(list);
        travelPool(han);
        ArrayList<RpcClientAdapter> backupHandlers = FieldUtil.getValue(han, "backupHandlers");
        assertEquals(4, backupHandlers.size());
        LinkedBlockingQueue<RpcClientAdapter> readyQueue = FieldUtil.getValue(han, "readyQueue");
        assertEquals(4, readyQueue.size());
        RpcClientAdapter h1 = han.take();
        RpcClientAdapter h2 = han.take();
        RpcClientAdapter h3 = han.take();
        RpcClientAdapter h4 = han.take();
        travelPool(han);
        han.offer(h1);
        han.offer(h2);
        han.offer(h3);
        han.offer(h4);
        travelPool(han);
        ArrayList<RpcClientAdapter> list2 = new ArrayList<RpcClientAdapter>();
        list2.add(makeHandler());
        han.addNew(list2);
        assertEquals(5, backupHandlers.size());
        assertEquals(10, readyQueue.size());
        han.destroy();
    }

    public RpcClientAdapter makeHandler2() throws IOException {
        RpcClient cli = new RpcClient(mock(IClient.class), new SingleInvoker(), new JsonConverter());
        cli.connect();
        cli.setConnectRetryTimes(5);
        return new RpcClientAdapter("test", cli);
    }

    @Test
    public void testDestroy() throws Exception {
        ArrayList<RpcClientAdapter> list = new ArrayList<RpcClientAdapter>();
        list.add(makeHandler2());
        list.add(makeHandler2());
        list.add(makeHandler2());
        MultiplexPoolHandler han = new MultiplexPoolHandler(list, 2, 3);
        han.setWaitTimeout(1);
        LinkedBlockingQueue<RpcClientAdapter> readyQueue = FieldUtil.getValue(han, "readyQueue");
        ArrayList<RpcClientAdapter> backupHandlers = FieldUtil.getValue(han, "backupHandlers");
        travelPool(han);
        han.destroy();
        travelPool(han);
        assertNull(han.take());
        assertNull(han.take());
        assertEquals(3, backupHandlers.size());
        assertEquals(0, readyQueue.size());
    }

}
