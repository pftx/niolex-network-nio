/**
 * PathUtilTest.java
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
package org.apache.niolex.address.util;

import static org.junit.Assert.*;

import org.apache.niolex.address.util.PathUtil.Path.Level;
import org.junit.Test;

/**
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5
 * @since 2013-3-15
 */
public class PathUtilTest extends PathUtil {

    /**
     * Test method for {@link org.apache.niolex.address.util.PathUtil#makeService2VersionPath(java.lang.String, java.lang.String)}.
     */
    @Test
    public void testMakeService2VersionPath() {
        String s = PathUtil.makeService2VersionPath("/online", "org.lex.Good");
        System.out.println(s);
        assertEquals("/online/services/org.lex.Good/versions", s);
    }

    /**
     * Test method for {@link org.apache.niolex.address.util.PathUtil#makeService2StatePath(java.lang.String, java.lang.String, int)}.
     */
    @Test
    public void testMakeService2StatePath() {
        String s = PathUtil.makeService2StatePath("/online", "org.lex.Good", 5);
        System.out.println(s);
        assertEquals("/online/services/org.lex.Good/versions/5", s);
    }

    /**
     * Test method for {@link org.apache.niolex.address.util.PathUtil#makeService2NodePath(java.lang.String, java.lang.String, int, java.lang.String)}.
     */
    @Test
    public void testMakeService2NodePath() {
        String s = PathUtil.makeService2NodePath("/online", "org.lex.Good",5, "default");
        System.out.println(s);
        assertEquals("/online/services/org.lex.Good/versions/5/default", s);
    }

    @Test
    public void testMakeMeta2ClientPath() throws Exception {
        String s = PathUtil.makeMeta2ClientPath("/online", "org.lex.Good");
        System.out.println(s);
        assertEquals("/online/services/org.lex.Good/clients", s);
    }

    @Test
    public void testMakeMeta2VersionPath() throws Exception {
        String s = PathUtil.makeMeta2VersionPath("/online", "org.lex.Good", 3);
        System.out.println(s);
        assertEquals("/online/services/org.lex.Good/clients/3", s);
    }

    @Test
    public void testMakeMeta2NodePath() throws Exception {
        String s = makeMeta2NodePath("/online", "org.lex.Good", 3, "lex");
        System.out.println(s);
        assertEquals("/online/services/org.lex.Good/clients/3/lex", s);
    }

    @Test
    public void testMakeOpPathString() throws Exception {
        assertEquals("/find/operators", makeOpPath("/find"));
    }

    @Test
    public void testMakeServerPathString() throws Exception {
        assertEquals("/find/servers", makeServerPath("/find"));
    }

    @Test
    public void testMakeClientPathString() throws Exception {
        assertEquals("/find/clients", makeClientPath("/find"));
    }

    @Test
    public void testMakeServicePathString() throws Exception {
        assertEquals("/find/services", makeServicePath("/find"));
    }

    @Test
    public void testMakeOpPathStringString() throws Exception {
        assertEquals("/find/operators/lex", makeOpPath("/find", "lex"));
    }

    @Test
    public void testMakeServerPathStringString() throws Exception {
        assertEquals("/find/servers/lex", makeServerPath("/find", "lex"));
    }

    @Test
    public void testMakeClientPathStringString() throws Exception {
        assertEquals("/find/clients/lex", makeClientPath("/find", "lex"));
    }

    @Test
    public void testMakeServicePathStringString() throws Exception {
        assertEquals("/find/services/lex", makeServicePath("/find", "lex"));
    }

    @Test
    public void testDecodePathOther1() throws Exception {
        Path p = decodePath("roo", "/find/services/org.apache.niolex.address.Test");
        assertEquals(Level.OTHER, p.getLevel());
    }

    @Test
    public void testDecodePathOther2() throws Exception {
        Path p = decodePath("roo", "/");
        assertEquals(Level.OTHER, p.getLevel());
    }

    @Test
    public void testDecodePathOther3() throws Exception {
        Path p = decodePath("roo", "roo");
        assertEquals(Level.OTHER, p.getLevel());
    }

    @Test
    public void testDecodePathRoot1() throws Exception {
        Path p = decodePath("roo", "/roo");
        assertEquals(Level.ROOT, p.getLevel());
        assertEquals("roo", p.getRoot());
        assertNull(p.getService());
    }

    @Test
    public void testDecodePathRoot2() throws Exception {
        Path p = decodePath("roo", "/roo/services");
        assertEquals(Level.RO_SER, p.getLevel());
        assertEquals("roo", p.getRoot());
        assertNull(p.getService());
    }

    @Test
    public void testDecodePathRoot3() throws Exception {
        Path p = decodePath("roo", "/roo/operators");
        assertEquals(Level.RO_OTHER, p.getLevel());
        assertEquals("roo", p.getRoot());
        assertNull(p.getService());
    }

    @Test
    public void testDecodePathRoot4() throws Exception {
        Path p = decodePath("roo", "/roo/operators/root");
        assertEquals(Level.RO_OTHER, p.getLevel());
        assertEquals("roo", p.getRoot());
        assertNull(p.getService());
    }

    @Test
    public void testDecodePathService1() throws Exception {
        Path p = decodePath("find", "/find/services/org.apache.niolex.address.Test");
        assertEquals(Level.SERVICE, p.getLevel());
        assertEquals("find", p.getRoot());
        assertEquals("org.apache.niolex.address.Test", p.getService());
    }

    @Test
    public void testDecodePathSVer() throws Exception {
        Path p = decodePath("find", "/find/services/org.apache.niolex.address.Test/versions");
        assertEquals(Level.SER_VER, p.getLevel());
        assertEquals("find", p.getRoot());
        assertEquals("org.apache.niolex.address.Test", p.getService());
        assertEquals(0, p.getVersion());
    }

