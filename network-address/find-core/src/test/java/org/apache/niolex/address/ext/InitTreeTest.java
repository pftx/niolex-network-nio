/**
 * InitTreeTest.java
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

import java.util.List;
import java.util.Map;

import org.apache.niolex.address.core.CoreTest;
import org.apache.niolex.address.op.OPMain;
import org.apache.niolex.commons.test.AnnotationOrderedRunner;
import org.apache.niolex.commons.test.AnnotationOrderedRunner.Order;
import org.apache.niolex.zookeeper.core.ZKException;
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
public class InitTreeTest {

    static ZKOperator zkop;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        zkop = new ZKOperator(CoreTest.ZK_ADDR, 5000);
        zkop.addAuthInfo(OPMain.OP_NAME, OPMain.OP_PASSWORD);
        zkop.setRoot("testMe");
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
    public void testInitTree() throws Exception {
        zkop.initTree(OPMain.OP_NAME, OPMain.OP_PASSWORD);
    }

    @Test(expected=IllegalStateException.class)
    @Order(1)
    public void testInitTreeExcep() throws Exception {
        ZKOperator init = new ZKOperator(CoreTest.ZK_ADDR, 5000);
        init.initTree(OPMain.OP_NAME, OPMain.OP_PASSWORD);
    }

    @Test
    @Order(2)
    public void testInitTreeExist() throws Exception {
        zkop.initTree(OPMain.OP_NAME, OPMain.OP_PASSWORD);
    }

    @Test
    @Order(3)
    public void testAddOperator() throws Exception {
        zkop.addOperator("lex", "lex123");
        List<String> list = zkop.getChildren("/testMe/operators");
        assertEquals(2, list.size());
        assertTrue(list.contains("lex"));
        assertFalse(list.contains("lex1"));
    }

    @Test
    @Order(4)
    public void testAddServer() throws Exception {
        zkop.addServer("lex", "lex123");
        List<String> list = zkop.getChildren("/testMe/servers");
        assertEquals(1, list.size());
        assertTrue(list.contains("lex"));
    }

    @Test
    @Order(5)
    public void testAddClient() throws Exception {
        zkop.addClient("lex", "lex123");
        List<String> list = zkop.getChildren("/testMe/clients");
        assertEquals(1, list.size());
        assertTrue(list.contains("lex"));
    }

    @Test
    @Order(6)
    public void testAddService() throws Exception {
        assertTrue(zkop.addService("org.new"));
    }

    @Test
    @Order(7)
    public void testInitServiceTree() throws Exception {
        assertTrue(zkop.initServiceTree("org.new", 1, new String[] {"M", "T"}));
        zkop.createNode("/testMe/services/org.new/clients/1/find-cli", "a=b");
        assertTrue(zkop.initServiceTree("org.pac", 1, new String[] {"M", "T"}));
    }

    @Test
    @Order(8)
    public void testAddVersion() throws Exception {
        assertEquals(1, zkop.getChildren("/testMe/services/org.new/clients").size());
        assertEquals(1, zkop.getChildren("/testMe/services/org.new/versions").size());
        assertTrue(zkop.addVersion("org.new", 3));
        assertTrue(zkop.addVersion("org.new", 4));
        assertEquals(3, zkop.getChildren("/testMe/services/org.new/versions").size());
        assertEquals(1, zkop.getChildren("/testMe/services/org.new/clients").size());
    }

    @Test
    @Order(9)
    public void testAddMetaVersion() throws Exception {
        assertTrue(zkop.addMetaVersion("org.new", 3));
        assertEquals(3, zkop.getChildren("/testMe/services/org.new/versions").size());
        assertEquals(2, zkop.getChildren("/testMe/services/org.new/clients").size());
    }

    @Test
    @Order(10)
    public void testCopyVersion() throws Exception {
        assertTrue(zkop.copyVersion("org.new", 1, 2));
        assertEquals(4, zkop.getChildren("/testMe/services/org.new/versions").size());
        assertEquals(3, zkop.getChildren("/testMe/services/org.new/clients").size());
        assertTrue(zkop.copyVersion("org.new", 4, 5));
        assertEquals(5, zkop.getChildren("/testMe/services/org.new/versions").size());
        assertEquals(3, zkop.getChildren("/testMe/services/org.new/clients").size());
    }

    @Test
    @Order(11)
    public void testAddState() throws Exception {
        assertEquals(2, zkop.getChildren("/testMe/services/org.new/versions/2").size());
        assertTrue(zkop.addState("org.new", 2, "W"));
        assertEquals(3, zkop.getChildren("/testMe/services/org.new/versions/2").size());
    }

    @Test
    @Order(12)
    public void testUpdateMetaDataStringStringIntStringString() throws Exception {
        assertTrue(zkop.updateMetaData("lex-cli", "org.new", 2, "1st", "Mi UI"));
        Map<String, String> metaData1 = zkop.getMetaData("lex-cli", "org.new", 2);
        assertEquals(1, metaData1.size());
        assertTrue(zkop.updateMetaData("lex-cli", "org.new", 2, "2nd", "skip update meta"));
        Map<String, String> metaData2 = zkop.getMetaData("lex-cli", "org.new", 2);
        assertEquals(2, metaData2.size());
        String e = "{2nd=skip update meta, 1st=Mi UI}";
        assertEquals(e, metaData2.toString());
    }

    @Test
    @Order(13)
    public void testUpdateMetaDataStringStringIntByteArray() throws Exception {
        assertFalse(zkop.updateMetaData("lex-cli", "org.new", 4, "1st", "Mi UI"));
    }

    @Test
    @Order(14)
    public void testUpdateClientTriggerM() throws Exception {
        String path = "/testMe/services/org.new/clients/1";
        zkop.updateNodeData(path, "lx-cli=rel");
        zkop.updateClientTrigger(path, "lx-cli");
        String s = zkop.getDataAsStr(path);
        String e = "lx-cli=6\n";
        assertEquals(e, s);
    }

    @Test(expected=ZKException.class)
    @Order(14)
    public void testUpdateClientTriggerEx() throws Exception {
        zkop.updateClientTrigger("/a/b/c", "lx-cli");
        System.out.println("not yet implemented");
    }

    @Test
    @Order(100)
    public void testDestroy() throws Exception {
        zkop.deleteNode("/testMe/operators/lex");
        zkop.deleteNode("/testMe/servers/lex");
        zkop.deleteNode("/testMe/clients/lex");
        zkop.deleteTree("/testMe");
    }

}
