/**
 * MultiAddressClientTest.java
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
package org.apache.niolex.rpc.client;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.niolex.commons.test.MockUtil;
import org.apache.niolex.commons.test.MultiPerformance;
import org.apache.niolex.rpc.core.CoreTest;
import org.apache.niolex.rpc.demo.RpcService;
import org.apache.niolex.rpc.demo.RpcService.IntArray;
import org.apache.niolex.rpc.protocol.JsonProtocol;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5, $Date: 2012-12-4$
 */
public class MultiAddressClientTest {

    static {
        CoreTest.startSvc();
    }

    /**
     * Test method for {@link org.apache.niolex.rpc.client.NioClient#run()}.
     * @throws IOException
     */
    @Test
    public void testRun() throws IOException {
        MultiAddressClient cli = new MultiAddressClient("localhost:9909/3; localhost:9909/5; localhost:9909/2");
        RpcProxy rpc = new RpcProxy(cli, new JsonProtocol());
        rpc.connect();
        final RpcService service = rpc.getService(RpcService.class);
        MultiPerformance perf = new MultiPerformance(10, 10, 300){
            @Override
            protected void run() {
                int a = MockUtil.ranInt(10240);
                int b = MockUtil.ranInt(10240);
                int c = MockUtil.ranInt(10240);
                assertEquals(a + b + c, service.sum(new IntArray(a, b, c)).i);
            }
        };
        System.out.print("10 connections ");
        perf.start();
        rpc.stop();
    }

    /**
     * Test method for {@link org.apache.niolex.rpc.client.NioClient#connect()}.
     */
    @Test
    public void testConnect() throws IOException {
        MultiAddressClient cli = new MultiAddressClient("localhost:9909/1");
        RpcProxy rpc = new RpcProxy(cli, new JsonProtocol());
        rpc.setConnectTimeout(100);
        rpc.connect();
        final RpcService service = rpc.getService(RpcService.class);
        MultiPerformance perf = new MultiPerformance(10, 10, 300){
            @Override
            protected void run() {
                int a = MockUtil.ranInt(10240);
                int b = MockUtil.ranInt(10240);
                int c = MockUtil.ranInt(10240);
                assertEquals(a + b + c, service.sum(new IntArray(a, b, c)).i);
            }
        };
        System.out.print("1 connection ");
        perf.start();
        rpc.stop();
    }

}
