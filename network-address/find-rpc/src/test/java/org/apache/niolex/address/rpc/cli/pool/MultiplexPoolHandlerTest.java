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

import java.util.Collections;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.niolex.commons.reflect.FieldUtil;
import org.apache.niolex.network.cli.RpcClientHandler;
import org.apache.niolex.network.rpc.RpcClient;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2014-1-22
 */
public class MultiplexPoolHandlerTest {

    @Test
    public void testMultiplexPoolHandler() throws Exception {
        RpcClient cli = mock(RpcClient.class);
        when(cli.getConnectRetryTimes()).thenReturn(3);
        when(cli.isValid()).thenReturn(true);
        RpcClientHandler handler = new RpcClientHandler("test", cli);
        MultiplexPoolHandler han = new MultiplexPoolHandler(Collections.singleton(handler), 2, 2);
        han.setWaitTimeout(1);
        assertEquals(handler, han.take());
        assertNotEquals(handler, han.take());
        assertNull(han.take());
    }

    @Test
    public void testTake() throws Exception {
        RpcClient cli = mock(RpcClient.class);
        when(cli.getConnectRetryTimes()).thenReturn(3);
        when(cli.isValid()).thenReturn(true);
        RpcClientHandler handler = new RpcClientHandler("test", cli);
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
        RpcClientHandler handler = new RpcClientHandler("test", cli);
        MultiplexPoolHandler han = new MultiplexPoolHandler(Collections.singleton(handler), 2, 3);
        han.addMultiplex();//2
        han.addMultiplex();//3
        han.addMultiplex();//3, return immediately
        int currentMultiplex = FieldUtil.getValue(han, "currentMultiplex");
        assertEquals(3, currentMultiplex);
        LinkedBlockingQueue<RpcClientHandler> readyQueue = FieldUtil.getValue(han, "readyQueue");
        assertEquals(1, readyQueue.size());
    }

    @Test
    public void testRepair() throws Exception {
        RpcClient cli1 = mock(RpcClient.class);
        when(cli1.getConnectRetryTimes()).thenReturn(2);
        when(cli1.isValid()).thenReturn(true);
        RpcClientHandler handler1 = new RpcClientHandler("test1", cli1);
        RpcClient cli2 = mock(RpcClient.class);
        when(cli2.getConnectRetryTimes()).thenReturn(0);
        when(cli2.isValid()).thenReturn(true);
        RpcClientHandler handler2 = new RpcClientHandler("test2", cli2);
        MultiplexPoolHandler han = new MultiplexPoolHandler(Collections.singleton(handler1), 2, 2);
        han.repair(handler1);
        han.repair(handler2);
        LinkedBlockingQueue<RpcClientHandler> readyQueue = FieldUtil.getValue(han, "readyQueue");
        assertEquals(2, readyQueue.size());
    }

    @Test
    public void testOffer() throws Exception {
        RpcClient cli1 = mock(RpcClient.class);
        when(cli1.getConnectRetryTimes()).thenReturn(2);
        when(cli1.isValid()).thenReturn(true);
        RpcClientHandler handler1 = new RpcClientHandler("test1", cli1);
        RpcClient cli2 = mock(RpcClient.class);
        when(cli2.getConnectRetryTimes()).thenReturn(0);
        when(cli2.isValid()).thenReturn(true);
        RpcClientHandler handler2 = new RpcClientHandler("test2", cli2);
        MultiplexPoolHandler han = new MultiplexPoolHandler(Collections.singleton(handler1), 2, 2);
        han.offer(handler1);
        han.offer(handler2);
        LinkedBlockingQueue<RpcClientHandler> readyQueue = FieldUtil.getValue(han, "readyQueue");
        assertEquals(2, readyQueue.size());
    }

    @Test
    public void testIsClosed() throws Exception {
        RpcClient cli1 = mock(RpcClient.class);
        when(cli1.getConnectRetryTimes()).thenReturn(2);
        when(cli1.isValid()).thenReturn(true);
        RpcClientHandler handler1 = new RpcClientHandler("test1", cli1);
        RpcClient cli2 = mock(RpcClient.class);
        when(cli2.getConnectRetryTimes()).thenReturn(0);
        when(cli2.isValid()).thenReturn(true);
        MultiplexPoolHandler han = new MultiplexPoolHandler(Collections.singleton(handler1), 2, 2);
        assertTrue(han.isClosed(cli2));
        assertFalse(han.isClosed(cli1));
    }

}
