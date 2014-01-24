/**
 * RpcExposeTest.java
 *
 * Copyright 2014 the original author or authors.
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
package org.apache.niolex.address.rpc.svr;


import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2014-1-24
 */
public class RpcExposeTest {

    @Test
    public void testRpcExposeObject() throws Exception {
        RpcExpose exp = new RpcExpose("bsp");
        assertEquals("bsp", exp.getTarget());
    }

    @Test
    public void testGetInterfaze() throws Exception {
        System.out.println("not yet implemented");
    }

    @Test
    public void testSetInterfaze() throws Exception {
        System.out.println("not yet implemented");
    }

    @Test
    public void testGetTarget() throws Exception {
        System.out.println("not yet implemented");
    }

    @Test
    public void testSetTarget() throws Exception {
        System.out.println("not yet implemented");
    }

    @Test
    public void testGetState() throws Exception {
        System.out.println("not yet implemented");
    }

    @Test
    public void testSetState() throws Exception {
        System.out.println("not yet implemented");
    }

    @Test
    public void testGetWeight() throws Exception {
        System.out.println("not yet implemented");
    }

    @Test
    public void testSetWeight() throws Exception {
        System.out.println("not yet implemented");
    }

    @Test
    public void testGetServiceName() throws Exception {
        System.out.println("not yet implemented");
    }

    @Test
    public void testSetServiceName() throws Exception {
        System.out.println("not yet implemented");
    }

    @Test
    public void testGetServiceType() throws Exception {
        System.out.println("not yet implemented");
    }

    @Test
    public void testSetServiceType() throws Exception {
        System.out.println("not yet implemented");
    }

    @Test
    public void testGetVersion() throws Exception {
        System.out.println("not yet implemented");
    }

    @Test
    public void testSetVersion() throws Exception {
        System.out.println("not yet implemented");
    }

}
