/**
 * SimplePoolTest.java
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


import static org.apache.niolex.address.rpc.AddressUtilTest.makeAddress;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.niolex.address.rpc.DemoService;
import org.apache.niolex.address.rpc.cli.NodeInfo;
import org.apache.niolex.address.rpc.cli.RpcStubPool;
import org.apache.niolex.commons.bean.MutableOne;
import org.apache.niolex.commons.reflect.FieldUtil;
import org.apache.niolex.network.IClient;
import org.apache.niolex.network.demo.json.DemoJsonRpcServer;
import org.apache.niolex.network.rpc.cli.BaseInvoker;
import org.apache.niolex.network.rpc.cli.RpcStub;
import org.apache.niolex.network.rpc.util.RpcUtil;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2014-1-23
 */
public class SimplePoolTest {

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        DemoJsonRpcServer.main(null);
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        DemoJsonRpcServer.stop();
    }

    private MutableOne<List<String>> mutableOne = new MutableOne<List<String>>();
    private TestPool pool;
    private Set<NodeInfo> readySet;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        pool = new TestPool(mutableOne);
        FieldUtil.setValue(pool, "isWorking", true);
        ArrayList<RpcStub> empty = new ArrayList<RpcStub>();
        FieldUtil.setValue(pool, "poolHandler", new MultiplexPoolHandler(empty, 2, 2));
        readySet = FieldUtil.getValue(pool, "readySet");
    }

    @Test
    public void testMarkDeleted() throws Exception {
        List<String> data = makeAddress();
        FieldUtil.setValue(pool, "isWorking", false);
        mutableOne.updateData(data);
        FieldUtil.setValue(pool, "isWorking", true);
        mutableOne.updateData(makeAddress());
        mutableOne.updateData(data);
        assertEquals(2, readySet.size());
        // Check the backed queue
        LinkedBlockingQueue<RpcStub> readyQueue = FieldUtil.getValue(pool.poolHandler, "readyQueue");
        int delc = 0, addc = 0;
        for (RpcStub cli : readyQueue) {
            if (RpcUtil.isInUse(cli))
                ++addc;
            else
                ++delc;
        }
        assertEquals(5, addc);
        assertEquals(5, delc);
    }

    @Test
    public void testMarkNew() throws Exception {
        List<String> data = makeAddress();
        FieldUtil.setValue(pool, "isWorking", false);
        mutableOne.updateData(data);
        FieldUtil.setValue(pool, "isWorking", true);
        mutableOne.updateData(makeAddress());
        mutableOne.updateData(data);
        mutableOne.updateData(makeAddress());
        assertEquals(2, readySet.size());
        // Check the backed queue
        LinkedBlockingQueue<RpcStub> readyQueue = FieldUtil.getValue(pool.poolHandler, "readyQueue");
        int delc = 0, addc = 0;
        for (RpcStub cli : readyQueue) {
            if (RpcUtil.isInUse(cli))
                ++addc;
            else
                ++delc;
        }
        assertEquals(5, addc);
        assertEquals(10, delc);
    }

    @Test
    public void testDestroy() throws Exception {
        pool.destroy();
        pool.destroy();//Already false
    }

    @Test
    public void testSimplePool() throws Exception {
        FieldUtil.setValue(pool, "isWorking", false);
        mutableOne.updateData(makeAddress());
        pool.build();
        pool.build();//Already true
    }

    @Test
    public void testBuildClients() throws Exception {
        FieldUtil.setValue(pool, "isWorking", false);
        FieldUtil.setValue(pool, "poolSize", 0);
        mutableOne.updateData(makeAddress());
        pool.build();
        pool.build();//Already true
    }

    @Test
    public void testBuild() throws Exception {
        NodeInfo info = new NodeInfo();
        info.setProtocol("abbcc");
        pool.buildSuperClients(info);
    }

    @Test
    public void testReallyBuild() throws Exception {
        NodeInfo info = new NodeInfo();
        info.setProtocol("network/json");
        InetSocketAddress address = new InetSocketAddress("localhost", 8808);
        info.setAddress(address);
        info.setWeight(1);
        FieldUtil.setValue(pool, "weightShare", 0.6);
        pool.buildSuperClients(info);
        Set<RpcStub> clientSet = RpcStubPool.getPool().getClients(info.getAddress());
        assertEquals(1, clientSet.size());
        RpcStub cli = clientSet.iterator().next();
        assertTrue(cli.isReady());
        cli.stop();
    }

    @Test
    public void testReallyBuildError() throws Exception {
        NodeInfo info = new NodeInfo();
        info.setProtocol("network/json");
        InetSocketAddress address = new InetSocketAddress("localhost", 8809);
        info.setAddress(address);
        info.setWeight(1);
        FieldUtil.setValue(pool, "weightShare", 0.6);
        pool.buildSuperClients(info);
        Set<RpcStub> clientSet = RpcStubPool.getPool().getClients(info.getAddress());
        assertEquals(0, clientSet.size());
    }

}

class TestPool extends SimplePool<DemoService> {

    public TestPool(MutableOne<List<String>> mutableOne) {
        super(10, DemoService.class, mutableOne);
    }

    /**
     * This is the override of super method.
     * @see org.apache.niolex.address.rpc.cli.pool.SimplePool#buildClients(org.apache.niolex.address.rpc.cli.NodeInfo)
     */
    @Override
    protected Set<RpcStub> buildClients(NodeInfo info) {
        Set<RpcStub> clientSet = RpcStubPool.getPool().getClients(info.getAddress());
        final int curMax = info.getWeight();
        for (int i = 0; i < curMax; ++i) {
            clientSet.add(new MockClient());
        }
        return clientSet;
    }

    protected void buildSuperClients(NodeInfo info) {
        super.buildClients(info);
    }

}

class MockClient extends RpcStub {

    public MockClient() {
        super(new BaseInvoker(mock(IClient.class)), null);
    }

    /**
     * This is the override of super method.
     * 
     * @see org.apache.niolex.network.rpc.RpcStub#connect()
     */
    @Override
    public void connect() throws IOException {
    }

    /**
     * This is the override of super method.
     * 
     * @see org.apache.niolex.network.rpc.RpcStub#isReady()
     */
    @Override
    public boolean isReady() {
        return true;
    }

}
