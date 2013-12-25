/**
 * EnvironmentTest.java
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
package org.apache.niolex.address.optool;


import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2013-12-25
 */
public class EnvironmentTest {
    Environment e = Environment.getInstance();

    @Test
    public void testGetInstance() throws Exception {
        assertEquals(Environment.EVN, Environment.getInstance());
    }

    @Test
    public void testParseOptions() throws Exception {
        assertEquals(Environment.LoginType.CLI, Environment.LoginType.valueOf("CLI"));
        assertEquals(Environment.LoginType.OP.toString(), "OP");
    }

    @Test
    public void testValidate() throws Exception {
        assertFalse(Environment.getInstance().validate());
        Environment.getInstance().root = "a";
        assertFalse(Environment.getInstance().validate());
        Environment.getInstance().userName = "b";
        assertTrue(Environment.getInstance().validate());
    }

    @Test
    public void testParseCommand() throws Exception {
        e.curPath = "/r";
        String s = e.getAbsolutePath("a/b");
        assertEquals("/r/a/b", s);
    }

    @Test
    public void testGetAbsolutePath() throws Exception {
        e.curPath = "/r";
        String s = e.getAbsolutePath("./a/b");
        assertEquals("/r/a/b", s);
    }

    @Test
    public void testCdUp() throws Exception {
        e.curPath = "/r/s/t";
        e.cdUp();
        String s = e.getAbsolutePath("../a/b");
        assertEquals("/r/a/b", s);
    }

}
