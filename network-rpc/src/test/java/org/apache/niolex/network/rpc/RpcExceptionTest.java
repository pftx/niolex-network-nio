/**
 * RpcExceptionTest.java
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
package org.apache.niolex.network.rpc;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5, $Date: 2012-11-14$
 */
public class RpcExceptionTest {

	/**
	 * Test method for {@link org.apache.niolex.network.rpc.RpcException#getType()}.
	 */
	@Test
	public void testGetType() {
		RpcException r = new RpcException();
		r.setType(RpcException.Type.ERROR_PARSE_PARAMS);
		assertEquals(RpcException.Type.ERROR_PARSE_PARAMS, r.getType());
	}

	@Test
	public void testGetExplanation()
	 throws Exception {
	    assertEquals(RpcException.Type.valueOf("ERROR_INVOKE").getExplanation(), "Error occured when server invoke this method on site.");
		assertEquals(RpcException.Type.ERROR_INVOKE.getExplanation(), "Error occured when server invoke this method on site.");
	}

}
