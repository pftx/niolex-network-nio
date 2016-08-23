/**
 * RpcClientFactoryTest.java
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.List;

import org.apache.niolex.address.client.Consumer;
import org.apache.niolex.address.rpc.RpcInterface;
import org.apache.niolex.address.rpc.cli.RpcClientFactory.StubBuilder;
import org.apache.niolex.address.rpc.svr.RpcServerMain;
import org.apache.niolex.commons.bean.MutableOne;
import org.apache.niolex.commons.reflect.FieldUtil;
import org.apache.niolex.network.rpc.anno.RpcMethod;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2014-1-27
 */
public class RpcClientFactoryTest {

    private static RpcClientFactoryMock factory;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // configure ZK
        System.setProperty("zk.cluster.address", "localhost:9181");
        System.setProperty("zk.session.timeout", "30000");
        System.setProperty("zk.root", "find");
        factory = new RpcClientFactoryMock("redis-client", "abcde");
        // Let's start!
        if (!factory.connectToZK()) {
            System.out.println("Failed to connect to ZK.");
            System.exit(-1);
        }
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        factory.disconnectFromZK();
    }

    @Test
    public void testRpcClientFactory() throws Exception {
        BaseStub<DemoFace> poolInternal = factory.newBuilder(DemoFace.class).buildPool();
        BaseMock<DemoFace> pool = (BaseMock<DemoFace>)poolInternal;
        MutableOne<List<String>> one = pool.mutableOne;
        List<String> list = one.data();
        assertEquals(2, list.size());
        System.out.println(one.data());
    }

    @Test
    public void testRpcClientFactoryStringString() throws Exception {
        BaseStub<DemoFace> poolInternal = factory.newBuilder(DemoFace.class).state("A").poolSize(0).buildPool();
        BaseMock<DemoFace> pool = (BaseMock<DemoFace>)poolInternal;
        MutableOne<List<String>> one = pool.mutableOne;
        List<String> list = one.data();
        assertEquals(3, list.size());
        System.out.println(one.data());
    }

    @Test
    public void testConnectToZK() throws Exception {
        RpcClientFactory factory = new RpcClientFactory("redis-client", "abcde");
        factory.setZkClusterAddress("oeoifj.ije.asi.com:3344");
        assertFalse(factory.connectToZK());
        factory.disconnectFromZK();
        factory.disconnectFromZK();
    }

    @Test
    public void testDisconnectFromZK() throws Exception {
        RpcServerMain.main(null);
        ClientPoolMain.main(new String[0]);
        ClientRetryMain.main(new String[0]);
        RpcServerMain.stop();
    }

    @Test
    public void testGetZkClusterAddress() throws Exception {
    }

    @Test
    public void testSetZkClusterAddress() throws Exception {
        factory.setZkClusterAddress("haha:8900");
        assertEquals("haha:8900", factory.getZkClusterAddress());
    }

    @Test
    public void testSetZkEnvironment() throws Exception {
        factory.setZkEnvironment("ddvv");
        assertEquals("ddvv", factory.getZkEnvironment());
    }

    @Test
    public void testSetZkSessionTimeout() throws Exception {
        factory.setZkSessionTimeout(30302);
        assertEquals(30302, factory.getZkSessionTimeout());
    }

    @Test
    public void testSetZkUserName() throws Exception {
        factory.setZkUserName("lex-client");
        assertEquals("lex-client", factory.getZkUserName());
    }

    @Test
    public void testSetZkPassword() throws Exception {
        factory.setZkPassword("LoveIt.");
        assertEquals("LoveIt.", factory.getZkPassword());
    }

    @Test
    public void testNewBuilder() throws Exception {
        StubBuilder<DemoFace> builder = factory.newBuilder(DemoFace.class);

        builder.serviceName("abc").version("3-100").version("1.0.3.304").state("default").poolSize(-3);
        builder.buildStub();
    }

}

class RpcClientFactoryMock extends RpcClientFactory {

    /**
     * Constructor
     * @param zkUserName
     * @param zkPassword
     */
    public RpcClientFactoryMock(String zkUserName, String zkPassword) {
        super(zkUserName, zkPassword);
    }

    /**
     * This is the override of super method.
     * @see org.apache.niolex.address.rpc.cli.RpcClientFactory#getPool(java.lang.Class, java.lang.String, java.lang.String, java.lang.String, int)
     */
    @Override
    public <T> BaseMock<T> getPool(Class<T> interfaze, String serviceName, String version, String state, int poolSize) {
        Consumer zkConsumer = FieldUtil.getValue(this, "zkConsumer");
        MutableOne<List<String>> mutableOne = zkConsumer.getAddressList(serviceName, version, state);
        return new BaseMock<T>(interfaze, mutableOne);
    }

    @Override
    public <T> BaseStub<T> getStub(Class<T> interfaze, String serviceName, String version, String state) {
        MutableOne<List<String>> mutableOne = new MutableOne<List<String>>();
        return new BaseMock<T>(interfaze, mutableOne);
    }

}

@RpcInterface(serviceName="test.DemoFace", version="1.0.0.1")
interface DemoFace {

    @RpcMethod(1)
    public int calc(int ...arr);

    @RpcMethod(2)
    public String getTime();

    @RpcMethod(3)
    public void tick(int tk);
}