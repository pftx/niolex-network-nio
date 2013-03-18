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

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.niolex.address.client.Consumer;
import org.apache.niolex.address.client.ConsumerMain;
import org.apache.niolex.address.client.DataWatcher;
import org.apache.niolex.commons.bean.MutableOne;
import org.apache.niolex.commons.util.DateTimeUtil;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5
 * @since 2013-3-15
 */
public class AdvancedProducerTest {

    private Consumer consumer;

    @Before
    public void createConsumer() throws Exception {
        consumer = new Consumer(ConsumerMain.ZK_ADDR, 5000);
        consumer.setRoot("dev");
        consumer.addAuthInfo("redis", "mailto:xiejiyun");
    }


    @Test
    public void testDataWatcher() throws Exception {
        final AtomicInteger i = new AtomicInteger(0);
        MutableOne<byte[]> listener = new MutableOne<byte[]>();
        listener.addListener(new MutableOne.DataChangeListener<byte[]>() {
            @Override
            public void onDataChange(byte[] newData) {
                System.out.println("Data Changed ==> \n" + new String(newData));
                i.incrementAndGet();
            }
        });
        DataWatcher a = consumer.new DataWatcher(listener);
        WatchedEvent ev = new WatchedEvent(Watcher.Event.EventType.NodeCreated, Watcher.Event.KeeperState.AuthFailed, null);
        a.process(ev);
        ev = new WatchedEvent(Watcher.Event.EventType.NodeDataChanged, Watcher.Event.KeeperState.AuthFailed,
                "/dev/com.Niolex.ad.find/4");
        a.process(ev);
        assertEquals(i.intValue(), 1);
    }

    /**
     * Test method for {@link org.apache.niolex.address.ext.AdvancedProducer#getMetaData(java.lang.String, java.lang.String)}.
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testGetMetaDataStringString() throws IOException, InterruptedException {
        final AtomicInteger count = new AtomicInteger(0);
        AdvancedProducer sub = new AdvancedProducer("10.22.241.233:8181", 5000);
        MutableOne<MetaData> meta = sub.getMetaData("com.Niolex.ad.find", 3);
        meta.addListener(new MutableOne.DataChangeListener<MetaData>(){

            @Override
            public void onDataChange(MetaData newData) {
                @SuppressWarnings("unchecked")
                List<String> newIps = (List<String>) newData.getBeanMap().get("IPS");
                System.out.println("New IP List:\n" + newIps);
                count.incrementAndGet();
            }});
        @SuppressWarnings("unchecked")
        List<String> newIps = (List<String>) meta.data().getBeanMap().get("IPS");
        System.out.println("IP List:\n" + newIps);
        meta.data().getPropMap().put("CTIME", DateTimeUtil.formatDate2DateTimeStr());
        sub.addAuthInfo("operator", "djidf3jdd23");
        sub.updateNode("/dev/com.Niolex.ad.find/3", meta.data().toByteArray());

        Thread.sleep(250);
        assertEquals(1, count.get());
    }

    /**
     * Test method for {@link org.apache.niolex.address.ext.AdvancedProducer#getMetaData(java.lang.String, int)}.
     */
    @Test
    public void testGetMetaDataStringInt() {
        fail("Not yet implemented");
    }

    @Test(expected = IllegalStateException.class)
    public void testGetMetaData1() throws Exception {
        AdvancedProducer sub = new AdvancedProducer("10.22.241.233:8181", 5000);
        sub.addAuthInfo("redis", "mailto:xiejiyun");
        sub.getMetaData("com.Niolex.ad.find", 3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetMetaData2() throws Exception {
        AdvancedProducer sub = new AdvancedProducer("10.22.241.233:8181", 5000);
        sub.addAuthInfo("redis", "mailto:xiejiyun");
        sub.setRoot("dev");
        sub.getMetaData("com.Niolex.ad.find", 0);
    }


    @Test(expected=IllegalStateException.class)
    public void testRoot_Err_1() throws Exception {
        try {
        consumer.setRoot(null);
        consumer.getMetaData("com.Niolex.ad.find", "1-5");
    } catch (Exception e) {throw e;}
    }

    @Test(expected=IllegalArgumentException.class)
    public void testVer_Err_1() throws Exception {
        try {
        consumer.getMetaData("com.Niolex.ad.find", "1-5+");
    } catch (Exception e) {throw e;}
    }

    @Test(expected=IllegalArgumentException.class)
    public void testVer_Err_11() throws Exception {
        consumer.getMetaData("com.Niolex.ad.find", "5+");
    }


    @Test
    public void testGetMetaDataAndListenChange() throws Exception {
        MutableOne<byte[]> data = consumer.getMetaData("com.Niolex.ad.find", "1-4");
        System.out.println("Current Data ==> \n" + new String(data.data()));
        MetaData k = MetaData.wrap(data).data();
        assertTrue(((List<?>)k.getBeanMap().get("IPS")).size() > 2);
        data.addListener(new MutableOne.DataChangeListener<byte[]>() {

            @Override
            public void onDataChange(byte[] newData) {
                System.out.println("New Data ==> \n" + new String(newData));
            }
        });
    }

    @Test
    public void testGetMetaData() throws Exception {
        MutableOne<byte[]> data = consumer.getMetaData("com.Niolex.ad.find", "1-4");
        System.out.println("Current Data ==> \n" + new String(data.data()));
        MetaData k = MetaData.wrap(data).data();
        assertTrue(((List<?>)k.getBeanMap().get("IPS")).size() > 2);
        final AtomicInteger count = new AtomicInteger(0);
        data.addListener(new MutableOne.DataChangeListener<byte[]>() {

            @Override
            public void onDataChange(byte[] newData) {
                System.out.println("New Data ==> \n" + new String(newData));
                count.incrementAndGet();
            }
        });
        DataWatcher ipw = consumer.new DataWatcher(data);
        String s = "/dev/com.Niolex.ad.find/3";
        WatchedEvent event = new WatchedEvent(EventType.NodeDataChanged, KeeperState.SyncConnected, s);
        ipw.process(event);
        assertEquals(1, count.get());
        event = new WatchedEvent(EventType.NodeCreated, KeeperState.SyncConnected, s);
        ipw.process(event);
        assertEquals(1, count.get());
    }

    @Test
    public void testGetMetaDataIntVersion() throws Exception {
        MutableOne<MetaData> data = consumer.getMetaData("com.Niolex.ad.find", 3);
        System.out.println("Current Data ==> \n" + data);
        assertTrue(((List<?>)data.data().getBeanMap().get("IPS")).size() > 2);
        final AtomicInteger count = new AtomicInteger(0);
        data.addListener(new MutableOne.DataChangeListener<MetaData>() {

            @Override
            public void onDataChange(MetaData newData) {
                System.out.println("New Data ==> \n" + newData);
                count.incrementAndGet();
            }
        });
    }

}
