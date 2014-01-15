/**
 * RpcConfigerTest.java
 *
 * Copyright 2010 Niolex, Inc.
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
package org.apache.niolex.network.cli.conf;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.niolex.network.cli.conf.RpcConfigBean;
import org.apache.niolex.network.cli.conf.RpcConfiger;
import org.junit.Test;


/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 *
 * @version @version@, $Date: 2010-10-15$
 *
 */
public class RpcConfigerTest {

    static RpcConfiger configer;

    static {
    	try {
			configer= new RpcConfiger("demo.properties");
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    @Test
    public void doConfig_Stand_Alone() {
        Map<String, RpcConfigBean> map = configer.getConfigs();
        RpcConfigBean conf = map.get("demo");
        assertEquals(conf.serverList[0], "dy-m-st01.niolex.net:8808");
        assertEquals(conf.serverList[2], "192.168.16.18:8808");
        assertEquals(conf.serviceUrl, "/cgi-bin/services/WdgetService.cgi");
        assertEquals(conf.rpcTimeout, 7000);
        assertEquals(conf.rpcErrorRetryTimes, 5);
        System.out.println(conf);
    }

    @Test
    public void doConfig_Extends_Super() {
        Map<String, RpcConfigBean> map = configer.getConfigs();
        RpcConfigBean conf = map.get("nio-acnt");
        assertEquals(conf.serverList[0], "http://dy-m-st01.niolex.net:8808");
        assertEquals(conf.serviceUrl, "/rpc/AcntService");
        assertEquals(conf.rpcTimeout, 12000);
    }

	@Test
	public void testRpcConfiger() throws Exception {
		byte[] buf = "serviceUrl=/cgi-bin/services/WdgetService.cgi".getBytes();
		InputStream ins = new ByteArrayInputStream(buf);
		RpcConfiger confi = new RpcConfiger(ins, "demo.properties");
		assertEquals("/cgi-bin/services/WdgetService.cgi", confi.getConfig().serviceUrl);
	}

    @Test
    public void testBaseConfiger() throws Exception {
        RpcConfiger configer = new RpcConfiger("guess.properties");
        RpcConfigBean conf = configer.getConfig("demo");
        assertEquals(conf.serverList[0], "dy-m-st01.niolex.net:8808");
        assertEquals(conf.serverList[2], "192.168.16.18:8808");
        assertEquals(conf.serviceUrl, "/cgi-bin/services/WdgetService.cgi");
        assertEquals(conf.rpcTimeout, 7000);
        assertEquals(conf.rpcErrorRetryTimes, 5);
        assertEquals(conf.header.get("password"), "ID(#J8329%$^S");
    }
}
