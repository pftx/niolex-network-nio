/**
 * ACLUtilTest.java
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
package org.apache.niolex.address.util;


import static org.junit.Assert.*;

import java.util.List;

import org.apache.niolex.commons.net.NetUtil;
import org.apache.zookeeper.ZooDefs.Perms;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

/**
 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2013-12-13
 */
public class ACLUtilTest extends ACLUtil {

    String s;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        s = getSignature("lex", "root");
        assertEquals("iRA3h10iqoCYtrgrk/c8uyvJBKc=", s);
    }

    @Test
    public void testGetSignature() throws Exception {
        List<ACL> old = getCRDRights("qq", "root");
        List<ACL> add = getAllRights("qq", "root");
        List<ACL> merged = mergeACL(old, add);
        assertEquals(1, merged.size());
        assertEquals(Perms.ALL, merged.get(0).getPerms());
        String r = "'digest,'qq:qyPbmtJUmj/VX2gpl2jN4A94Afk=\n";
        assertEquals(r, merged.get(0).getId().toString());
    }

    @Test
    public void testGetId() throws Exception {
        Id id = getId("qq", "root");
        String r = "'digest,'qq:qyPbmtJUmj/VX2gpl2jN4A94Afk=\n";
        assertEquals(r.length(), id.toString().length());
        assertEquals(r, id.toString());
    }

    @Test
    public void testGetAllRights() throws Exception {
        List<ACL> allRights = getAllRights("qq", "root");
        assertEquals(1, allRights.size());
        assertEquals(Perms.ALL, allRights.get(0).getPerms());
        String r = "'digest,'qq:qyPbmtJUmj/VX2gpl2jN4A94Afk=\n";
        assertEquals(r, allRights.get(0).getId().toString());
    }

    @Test
    public void testGetCRDRights() throws Exception {
        List<ACL> crdRights = getCRDRights("qq", "root");
        assertEquals(1, crdRights.size());
        int p = crdRights.get(0).getPerms();
        assertNotEquals(Perms.ALL, p);
        assertTrue((Perms.CREATE & p) > 0);
        assertTrue((Perms.DELETE & p) > 0);
        assertTrue((Perms.READ & p) > 0);
        assertTrue((Perms.ADMIN & p) == 0);
        assertTrue((Perms.WRITE & p) == 0);
        String r = "'digest,'qq:qyPbmtJUmj/VX2gpl2jN4A94Afk=\n";
        assertEquals(r, crdRights.get(0).getId().toString());
    }

    @Test
    public void testGetCRDRightsList() throws Exception {
        Id id = getId("qqname", "root");
        List<ACL> old = Lists.newArrayList();
        ACL su1 = new ACL(Perms.DELETE, id);
        ACL su2 = new ACL(Perms.CREATE | Perms.READ | Perms.DELETE, id);
        ACL su3 = new ACL(Perms.ALL, id);
        old.add(su1);
        old.add(su2);
        old.add(su1);
        Id id2 = getId("qq", "lex");
        ACL su4 = new ACL(Perms.ALL, id2);
        old.add(su4);
        old.add(su3);
        List<ACL> crdRights = getCRDRights("qq", old);
        assertEquals(1, crdRights.size());
        int p = crdRights.get(0).getPerms();
        assertNotEquals(Perms.ALL, p);
        assertTrue((Perms.CREATE & p) > 0);
        assertTrue((Perms.DELETE & p) > 0);
        assertTrue((Perms.READ & p) > 0);
        assertTrue((Perms.ADMIN & p) == 0);
        assertTrue((Perms.WRITE & p) == 0);
        assertEquals(id2, crdRights.get(0).getId());
    }

    @Test
    public void testGetCRDRightsListEmpty() throws Exception {
        List<ACL> old = Lists.newArrayList();
        List<ACL> crdRights = getCRDRights("qq", old);
        assertEquals(0, crdRights.size());
    }

    @Test
    public void testGetReadRight() {
        List<ACL> list = Lists.newArrayList();

        List<ACL> readRight = getReadRight("qq", list);
        assertEquals(0, readRight.size());
        Id root = getId("qq", "root");
        list.add(new ACL(Perms.DELETE, root));
        Id lex = getId("qq", "lex");
        list.add(new ACL(Perms.CREATE, lex));

        readRight = getReadRight("qq", list);
        assertEquals(1, readRight.size());
        assertEquals(Perms.READ, readRight.get(0).getPerms());
        assertEquals(root, readRight.get(0).getId());
        //
        list.clear();
        root = getId("qqa", "root");
        list.add(new ACL(Perms.DELETE, root));
        list.add(new ACL(Perms.CREATE, lex));

        readRight = getReadRight("qq", list);
        assertEquals(1, readRight.size());
        assertEquals(Perms.READ, readRight.get(0).getPerms());
        assertEquals(lex, readRight.get(0).getId());
    }

    @Test
    public void testBelongsTo() throws Exception {
        Id id = new Id("ip", NetUtil.getLocalIP());
        assertFalse(belongsTo(id, "ip"));
        id = getId("qq", "root");
        assertFalse(belongsTo(id, "q"));
        assertTrue(belongsTo(id, "qq"));
        assertFalse(belongsTo(id, "qqq"));
    }

    @Test
    public void testMergeACL() throws Exception {
        Id id = getId("qq", "root");
        List<ACL> old = Lists.newArrayList();
        ACL su1 = new ACL(Perms.DELETE, id);
        ACL su2 = new ACL(Perms.CREATE | Perms.READ | Perms.DELETE, id);
        ACL su3 = new ACL(Perms.ALL, id);
        old.add(su1);
        old.add(su2);
        old.add(su1);
        old.add(su3);
        List<ACL> add = Lists.newArrayList();
        id = getId("qq", "lex");
        ACL su4 = new ACL(Perms.ALL, id);
        add.add(su2);
        add.add(su1);
        add.add(su4);
        List<ACL> merged = mergeACL(add, old);
        assertEquals(2, merged.size());
        assertEquals(Perms.DELETE, merged.get(0).getPerms());
        String r = "'digest,'qq:qyPbmtJUmj/VX2gpl2jN4A94Afk=\n";
        assertEquals(r, merged.get(0).getId().toString());
    }

    @Test
    public void testRemoveId() throws Exception {
        Id root = getId("qq", "root");
        Id lex = getId("qq", "lex");
        Id nio = getId("nio", "lex");
        List<ACL> old = Lists.newArrayList();
        old.add(new ACL(Perms.DELETE, root));
        old.add(new ACL(Perms.DELETE | Perms.READ, lex));
        old.add(new ACL(Perms.ALL, nio));
        old.add(new ACL(Perms.DELETE, root));
        old.add(new ACL(Perms.CREATE | Perms.READ, lex));
        old.add(new ACL(Perms.ALL, nio));
        old.add(new ACL(Perms.READ, root));
        old.add(new ACL(Perms.ADMIN, lex));
        old.add(new ACL(Perms.DELETE, nio));
        List<ACL> acl = removeId(old, root);
        assertEquals(6, acl.size());
        acl = removeId(acl, lex);
        assertEquals(3, acl.size());
        acl = removeId(acl, getId("qq:", "lex"));
        assertEquals(3, acl.size());
        acl = removeId(acl, nio);
        assertEquals(0, acl.size());
    }

}
