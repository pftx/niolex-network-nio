/**
 * AdvancedProducerTest.java
 *
 * Copyright 2013 The original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.niolex.address.ext;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.niolex.address.core.CoreTest;
import org.apache.niolex.address.core.ZKConnector;
import org.apache.niolex.address.ext.AdvancedProducer.DataWatcher;
import org.apache.niolex.address.op.OPMain;
import org.apache.niolex.commons.util.DateTimeUtil;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.junit.Test;

/**
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5
 * @since 2013-3-15
 */
public class AdvancedProducerTest {

    private AdvancedProducer produ = CoreTest.PRO_DU;

    @Test
    public void testDataWatcher() throws Exception {
        final AtomicInteger i = new AtomicInteger(0);
        ConcurrentHashMap<String, MetaData> map = new ConcurrentHashMap<String, MetaData>();
        DataWatcher a = produ.new DataWatcher(map) {

            @Override
            public void parseMetaData(String path, String client) throws KeeperException, InterruptedException {
                i.incrementAndGet();
                super.parseMetaData(path, client);
            }

        };
        a.setData(new byte[3]);
        WatchedEvent ev = new WatchedEvent(Watcher.Event.EventType.NodeCreated, Watcher.Event.KeeperState.AuthFailed, null);
        a.process(ev);
        ev = new WatchedEvent(Watcher.Event.EventType.NodeDataChanged, Watcher.Event.KeeperState.AuthFailed,
                "/find/services/org.apache.niolex.address.Test/clients/1");
        a.process(ev);
        assertEquals(i.intValue(), 1);
        System.out.println(map);
    }

    /**
     * Test method for {@link org.apache.niolex.address.ext.AdvancedProducer#getMetaData(java.lang.String, java.lang.String)}.
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testGetMetaDataStringString() throws IOException, InterruptedException {
        ConcurrentHashMap<String, MetaData> map = produ.getMetaData(CoreTest.TEST_SERVICE, 1);
        MetaData meta = map.get("find-cli");
        String ts = DateTimeUtil.formatDate2DateTimeStr();
        meta.getPropMap().put("UTIME", ts);

        ZKConnector zKConnector = new ZKConnector(CoreTest.ZK_ADDR, 5000);
        zKConnector.addAuthInfo(OPMain.OP_NAME, OPMain.OP_PASSWORD);
        byte[] data = meta.toByteArray();
        meta.getPropMap().put("UTIME", "FAKE");
        zKConnector.updateNode("/find/services/org.apache.niolex.address.Test/clients/1/find-cli", data);
        byte[] k = zKConnector.getData("/find/services/org.apache.niolex.address.Test/clients/1");
        int e = k.length - 1;
        if (k[e] == '0') {
            k[e] = '1';
        } else {
            k[e] = '0';
        }
        zKConnector.updateNode("/find/services/org.apache.niolex.address.Test/clients/1", k);
        zKConnector.close();

        Thread.sleep(100);
        assertEquals(ts, map.get("find-cli").getPropMap().get("UTIME"));
    }

    /**
     * Test method for {@link org.apache.niolex.address.ext.AdvancedProducer#getMetaData(java.lang.String, int)}.
     * @throws IOException
     */
    @Test
    public void testGetMetaDataStringInt() throws IOException {
        AdvancedProducer pro = new AdvancedProducer(CoreTest.ZK_ADDR, 5000);
        pro.addAuthInfo(OPMain.CLI_NAME, OPMain.CLI_PASSWORD);
        pro.setRoot("find");
        ConcurrentHashMap<String, MetaData> map = pro.getMetaData(CoreTest.TEST_SERVICE, "1+");
        System.out.println(map);
        assertEquals(map.get("find-cli").getPropMap().size(), 2);
    }

    @Test(expected = IllegalStateException.class)
    public void testGetMetaData1() throws Exception {
        AdvancedProducer sub = new AdvancedProducer(CoreTest.ZK_ADDR, 5000);
        sub.addAuthInfo("redis", "mailto:xiejiyun");
        sub.getMetaData("org.apache.niolex.address.Test", 3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetMetaData2() throws Exception {
        AdvancedProducer sub = new AdvancedProducer(CoreTest.ZK_ADDR, 5000);
        sub.addAuthInfo("redis", "mailto:xiejiyun");
        sub.setRoot("dev");
        sub.getMetaData("org.apache.niolex.address.Test", 0);
    }

}
