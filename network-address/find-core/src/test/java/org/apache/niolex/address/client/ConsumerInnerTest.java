/**
 * @(#)ConsumerInnerTest.java, 2012-8-20.
 *
 * Copyright 2012 Niolex, Inc. All rights reserved.
 */
package org.apache.niolex.address.client;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.niolex.address.client.Consumer.NodeWatcher;
import org.apache.niolex.address.core.CoreTest;
import org.apache.niolex.commons.bean.MutableOne;
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
        a.onDataChange(null);
        a.onChildrenChange(Collections.singletonList("Lex"));
        assertEquals(i.intValue(), 1);
    }
}
