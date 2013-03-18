/**
 * @(#)CreateItemTest.java, 2012-9-17. 
 * 
 * Copyright 2012 Niolex, Inc. All rights reserved.
 */
package org.apache.niolex.address.core;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.apache.niolex.address.core.CreateItem;
import org.apache.zookeeper.CreateMode;
import org.junit.Test;

/**
 *
 * @author Xie, Jiyun
 *
 */
public class CreateItemTest {
    
    CreateItem item = new CreateItem("a/b/c/d", new byte[] {1,2,3,45,78}, CreateMode.EPHEMERAL_SEQUENTIAL);

    @Test
    public void testPath() {
        item.setPath("abc");
        assertEquals(item.getPath(), "abc");
    }
    
    @Test
    public void testMode() {
        item.setMode(CreateMode.PERSISTENT);
        assertEquals(item.getMode(), CreateMode.PERSISTENT);
    }
    
    @Test
    public void testData() {
        item.setData(new byte[] {1,2,3,45,78});
        assertArrayEquals(item.getData(), new byte[] {1,2,3,45,78});
    }
}
