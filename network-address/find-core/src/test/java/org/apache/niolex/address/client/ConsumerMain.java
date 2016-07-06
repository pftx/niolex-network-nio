/**
 * @(#)ConsumerMain.java, 2012-8-2.
 *
 * Copyright 2012 Niolex, Inc. All rights reserved.
 */
package org.apache.niolex.address.client;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.niolex.address.core.CoreTest;
import org.apache.niolex.address.ext.MetaData;
import org.apache.niolex.address.server.Producer;
import org.apache.niolex.commons.bean.MutableOne;

/**
 *
 * @author Xie, Jiyun
 *
 */
public class ConsumerMain {

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        testStatesAndListenChange();
        testGetAddressAndListenChange();
        testPublishAndWithDrawAddress();
        testGetMetaData();
    }

    public static void testStatesAndListenChange() throws Exception {
     // It's good.
        MutableOne<List<String>> st = CoreTest.CON_SU.getAllStats(CoreTest.TEST_SERVICE, "1-5");
        System.out.println("[S] Current StateList ==> " + st.data());
        st.addListener(new MutableOne.DataChangeListener<List<String>>() {
            @Override
            public void onDataChange(List<String> old, List<String> newData) {
                System.out.println("[S] New StateList ==> " + newData);
            }
        });
    }

    public static void testGetAddressAndListenChange() throws Exception {
        // It's good.
        MutableOne<List<String>> st = CoreTest.CON_SU.getAddressList(CoreTest.TEST_SERVICE, "1-5", "A");
        System.out.println("[X] Current AddressList ==> " + st.data());
        st.addListener(new MutableOne.DataChangeListener<List<String>>() {
            @Override
            public void onDataChange(List<String> old, List<String> newData) {
                System.out.println("[X] New AddressList ==> " + newData);
            }
        });
    }

    public static void testPublishAndWithDrawAddress() {
        Producer sub = CoreTest.PRO_DU;
        sub.publishService(CoreTest.TEST_SERVICE, 4, "A", "localhost:6608", true);
        sub.withdrawService(CoreTest.TEST_SERVICE, 4, "A", "localhost:6608");
    }

    public static void testGetMetaData() throws IOException, InterruptedException {
        ConcurrentHashMap<String, MetaData> map = CoreTest.PRO_DU.getMetaData(CoreTest.TEST_SERVICE, 1);
        int i = 6000;
        String oldQ = "Q";
        while (i-- > 0) {
            MetaData meta = map.get("find-cli");
            String newQ = meta.getPropMap().get("IPS");
            if (!newQ.equals(oldQ)) {
                System.out.println("[M] New MetaData ==> " + newQ);
                oldQ = newQ;
            }
            Thread.sleep(1000);
        }
    }
}
