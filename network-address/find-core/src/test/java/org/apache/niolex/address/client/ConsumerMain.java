/**
 * @(#)ConsumerMain.java, 2012-8-2.
 *
 * Copyright 2012 Niolex, Inc. All rights reserved.
 */
package org.apache.niolex.address.client;

import java.util.List;

import org.apache.niolex.address.core.CoreTest;
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
        Thread.sleep(1000000);
    }

    public static void testGetAddressAndListenChange() throws Exception {
        // It's good.
        MutableOne<List<String>> st = CoreTest.CON_SU.getAddressList(CoreTest.TEST_SERVICE, "1-5", "A");
        System.out.println("[X] Current AddressList ==> \n" + st.data());
        st.addListener(new MutableOne.DataChangeListener<List<String>>() {
            @Override
            public void onDataChange(List<String> newData) {
                System.out.println("[X] New AddressList ==> \n" + newData);
            }
        });
    }

}
