/**
 * RpcMethodTest.java
 *
 * Copyright 2012 Niolex, Inc.
 *
 * Niolex licenses this file to you under the Apache License, version 2.0
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
package org.apache.niolex.network.rpc.anno;

import static org.junit.Assert.*;

import java.lang.reflect.Method;

import org.apache.niolex.commons.reflect.MethodUtil;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-11-8
 */
public class RpcMethodTest {

	/**
	 * Test method for {@link org.apache.niolex.network.rpc.anno.RpcMethod#value()}.
	 */
	@Test
	@RpcMethod(23)
	public void testValue() {
		Method m = MethodUtil.getFirstMethod(getClass(), "testValue");
		RpcMethod r = m.getAnnotation(RpcMethod.class);
		assertEquals(23, r.value());
	}

}
