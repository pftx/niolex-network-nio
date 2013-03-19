/**
 * OPToolTest.java
 *
 * Copyright 2013 The original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.niolex.address.optool;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.ACL;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5
 * @since 2013-3-19
 */
public class OPToolTest {
    protected static final Logger LOG = LoggerFactory.getLogger(OPToolTest.class);

    private static OPTool optool;

    static {
        try {
            optool = new OPTool("10.22.241.233:8181", 5000);
            optool.addAuthInfo("operator:djidf3jdd23");
        } catch (IOException e) {
            LOG.error("Error occured when init optool.", e);
        }
    }

    /**
     * Test method for {@link org.apache.niolex.address.optool.OPTool#setDataStr(java.lang.String, java.lang.String)}.
     * @throws InterruptedException
     * @throws KeeperException
     */
    @Test
    public void testSetDataStr() throws KeeperException, InterruptedException {
        optool.setDataStr("/tmp", "This is so good!!!");
        assertEquals("This is so good!!!", optool.getDataStr("/tmp"));
    }

    /**
     * Test method for {@link org.apache.niolex.address.optool.OPTool#getDataStr(java.lang.String)}.
     * @throws InterruptedException
     * @throws KeeperException
     */
    @Test
    public void testGetDataStr() throws KeeperException, InterruptedException {
        System.out.println(optool.getDataStr("/find/clients"));
    }

    /**
     * Test method for {@link org.apache.niolex.address.optool.OPTool#getACLs(java.lang.String)}.
     * @throws InterruptedException
     * @throws KeeperException
     */
    @Test
    public void testGetACLs() throws KeeperException, InterruptedException {
        List<ACL> list = optool.getACLs("/find/services/org.apache.niolex.address.Test/versions");
        System.out.println(list);
        list = optool.getACLs("/find/clients/find-cli");
        System.out.println("/cli" + list);
    }

    /**
     * Test method for {@link org.apache.niolex.address.optool.OPTool#setACLs(java.lang.String, java.util.List)}.
     * @throws InterruptedException
     * @throws KeeperException
     */
    @Test
    public void testSetACLs() throws KeeperException, InterruptedException {
        optool.setACLs("/tmp", Ids.CREATOR_ALL_ACL);
        System.out.println("/tmp" + optool.getACLs("/tmp"));
    }

}
