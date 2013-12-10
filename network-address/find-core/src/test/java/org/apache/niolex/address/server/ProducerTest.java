/**
 * @(#)SubscriberTest.java, 2012-6-21. Copyright 2012 Niolex, Inc. All rights
 *                          reserved.
 */
package org.apache.niolex.address.server;

import static org.junit.Assert.*;

import java.util.List;

import org.apache.niolex.address.core.CoreTest;
import org.apache.niolex.commons.bean.MutableOne;
import org.apache.niolex.commons.concurrent.ThreadUtil;
import org.apache.niolex.zookeeper.core.ZKException;
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
        int k = list.data().size();

        sub.withdrawService(CoreTest.TEST_SERVICE, 4, "A", "localhost:6606");
        sub.withdrawService(CoreTest.TEST_SERVICE, 4, "A", "localhost:6608");
        ThreadUtil.sleep(100);
        int d = list.data().size();
        assertEquals(-2, d - k);
        assertEquals("/" + CoreTest.ZK_ROOT, sub.getRoot());
    }

    @Test(expected = ZKException.class)
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

    @Test(expected = IllegalStateException.class)
    public void testNoRoot1() throws Exception {
        CoreTest.NO_ROOT.publishService(CoreTest.TEST_SERVICE, 4, "A", "localhost:7608", true);
    }

    @Test(expected = IllegalStateException.class)
    public void testNoRoot2() throws Exception {
        CoreTest.NO_ROOT.withdrawService(CoreTest.TEST_SERVICE, 4, "A", "localhost:6606");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSmallVersion() throws Exception {
        Producer pro = CoreTest.PRO_DU;
        pro.withdrawService(CoreTest.TEST_SERVICE, -4, "A", "localhost:7608");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidVersion() throws Exception {
        Producer sub = CoreTest.PRO_DU;
        sub.publishService(CoreTest.TEST_SERVICE, 0, "shard", "localhost:8189", true);
    }

    @Test
    public void testTempSeq() throws Exception {
        Producer sub = CoreTest.PRO_DU;
        String s = sub.publishService(CoreTest.TEST_SERVICE, 4, "A", "localhost:8189:2:", null, true, true);
        System.out.println("临时自增节点 " + s);
        assertTrue(s.contains("localhost:8189:2:"));
        sub.withdrawSequentialService(s);
    }

}
