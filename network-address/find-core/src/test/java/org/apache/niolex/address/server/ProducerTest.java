/**
 * @(#)SubscriberTest.java, 2012-6-21. Copyright 2012 Niolex, Inc. All rights
 *                          reserved.
 */
package org.apache.niolex.address.server;

import java.util.List;

import org.apache.niolex.address.core.CoreTest;
import org.apache.niolex.address.core.FindException;
import org.apache.niolex.commons.bean.MutableOne;
import org.junit.Test;


/**
 * @author Xie, Jiyun
 */
public class ProducerTest {

    @Test
    public void testCreate() throws Exception {
        Producer sub = CoreTest.PRO_DU;
        sub.publishService(CoreTest.TEST_SERVICE, 4, "A", "localhost:6608", true);
        sub.publishService(CoreTest.TEST_SERVICE, 4, "A", "localhost:6606", "abc".getBytes(), true);

        MutableOne<List<String>> list = sub.getAddressList(CoreTest.TEST_SERVICE, 4, "A");
        System.out.println("所有节点 " + list.data());

        sub.withdrawService(CoreTest.TEST_SERVICE, 4, "A", "localhost:6606");
        sub.withdrawService(CoreTest.TEST_SERVICE, 4, "A", "localhost:6608");
    }

    @Test(expected = FindException.class)
    public void testNoAuth() throws Exception {
        Producer sub = new Producer(CoreTest.ZK_ADDR, 5000);
        try {
            sub.setRoot(CoreTest.ZK_ROOT);
            sub.addAuthInfo("redis", "mailto:xiejiyun3");
            sub.publishService(CoreTest.TEST_SERVICE, 4, "A", "localhost:8189", true);
        } finally {
            sub.close();
        }
    }

    @Test
    public void testTempSeq() throws Exception {
        Producer sub = CoreTest.PRO_DU;
        String s = sub.publishService(CoreTest.TEST_SERVICE, 4, "A", "localhost:8189:2:", null, true, true);
        System.out.println("临时自增节点 " + s);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidVersion() throws Exception {
        Producer sub = CoreTest.PRO_DU;
        sub.publishService(CoreTest.TEST_SERVICE, 0, "shard", "localhost:8189", true);
    }

}
