/**
 * AddressUtilTest.java
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
package org.apache.niolex.address.rpc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.apache.niolex.address.rpc.cli.NodeInfo;
import org.apache.niolex.address.rpc.svr.RpcExpose;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2014-1-27
 */
public class AddressUtilTest extends AddressUtil {

    private static int ip = 1;
    private static int port = 1000;

    public static List<String> makeAddress() {
        List<String> add = new ArrayList<String>();
        add.add("rtp#10.1.2." + (ip++) + ":" + (port++) + "#2");
        add.add("rtp#10.1.2." + (ip++) + ":" + (port++) + "#3");
        return add;
    }

    @Test
    public void testGenerateAddress() throws Exception {
        RpcExpose ee = new RpcExpose(DemoService.class, "bsp", "abc", 3);
        ee.build();
        String s = generateAddress(ee, 10010);
        System.out.println(" xxx " + s);
        NodeInfo info = parseAddress(s);
        assertEquals("network/json", info.getProtocol());
    }

    @Test
    public void testParseAddress() throws Exception {
        NodeInfo info = parseAddress("json^rpc#10.254.2.10:8090#368#10100001010");
        assertEquals("/10.254.2.10:8090", info.getAddress().toString());
        assertEquals("json/rpc", info.getProtocol());
    }

    @Test
    public void testMakeNodeInfo() throws Exception {
        assertNull(parseAddress("rpc#10.256.2.10:8090#3"));
        assertNull(parseAddress("rpc#10.12.2.10:8090"));
    }

}