    @Test
    public void testDecodePathSVerVer1() throws Exception {
        Path p = decodePath("find", "/find/services/org.apache.niolex.address.Test/versions/6");
        assertEquals(Level.SVERSION, p.getLevel());
        assertEquals("find", p.getRoot());
        assertEquals("org.apache.niolex.address.Test", p.getService());
        assertEquals(6, p.getVersion());
    }

    @Test
    public void testDecodePathSVerVer2() throws Exception {
        Path p = decodePath("find", "/find/services/org.apache.niolex.address.Test/versions/8");
        assertEquals(Level.SVERSION, p.getLevel());
        assertEquals("find", p.getRoot());
        assertEquals("org.apache.niolex.address.Test", p.getService());
        assertEquals(8, p.getVersion());
        assertNull(p.getState());
    }

    @Test
    public void testDecodePathState1() throws Exception {
        Path p = decodePath("find", "/find/services/org.apache.niolex.address.Test/versions/8/sha1");
        assertEquals(Level.STATE, p.getLevel());
        assertEquals("find", p.getRoot());
        assertEquals("org.apache.niolex.address.Test", p.getService());
        assertEquals(8, p.getVersion());
        assertEquals("sha1", p.getState());
        assertNull(p.getNode());
    }

    @Test
    public void testDecodePathState2() throws Exception {
        Path p = decodePath("find", "/find/services/org.apache.niolex.address.Test/versions/8/rsa");
        assertEquals(Level.STATE, p.getLevel());
        assertEquals("find", p.getRoot());
        assertEquals("org.apache.niolex.address.Test", p.getService());
        assertEquals(8, p.getVersion());
        assertEquals("rsa", p.getState());
        assertNull(p.getNode());
    }

    @Test
    public void testDecodePathNode1() throws Exception {
        Path p = decodePath("find", "/find/services/org.apache.niolex.address.Test/versions/8/rsa/127.0.0.1:3721");
        assertEquals(Level.NODE, p.getLevel());
        assertEquals("find", p.getRoot());
        assertEquals("org.apache.niolex.address.Test", p.getService());
        assertEquals(8, p.getVersion());
        assertEquals("rsa", p.getState());
        assertEquals("127.0.0.1:3721", p.getNode());
    }

    @Test
    public void testDecodePathNode2() throws Exception {
        Path p = decodePath("find", "/find/services/org.apache.niolex.address.Test/versions/8/rsa/www.baidu.com/server1");
        assertEquals(Level.NODE, p.getLevel());
        assertEquals("find", p.getRoot());
        assertEquals("org.apache.niolex.address.Test", p.getService());
        assertEquals(8, p.getVersion());
        assertEquals("rsa", p.getState());
        assertEquals("www.baidu.com", p.getNode());
    }

    @Test
    public void testDecodePathCVer() throws Exception {
        Path p = decodePath("find", "/find/services/org.apache.niolex.address.Test/clients");
        assertEquals(Level.SER_CLI, p.getLevel());
        assertEquals("find", p.getRoot());
        assertEquals("org.apache.niolex.address.Test", p.getService());
        assertEquals(0, p.getVersion());
    }

    @Test
    public void testDecodePathCVerVer1() throws Exception {
        Path p = decodePath("find", "/find/services/org.apache.niolex.address.Test/clients/7");
        assertEquals(Level.CVERSION, p.getLevel());
        assertEquals("find", p.getRoot());
        assertEquals("org.apache.niolex.address.Test", p.getService());
        assertEquals(7, p.getVersion());
        assertNull(p.getClient());
    }

    @Test
    public void testDecodePathCVerVer2() throws Exception {
        Path p = decodePath("/find", "/find/services/org.apache.niolex.address.Test/clients/9");
        assertEquals(Level.CVERSION, p.getLevel());
        assertEquals("find", p.getRoot());
        assertEquals("org.apache.niolex.address.Test", p.getService());
        assertEquals(9, p.getVersion());
        assertNull(p.getClient());
    }

    @Test
    public void testDecodePathClient1() throws Exception {
        Path p = decodePath("find", "/find/services/org.apache.niolex.address.Test/clients/9/find-cli");
        assertEquals(Level.CLIENT, p.getLevel());
        assertEquals("find", p.getRoot());
        assertEquals("org.apache.niolex.address.Test", p.getService());
        assertEquals(9, p.getVersion());
        assertEquals("find-cli", p.getClient());
    }

    @Test
    public void testDecodePathClient2() throws Exception {
        Path p = decodePath("/find", "/find/services/org.apache.niolex.address.Test/clients/9/nitofy-cli/new-data");
        assertEquals(Level.CLIENT, p.getLevel());
        assertEquals("find", p.getRoot());
        assertEquals("org.apache.niolex.address.Test", p.getService());
        assertEquals(9, p.getVersion());
        assertEquals("nitofy-cli", p.getClient());
    }

    @Test
    public void testDecodePathVerOther() throws Exception {
        Path p = decodePath("find", "/find/services/org.apache.niolex.address.Test/nobody");
        assertEquals(Level.SER_OTHER, p.getLevel());
        assertEquals("find", p.getRoot());
        assertEquals("org.apache.niolex.address.Test", p.getService());
        assertEquals(0, p.getVersion());
    }

    @Test
    public void testDecodePathLevel() throws Exception {
        assertEquals(Level.SER_OTHER, Level.valueOf("SER_OTHER"));
        assertEquals("CLIENT", Level.CLIENT.toString());
    }

}
