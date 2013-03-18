/**
 * @(#)StateTest.java, 2012-8-2. Copyright 2012 Niolex, Inc. All rights
 *                     reserved.
 */
package org.apache.niolex.address.client;

import java.util.List;

import org.apache.niolex.address.core.CoreTest;
import org.apache.niolex.commons.bean.MutableOne;
import org.junit.Test;


/**
 * @author Xie, Jiyun
 */
public class StateTest {

    private Consumer consumer = CoreTest.CON_SU;

    /**
     * Test method for
     * {@link org.apache.niolex.find.client.Consumer#getAllStatAndListenChange}
     * .
     */
    @Test
    public void testGetAllStatAndListenChange() throws Exception {
        MutableOne<List<String>> stats = consumer.getAllStats(CoreTest.TEST_SERVICE, "1-4");
        List<String> nodeList = stats.data();
        System.out.println("Current States List ==> " + nodeList);
    }
}
