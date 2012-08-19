/**
 * RpcServiceImpl.java
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
package org.apache.niolex.rpc.json;

import java.util.List;

import org.apache.niolex.commons.util.DateTimeUtil;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-2
 */
public class RpcServiceImpl implements RpcService {

	/**
	 * This is the override of super method.
	 * @see org.apache.niolex.network.demo.rpc.RpcService#add(int[])
	 */
	@Override
	public int add(int... args) {
		int k = 0;
		for (int i : args) {
			k += i;
		}
		return k;
	}

	/**
	 * This is the override of super method.
	 * @see org.apache.niolex.network.demo.rpc.RpcService#size(java.util.List)
	 */
	@Override
	public int size(List<String> arg) {
		if (arg != null) {
			return arg.size();
		}
		return 0;
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.demo.rpc.RpcService#throwEx()
	 */
	@Override
	public String throwEx() {
		throw new RuntimeException("org.apache.niolex.network.demo.rpc.RpcService#throwEx()");
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.demo.rpc.RpcService#concat(java.lang.String, java.lang.String)
	 */
	@Override
	public String concat(String a, String b) {
		return a + b;
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.demo.rpc.RpcService#testMe()
	 */
	@Override
	public void testMe() {
		System.out.println("TestMe called at " + DateTimeUtil.formatDate2DateTimeStr());
	}

}
