/**
 * BaseStubTest.java
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
package org.apache.niolex.address.rpc.cli;


import static org.apache.niolex.address.rpc.AddressUtilTest.*;
import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.niolex.address.rpc.AddressUtil;
import org.apache.niolex.commons.bean.MutableOne;
import org.apache.niolex.network.demo.json.RpcService;
import org.apache.niolex.network.rpc.RpcClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2014-1-23
 */
public class BaseStubTest {

    MutableOne<List<String>> mutableOne = new MutableOne<List<String>>();
    private BaseMock stub;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        stub = new BaseMock(RpcService.class, mutableOne);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testBaseStubAdd() throws Exception {
        List<String> data = makeAddress();
        assertEquals(0, stub.readySet.size());
        mutableOne.updateData(data);
        assertEquals(2, stub.readySet.size());
        mutableOne.updateData(makeAddress());
        assertEquals(2, stub.readySet.size());
    }

    @Test
    public void testOnDataChange() throws Exception {
        List<String> data = makeAddress();
        data.add("not yet implemented");
        assertEquals(0, stub.readySet.size());
        mutableOne.updateData(data);
        assertEquals(2, stub.readySet.size());
        data.addAll(makeAddress());
        mutableOne.updateData(data);
        assertEquals(4, stub.readySet.size());
    }

    @Test(expected=IllegalStateException.class)
    public void testGetService() throws Exception {
        stub.getService();
    }

    @Test
    public void testClientSet() throws Exception {
        NodeInfo info = AddressUtil.parseAddress("rpc#10.254.2.10:8090#3");
        Set<RpcClient> set = stub.clientSet(info);
        assertEquals(0, set.size());
        assertEquals("rpc://10.254.2.10:8090#3", info.toString());
    }

    @Test
    public void testMarkDeleted() throws Exception {
        List<String> data = makeAddress();
        assertEquals(0, stub.readySet.size());
        mutableOne.updateData(data);
        assertEquals(2, stub.readySet.size());
        stub.isWorking = true;
        data.addAll(makeAddress());
        mutableOne.updateData(data);
        assertEquals(2, stub.readySet.size());
        assertEquals(0, stub.delCnt);
        mutableOne.updateData(makeAddress());
        assertEquals(2, stub.readySet.size());
        assertEquals(1, stub.delCnt);
    }

    @Test
    public void testMarkNew() throws Exception {
        List<String> data = makeAddress();
        data.add("not yet implemented");
        assertEquals(0, stub.readySet.size());
        stub.isWorking = true;
        mutableOne.updateData(data);
        assertEquals(0, stub.readySet.size());
        data.addAll(makeAddress());
        mutableOne.updateData(data);
        assertEquals(0, stub.readySet.size());
        assertEquals(2, stub.newCnt);
    }

    @Test
    public void testBuild() throws Exception {
        List<String> data = makeAddress();
        assertEquals(0, stub.readySet.size());
        mutableOne.updateData(data);
        assertEquals(2, stub.readySet.size());
        stub.isWorking = true;
        data.remove(1);
        mutableOne.updateData(data);
        assertEquals(2, stub.readySet.size());
        assertEquals(1, stub.delCnt);
        assertEquals(0, stub.newCnt);
    }

    @Test
    public void testDestroy() throws Exception {
        stub.isWorking = true;
        stub.getService();
    }

    @Test
    public void testGetConnectTimeout() throws Exception {
        assertEquals(3000, stub.getConnectTimeout());
    }

    @Test
    public void testSetConnectTimeout() throws Exception {
        stub.setConnectTimeout(6700);
        assertEquals(6700, stub.getConnectTimeout());
    }

    @Test
    public void testGetConnectRetryTimes() throws Exception {
        assertEquals(100, stub.getConnectRetryTimes());
    }

    @Test
    public void testSetConnectRetryTimes() throws Exception {
        stub.setConnectRetryTimes(3030);
        assertEquals(3030, stub.getConnectRetryTimes());
    }

    @Test
    public void testGetConnectSleepBetweenRetry() throws Exception {
        assertEquals(1000, stub.getConnectSleepBetweenRetry());
    }

    @Test
    public void testSetConnectSleepBetweenRetry() throws Exception {
        stub.setConnectSleepBetweenRetry(300);
        assertEquals(300, stub.getConnectSleepBetweenRetry());
    }

    @Test
    public void testGetRpcTimeout() throws Exception {
        assertEquals(3000, stub.getRpcTimeout());
    }

    @Test
    public void testSetRpcTimeout() throws Exception {
        stub.setRpcTimeout(6300);
        assertEquals(6300, stub.getRpcTimeout());
    }

    @Test
    public void testGetRpcErrorRetryTimes() throws Exception {
        assertEquals(3, stub.getRpcErrorRetryTimes());
    }

    @Test
    public void testSetRpcErrorRetryTimes() throws Exception {
        stub.setRpcErrorRetryTimes(3050);
        assertEquals(3050, stub.getRpcErrorRetryTimes());
    }

}

class BaseMock extends BaseStub<RpcService> {
    int newCnt, delCnt;

    /**
     * Constructor
     * @param interfaze
     * @param mutableOne
     */
    public BaseMock(Class<RpcService> interfaze, MutableOne<List<String>> mutableOne) {
        super(interfaze, mutableOne);
    }

    /**
     * This is the override of super method.
     * @see org.apache.niolex.address.rpc.cli.BaseStub#markDeleted(java.util.HashSet)
     */
    @Override
    protected void markDeleted(HashSet<NodeInfo> delSet) {
        ++delCnt;
    }

    /**
     * This is the override of super method.
     * @see org.apache.niolex.address.rpc.cli.BaseStub#markNew(java.util.HashSet)
     */
    @Override
    protected void markNew(HashSet<NodeInfo> addSet) {
        ++newCnt;
    }

    /**
     * This is the override of super method.
     * @see org.apache.niolex.address.rpc.cli.BaseStub#build()
     */
    @Override
    public BaseStub<RpcService> build() {
        return null;
    }

    /**
     * This is the override of super method.
     * @see org.apache.niolex.address.rpc.cli.BaseStub#destroy()
     */
    @Override
    public void destroy() {

    }

}