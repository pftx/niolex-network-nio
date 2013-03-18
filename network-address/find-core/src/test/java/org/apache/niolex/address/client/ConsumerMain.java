/**
 * @(#)ConsumerMain.java, 2012-8-2.
 *
 * Copyright 2012 Niolex, Inc. All rights reserved.
 */
package org.apache.niolex.address.client;

import java.util.List;

import org.apache.niolex.address.core.CoreTest;
import org.apache.niolex.commons.bean.MutableOne;
import org.junit.Test;

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


    /**
     * Test method for {@link org.apache.niolex.find.client.Consumer#getAddressList(java.lang.String, java.lang.String, java.lang.String, org.apache.niolex.find.server.IPChangeListener)}.
     */
    @Test
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
