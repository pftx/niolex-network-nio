/**
 * RpcInterfaceTest.java
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
package org.apache.niolex.address.rpc;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2014-1-24
 */
public class RpcInterfaceTest {

    RpcInterface inter;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        inter = DemoService.class.getAnnotation(RpcInterface.class);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testServiceType() throws Exception {
        assertEquals("network/json", inter.serviceType());
    }

    @Test
    public void testServiceName() throws Exception {
        assertEquals("", inter.serviceName());
    }

    @Test
    public void testVersion() throws Exception {
        assertEquals("1.0.0.001", inter.version());
    }

}
