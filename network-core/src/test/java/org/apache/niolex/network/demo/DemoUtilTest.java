/**
 * DemoUtilTest.java
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
package org.apache.niolex.network.demo;


import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2013-11-8
 */
public class DemoUtilTest extends DemoUtil {

    @Test
    public void testParseArgs1() throws Exception {
        parseArgs(null);
        assertEquals(-1, LAST);
    }

    @Test(expected=NumberFormatException.class)
    public void testParseArgs2() throws Exception {
        parseArgs(new String[] {"not yet implemented"});
    }

    @Test
    public void testParseArgs3() throws Exception {
        parseArgs(new String[] {"-p", "5868"});
    }

    @Test
    public void testParseArgs4() throws Exception {
        System.out.println("not yet implemented");
    }

    @Test
    public void testParseArgs5() throws Exception {
        System.out.println("not yet implemented");
    }

    @Test
    public void testParseArgs6() throws Exception {
        System.out.println("not yet implemented");
    }

    @Test
    public void testParseArgs7() throws Exception {
        System.out.println("not yet implemented");
    }

    @Test
    public void testParseArgs8() throws Exception {
        System.out.println("not yet implemented");
    }

}
