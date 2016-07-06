/**
 * @(#)KDataParserTest.java, 2012-8-20. Copyright 2012 Niolex, Inc. All rights
 *                           reserved.
 */
package org.apache.niolex.address.ext;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Map;

import org.apache.niolex.commons.file.FileUtil;
import org.junit.Test;


/**
 * @author Xie, Jiyun
 */
public class MetaDataTest {

    @Test
    public void testSimpleParse() {
        byte[] origin = "IPS=10.1.2.3,10.1.2.4\nQUOTA=100,6000\nUTIME=2013-03-18 16:06:37\n".getBytes();
        MetaData kd = MetaData.parse(origin);
        // ----
        byte[] after = kd.toByteArray();
        assertArrayEquals(origin, after);
        String s = "MetaData [propMap={IPS=10.1.2.3,10.1.2.4, QUOTA=100,6000, UTIME=2013-03-18 16:06:37}," +
        		" beanMap={IPS=[10.1.2.3, 10.1.2.4], QUOTA={secQ=100, minQ=6000}}]";
        assertEquals(s, kd.toString());
        Map<String, Object> map = kd.getBeanMap();
        assertEquals(map.size(), 2);

        Map<String, String> prop = kd.getPropMap();
        assertEquals(prop.size(), 3);
        prop.put("LEX", "I modified this map.");
        // -----
        byte[] again = kd.toByteArray();
        assertNotEquals(after.length, again.length);

        Collection<String> ips = kd.getIPs();
        assertEquals(2, ips.size());
        QuotaInfo q = kd.getQuota();
        assertEquals(100, q.getSecondQuota());
        assertEquals(6000, q.getMinuteQuota());
    }

    @Test
    public void testExtraParse() {
        byte[] origin = "LEX=100,6000\nUTIME=2013-03-18 16:06:37\n".getBytes();
        MetaData kd = MetaData.parse(origin);
        // ----
        byte[] after = kd.toByteArray();
        assertArrayEquals(origin, after);
        String s = "MetaData [propMap={LEX=100,6000, UTIME=2013-03-18 16:06:37}, beanMap={}]";
        assertEquals(s, kd.toString());
        Map<String, Object> map = kd.getBeanMap();
        assertEquals(map.size(), 0);

        Map<String, String> prop = kd.getPropMap();
        assertEquals(prop.size(), 2);
    }

    @Test
    public void testFileParse() {
        byte[] origin = FileUtil.getBinaryFileContentFromClassPath("meta.txt", MetaDataTest.class);
        MetaData kd = MetaData.parse(origin);
        // ----
        String s = "MetaData [propMap={IPS=,\t\t,1,2;3:127.0.0.1:127.0.0.2:, QUOTA=10\t\t,  100,3000, a=b, c=d}, beanMap={IPS=[, 1, 2, 3, 127.0.0.1, 127.0.0.2], QUOTA={secQ=10, minQ=100}}]";
        assertEquals(s, kd.toString());
        Map<String, Object> map = kd.getBeanMap();
        assertEquals(map.size(), 2);

        Map<String, String> prop = kd.getPropMap();
        assertEquals(prop.size(), 4);
    }

}
