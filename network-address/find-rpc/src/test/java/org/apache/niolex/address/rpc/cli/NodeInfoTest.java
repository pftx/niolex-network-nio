/**
 * NodeInfoTest.java
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


import static org.junit.Assert.*;

import java.net.InetSocketAddress;

import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2014-1-23
 */
public class NodeInfoTest {

    @Test
    public void testHashCode() throws Exception {
        NodeInfo info = new NodeInfo();
        info.setAddress(new InetSocketAddress("localhost", 8998));
        assertNotEquals(new NodeInfo().hashCode(), info.hashCode());
        NodeInfo info2 = new NodeInfo();
        info2.setAddress(new InetSocketAddress("localhost", 9998));
        assertNotEquals(info2.hashCode(), info.hashCode());
    }

    @Test
    public void testGetProtocol() throws Exception {
        NodeInfo info = new NodeInfo();
        info.setProtocol("not yet implemented");
        assertEquals("not yet implemented", info.getProtocol());
    }

    @Test
    public void testSetProtocol() throws Exception {
        assertEquals(new NodeInfo().hashCode(), new NodeInfo().hashCode());
    }

    @Test
    public void testGetAddress() throws Exception {
        NodeInfo info = new NodeInfo();
        info.setAddress(new InetSocketAddress("localhost", 9998));
        assertEquals(new InetSocketAddress("localhost", 9998), info.getAddress());
    }

    @Test
    public void testSetAddress() throws Exception {
        NodeInfo info = new NodeInfo();
        info.setAddress(new InetSocketAddress("localhost", 8998));
        info.setProtocol("network/rpc");
        info.setWeight(56);
        NodeInfo info2 = new NodeInfo();
        info2.setAddress(new InetSocketAddress("localhost", 8998));
        assertEquals(info2.hashCode(), info.hashCode());
        assertEquals("network/rpc:/localhost/127.0.0.1:8998#56", info.toString());
    }

    @Test
    public void testGetWeight() throws Exception {
        NodeInfo info = new NodeInfo();
        info.setWeight(3);
        assertEquals(3, info.getWeight());
    }

    @Test
    public void testSetWeight() throws Exception {
        NodeInfo info = new NodeInfo();
        info.setAddress(new InetSocketAddress("localhost", 8998));
        NodeInfo info2 = new NodeInfo();
        info2.setAddress(new InetSocketAddress("localhost", 8998));
        assertEquals(info2, info);
    }

    @Test
    public void testEquals() throws Exception {
        assertNotEquals(new NodeInfo(), null);
        assertNotEquals(new NodeInfo(), new InetSocketAddress("localhost", 8998));
    }

    @Test
    public void testToString() throws Exception {
        assertEquals(new NodeInfo(), new NodeInfo());
        NodeInfo info = new NodeInfo();
        info.setAddress(new InetSocketAddress("localhost", 8998));
        assertNotEquals(new NodeInfo(), info);
        assertNotEquals(info, new NodeInfo());
        NodeInfo info2 = new NodeInfo();
        info2.setAddress(new InetSocketAddress("localhost", 9998));
        assertNotEquals(info2, info);
        assertEquals(info, info);
    }

}
