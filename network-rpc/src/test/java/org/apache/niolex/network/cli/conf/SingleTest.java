/**
 * SingleTest.java
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
package org.apache.niolex.network.cli.conf;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.niolex.network.cli.conf.RpcConfigBean;
import org.apache.niolex.network.cli.conf.RpcConfiger;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-3
 */
public class SingleTest {

    static RpcConfiger configer;

    static {
    	try {
			configer= new RpcConfiger("single.properties");
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    @Test
    public void doConfig_Stand_Alone() {
        RpcConfigBean conf = configer.getConfig();
        assertEquals(conf.serverList[0], "dy-m-st01.niolex.net:8808");
        assertEquals(conf.serverList[2], "192.168.16.18:8808");
        assertEquals(conf.serviceUrl, "/cgi-bin/services/WdgetService.cgi");
        assertEquals(conf.rpcTimeout, 12000);
        assertEquals(conf.rpcErrorRetryTimes, 3);
    }
}
