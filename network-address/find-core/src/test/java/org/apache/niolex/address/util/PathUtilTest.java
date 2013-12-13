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

    /**
     * Test method for {@link org.apache.niolex.address.util.PathUtil#validateVersion(java.lang.String)}.
     */
    @Test
    public void testValidateVersion() {
        Result res = PathUtil.validateVersion("3-700");
        System.out.println(res);
        assertEquals("{V?true, R?true, [3, 700)}", res.toString());
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

}
