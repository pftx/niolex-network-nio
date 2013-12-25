/**
 * ZKOperatorTest.java
 *
 * Copyright 2013 the original author or authors.
 *
 * We licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.apache.niolex.address.ext;


import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.apache.niolex.address.core.CoreTest;
import org.apache.niolex.address.op.OPMain;
import org.apache.niolex.address.util.ACLUtil;
import org.apache.niolex.commons.reflect.FieldUtil;
import org.apache.niolex.commons.test.AnnotationOrderedRunner;
import org.apache.niolex.commons.test.AnnotationOrderedRunner.Order;
import org.apache.niolex.zookeeper.core.ZKConnector;
import org.apache.niolex.zookeeper.core.ZKException;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2013-12-25
 */
@RunWith(AnnotationOrderedRunner.class)
public class ZKOperatorTest {

    static ZKOperator zkop;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        zkop = new ZKOperator(CoreTest.ZK_ADDR, 5000);
        zkop.addAuthInfo(OPMain.OP_NAME, OPMain.OP_PASSWORD);
        zkop.setRoot(CoreTest.ZK_ROOT);
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        zkop.close();
    }

    @Test
    @Order(0)
    public void testZKOperator() throws Exception {
        String path = "/find/services/org.new/versions/3/B/localhost:9001";
        List<ACL> acl = OPMain.getCDR4Server();
        zkop.createNode(path, acl);
        boolean flag = false;
        try {
            zkop.createNode(path, acl);
        } catch (ZKException e) {
            flag = true;
            assertEquals(e.getCode(), ZKException.Code.NODE_EXISTS);
        }
        assertTrue(flag);
        zkop.deleteNode(path);
    }

    @Test
    public void testGetACL() throws Exception {
        String path = "/find/services/org.new/versions/3/B";
        List<ACL> acl = zkop.getACL(path);
        assertEquals(acl.size(), 3);
        boolean flag = false;
        try {
            zkop.getACL(path + "/localhost:9001");
        } catch (ZKException e) {
            flag = true;
            assertEquals(e.getCode(), ZKException.Code.NO_NODE);
        }
        assertTrue(flag);
    }

    @Test
    public void testSetACL() throws Exception {
        String path = "/find/services/org.new/versions/3/A";
        List<ACL> acl = OPMain.getCDR4Server();
        acl = ACLUtil.mergeACL(acl, OPMain.getAll4Op());
        List<ACL> back = zkop.getACL(path);
        assertEquals(3, back.size());
        zkop.setACL(path, acl);
        assertEquals(zkop.getACL(path), acl);
        zkop.setACL(path, back);
        boolean flag = false;
        try {
            zkop.setACL(path + "/localhost:9001", acl);
        } catch (ZKException e) {
            flag = true;
            assertEquals(e.getCode(), ZKException.Code.NO_NODE);
        }
        assertTrue(flag);
    }

    @Test
    public void testAddACL() throws Exception {
        String path = "/find/services/org.new/versions/3/A";
        List<ACL> acl = OPMain.getCDR4Server();
        List<ACL> back = zkop.getACL(path);
        assertEquals(3, back.size());
        zkop.addACL(path, acl);
        assertEquals(zkop.getACL(path).size(), 4);
        zkop.removeACL(path, acl);
        boolean flag = false;
        try {
            zkop.addACL(path + "/localhost:9001", acl);
        } catch (ZKException e) {
            flag = true;
            assertEquals(e.getCode(), ZKException.Code.NO_NODE);
        }
        assertTrue(flag);
    }

    @Test
    public void testAddACLTree() throws Exception {
        String path = "/find/services/org.new/versions/3/A";
        String path2 = "/find/services/org.new/versions/3/A/1.2.3.4";
        List<ACL> acl = OPMain.getCDR4Server();
        List<ACL> back = zkop.getACL(path);
        List<ACL> back2 = zkop.getACL(path2);
        zkop.addACLTree(path, acl);
        assertEquals(back.size() + 1, zkop.getACL(path).size());
        assertEquals(back2.size() + 1, zkop.getACL(path2).size());
        zkop.removeACLTree(path, acl);
        assertEquals(back, zkop.getACL(path));
        assertEquals(back2, zkop.getACL(path2));
    }

    @Test
    public void testRemoveACL() throws Exception {
        String path = "/find/services/org.new/versions/3/A";
        List<ACL> acl = OPMain.getCDR4Server();
        boolean flag = false;
        try {
            zkop.removeACL(path + "/localhost:9001", acl);
        } catch (ZKException e) {
            flag = true;
            assertEquals(e.getCode(), ZKException.Code.NO_NODE);
        }
        assertTrue(flag);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRemoveACLTree() throws Exception {
        ZooKeeper zkback = zkop.zooKeeper();
        Field f = FieldUtil.getField(ZKConnector.class, "zk");
        ZooKeeper zk = mock(ZooKeeper.class);
        KeeperException throwable1 = KeeperException.create(KeeperException.Code.BADVERSION);
        KeeperException throwable2 = KeeperException.create(KeeperException.Code.APIERROR);
        when(zk.setACL(anyString(), anyList(), anyInt())).thenThrow(throwable1, throwable2, throwable1, throwable2);
        //
        List<ACL> acl = OPMain.getCDR4Server();
        FieldUtil.setFieldValue(f, zkop, zk);
        boolean flag = false;
        try {
            zkop.removeACL("/localhost:9001", acl);
        } catch (ZKException e) {
            flag = true;
            assertEquals(e.getCode(), ZKException.Code.OTHER);
        }
        assertTrue(flag);
        // ------
        flag = false;
        try {
            zkop.addACL("/localhost:9001", acl);
        } catch (ZKException e) {
            flag = true;
            assertEquals(e.getCode(), ZKException.Code.OTHER);
        }
        assertTrue(flag);
        FieldUtil.setFieldValue(f, zkop, zkback);
    }

    @Test
    public void testLoginOp() throws Exception {
        ZKOperator zkop = new ZKOperator(CoreTest.ZK_ADDR, 5000);
        zkop.setRoot(CoreTest.ZK_ROOT);
        assertFalse(zkop.loginOp(OPMain.OP_NAME, "abcdefg"));
        assertFalse(zkop.loginOp("abcdefg", OPMain.OP_PASSWORD));
        assertTrue(zkop.loginOp(OPMain.OP_NAME, OPMain.OP_PASSWORD));
        zkop.close();
    }

    @Test
    public void testLoginServer() throws Exception {
        ZKOperator zkop = new ZKOperator(CoreTest.ZK_ADDR, 5000);
        zkop.setRoot(CoreTest.ZK_ROOT);
        assertFalse(zkop.loginServer(OPMain.SVR_NAME, "abcdefg"));
        assertFalse(zkop.loginServer("abcdefg", OPMain.SVR_PASSWORD));
        assertFalse(zkop.loginServer(OPMain.OP_NAME, OPMain.OP_PASSWORD));
        assertTrue(zkop.loginServer(OPMain.SVR_NAME, OPMain.SVR_PASSWORD));
        zkop.close();
    }

    @Test
    public void testLoginClient() throws Exception {
        ZKOperator zkop = new ZKOperator(CoreTest.ZK_ADDR, 5000);
        zkop.setRoot(CoreTest.ZK_ROOT);
        assertFalse(zkop.loginClient(OPMain.CLI_NAME, "abcdefg"));
        assertFalse(zkop.loginClient("abcdefg", OPMain.CLI_PASSWORD));
        assertFalse(zkop.loginClient(OPMain.OP_NAME, OPMain.OP_PASSWORD));
        assertTrue(zkop.loginClient(OPMain.CLI_NAME, OPMain.CLI_PASSWORD));
        zkop.close();
    }

    @Test
    public void testGetSuperUser() throws Exception {
        assertEquals(OPMain.OP_NAME, zkop.getSuperUser());
    }

    @Test
    public void testAddOperator() throws Exception {
        assertFalse(zkop.addOperator(OPMain.OP_NAME, OPMain.OP_PASSWORD));
    }

    @Test
    public void testAddServer() throws Exception {
        assertFalse(zkop.addServer(OPMain.SVR_NAME, OPMain.SVR_PASSWORD));
    }

    @Test
    public void testAddClient() throws Exception {
        assertFalse(zkop.addClient(OPMain.CLI_NAME, OPMain.CLI_PASSWORD));
    }

    @Test
    public void testGetServerACL() throws Exception {
        List<ACL> list = zkop.getServerACL(OPMain.SVR_NAME);
        String s = ACLUtil.formatACL(list);
        String e = "total 1\n\tACDRW find-svr\n";
        assertEquals(e, s);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetServerACLEx() throws Exception {
        List<ACL> list = zkop.getServerACL(OPMain.CLI_NAME);
        String s = ACLUtil.formatACL(list);
        String e = "total 1\n\tACDRW find-svr\n";
        assertEquals(e, s);
    }

    @Test
    public void testGetClientACL() throws Exception {
        List<ACL> list = zkop.getClientACL(OPMain.CLI_NAME);
        String s = ACLUtil.formatACL(list);
        String e = "total 1\n\tACDRW find-cli\n";
        assertEquals(e, s);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetClientACLEx() throws Exception {
        List<ACL> list = zkop.getClientACL(OPMain.SVR_NAME);
        String s = ACLUtil.formatACL(list);
        String e = "total 1\n\tACDRW find-cli\n";
        assertEquals(e, s);
    }

    @Test
    public void testInitTree() throws Exception {
        assertFalse(zkop.initTree(null, null));
    }

    @Test
    public void testAddService() throws Exception {
        assertFalse(zkop.addService("org.new"));
    }

    @Test
    public void testInitServiceTree() throws Exception {
        System.out.println("skip init-service-tree");
    }

    @Test
    public void testAddVersion() throws Exception {
        assertFalse(zkop.addVersion("org.pac", 3));
    }

    @Test
    public void testAddMetaVersion() throws Exception {
        assertFalse(zkop.addMetaVersion("org.pac", 3));
    }

    @Test
    public void testCopyVersion() throws Exception {
        assertFalse(zkop.copyVersion("org.pac", 3, 6));
    }

    @Test
    public void testAddState() throws Exception {
        assertFalse(zkop.addState("org.new", 3, "D"));
    }

    @Test
    public void testAddServerAuthStringStringInt() throws Exception {
        assertFalse(zkop.addServerAuth("find-svr", "org.pac", 3));
        List<ACL> acl1 = zkop.getACL("/find/services/org.new/versions/3/A");
        assertTrue(zkop.addServerAuth("find-svr", "org.new", 3));
        List<ACL> acl2 = zkop.getACL("/find/services/org.new/versions/3/A");
        assertTrue(zkop.removeServerAuth("find-svr", "org.new", 3));
        List<ACL> acl3 = zkop.getACL("/find/services/org.new/versions/3/A");
        assertEquals(acl1, acl3);
        assertEquals(acl1.size() + 1, acl2.size());
    }

    @Test
    public void testAddServerMetaAuth() throws Exception {
        assertFalse(zkop.addServerMetaAuth("find-svr", "org.pac", 3));
        List<ACL> acl1 = zkop.getACL("/find/services/org.new/clients/3/abc");
        assertTrue(zkop.addServerMetaAuth("find-svr", "org.new", 3));
        List<ACL> acl2 = zkop.getACL("/find/services/org.new/clients/3/abc");
        assertTrue(zkop.removeServerMetaAuth("find-svr", "org.new", 3));
        List<ACL> acl3 = zkop.getACL("/find/services/org.new/clients/3/abc");
        assertEquals(acl1, acl3);
        assertEquals(acl1.size() + 1, acl2.size());
    }

    @Test
    public void testAddServerAuthStringStringIntString() throws Exception {
        assertFalse(zkop.addServerAuth("find-svr", "org.pac", 3, "D"));
        List<ACL> acl11 = zkop.getACL("/find/services/org.new/versions/3/A");
        List<ACL> acl12 = zkop.getACL("/find/services/org.new/versions/3/D");
        assertTrue(zkop.addServerAuth("find-svr", "org.new", 3, "D"));
        List<ACL> acl21 = zkop.getACL("/find/services/org.new/versions/3/A");
        List<ACL> acl22 = zkop.getACL("/find/services/org.new/versions/3/D");
        assertTrue(zkop.removeServerAuth("find-svr", "org.new", 3, "D"));
        List<ACL> acl31 = zkop.getACL("/find/services/org.new/versions/3/A");
        List<ACL> acl32 = zkop.getACL("/find/services/org.new/versions/3/D");
        assertEquals(acl11, acl31);
        assertEquals(acl21, acl31);
        assertEquals(acl12, acl32);
        assertEquals(acl12.size() + 1, acl22.size());
    }

    @Test
    public void testAddClientAuthStringString() throws Exception {
        assertFalse(zkop.addClientAuth("find-cli", "org.pac"));
        List<ACL> acl1 = zkop.getACL("/find/services/org.new/versions/7");
        assertTrue(zkop.addClientAuth("find-cli", "org.new"));
        List<ACL> acl2 = zkop.getACL("/find/services/org.new/versions/7");
        assertTrue(zkop.removeClientAuth("find-cli", "org.new"));
        List<ACL> acl3 = zkop.getACL("/find/services/org.new/versions/7");
        assertEquals(acl1, acl3);
        assertEquals(acl1.size() + 1, acl2.size());
    }

    @Test
    public void testAddClientAuthStringStringInt() throws Exception {
        assertFalse(zkop.addClientAuth("find-cli", "org.pac", 3));
        List<ACL> acl1 = zkop.getACL("/find/services/org.new/versions/3/A");
        assertTrue(zkop.addClientAuth("find-cli", "org.new", 3));
        List<ACL> acl2 = zkop.getACL("/find/services/org.new/versions/3/A");
        assertTrue(zkop.removeClientAuth("find-cli", "org.new", 3));
        List<ACL> acl3 = zkop.getACL("/find/services/org.new/versions/3/A");
        assertEquals(acl1, acl3);
        assertEquals(acl1.size() + 1, acl2.size());
    }

    @Test
    public void testRemoveServerAuthStringStringInt() throws Exception {
        assertFalse(zkop.removeServerAuth("find-svr", "org.pac", 3));
    }

    @Test
    public void testRemoveServerMetaAuth() throws Exception {
        assertFalse(zkop.removeServerMetaAuth("find-svr", "org.pac", 3));
    }

    @Test
    public void testRemoveServerAuthStringStringIntString() throws Exception {
        assertFalse(zkop.removeServerAuth("find-svr", "org.pac", 3, "D"));
    }

    @Test
    public void testRemoveClientAuthStringString() throws Exception {
        assertFalse(zkop.removeClientAuth("find-cli", "org.pac"));
    }

    @Test
    public void testRemoveClientAuthStringStringInt() throws Exception {
        assertFalse(zkop.removeClientAuth("find-cli", "org.pac", 3));
    }

    @Test
    public void testGetMetaData() throws Exception {
        assertNull(zkop.getMetaData("find-cli", "org.pac", 3));
        Map<String, String> metaData = zkop.getMetaData("a", "org.new", 3);
        assertEquals(2, metaData.size());
    }

    @Test
    @Order(14)
    public void testUpdateClientTrigger() throws Exception {
        ZooKeeper zkback = zkop.zooKeeper();
        Field f = FieldUtil.getField(ZKConnector.class, "zk");
        ZooKeeper zk = mock(ZooKeeper.class);
        KeeperException throwable1 = KeeperException.create(KeeperException.Code.BADVERSION);
        KeeperException throwable2 = KeeperException.create(KeeperException.Code.APIERROR);
        when(zk.setData(anyString(), any(byte[].class), anyInt())).thenThrow(throwable1, throwable2, throwable1, throwable2);
        //
        FieldUtil.setFieldValue(f, zkop, zk);
        boolean flag = false;
        try {
            String path = "/find/services/org.new/clients/1";
            zkop.updateClientTrigger(path, "lx-cli");
        } catch (ZKException e) {
            flag = true;
            assertEquals(e.getCode(), ZKException.Code.OTHER);
        }
        assertTrue(flag);
        FieldUtil.setFieldValue(f, zkop, zkback);
    }

}
