/**
 * @(#)KDataParserTest.java, 2012-8-20. Copyright 2012 Niolex, Inc. All rights
 *                           reserved.
 */
package org.apache.niolex.address.ext;

import static org.junit.Assert.assertEquals;

import java.util.Collection;

import org.junit.Test;


/**
 * @author Xie, Jiyun
 */
public class MetaDataTest {

    @Test
    public void testFindExceptionStringThrowable() {
        MetaData kd = MetaData.parse("IPS=10.1.2.3,10.1.2.4\nQUOTA=100,6000\nUTIME=2013-03-18 16:06:37".getBytes());
        // ----
        Collection<Object> list = kd.getBeanMap().values();
        System.out.println(kd);
        assertEquals(list.size(), 2);
    }

}
