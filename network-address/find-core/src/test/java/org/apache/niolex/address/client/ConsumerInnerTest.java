/**
 * @(#)ConsumerInnerTest.java, 2012-8-20.
 *
 * Copyright 2012 Niolex, Inc. All rights reserved.
 */
package org.apache.niolex.address.client;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.niolex.address.client.Consumer.NodeWatcher;
import org.apache.niolex.address.core.CoreTest;
import org.apache.niolex.commons.bean.MutableOne;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.junit.Test;


/**
 *
 * @author Xie, Jiyun
 *
 */
public class ConsumerInnerTest {


    static private Consumer consumer = CoreTest.CON_SU;

    @Test
    public void testNodeWatcher() throws Exception {
        final AtomicInteger i = new AtomicInteger(0);
        MutableOne<List<String>> listener = new MutableOne<List<String>>();
        listener.addListener(new MutableOne.DataChangeListener<List<String>>() {
            @Override
            public void onDataChange(List<String> old, List<String> newData) {
                System.out.println("[X] New AddressList ==> " + newData);
                i.incrementAndGet();
            }
        });
        NodeWatcher a = consumer.new NodeWatcher(listener);
        WatchedEvent ev = new WatchedEvent(Watcher.Event.EventType.NodeCreated, Watcher.Event.KeeperState.AuthFailed, null);
        a.process(ev);
        ev = new WatchedEvent(Watcher.Event.EventType.NodeChildrenChanged, Watcher.Event.KeeperState.AuthFailed,
                "/find/services/org.apache.niolex.address.Test/versions/1/B");
        a.process(ev);
        assertEquals(i.intValue(), 1);
    }
}
