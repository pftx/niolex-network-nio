/**
 * @(#)KDataParserTest.java, 2012-8-20. Copyright 2012 Niolex, Inc. All rights
 *                           reserved.
 */
package org.apache.niolex.address.ext;

import static org.junit.Assert.assertEquals;

import java.util.Collection;

import org.apache.niolex.address.ext.MetaData;
import org.apache.niolex.address.ext.QuotaInfo;
import org.junit.Test;


/**
 * @author Xie, Jiyun
 */
public class MetaDataTest {

    @Test
    public void testFindExceptionStringThrowable() {
        MetaData kd = new MetaData();
        QuotaInfo info = new QuotaInfo();
        info.setClientName("abc");
        kd.getBeanMap().put("abc", info);
        info = new QuotaInfo();
        info.setClientName("123");
        kd.getBeanMap().put("123", info);
        // ----
        Collection<Object> list = kd.getBeanMap().values();
        assertEquals(list.size(), 2);
    }
    
}
