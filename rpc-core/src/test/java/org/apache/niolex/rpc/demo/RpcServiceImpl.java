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
package org.apache.niolex.rpc.demo;

import org.apache.niolex.commons.test.Benchmark;
import org.apache.niolex.commons.util.DateTimeUtil;

/**
 * The demo implementation.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-2
 */
public class RpcServiceImpl implements RpcService {

	/**
	 * Override super method
	 * @see org.apache.niolex.rpc.demo.RpcService#benchmark(org.apache.niolex.commons.test.Benchmark, java.lang.String)
	 */
	@Override
	public Int benchmark(Benchmark ben, String mark) {
		return new Int(ben.getClassId() + ben.getPriv() + mark.length());
	}

	/**
	 * This is the override of super method.
	 * @see org.apache.niolex.rpc.demo.RpcService#size(String[]))
	 */
	@Override
	public Int size(String[] arg) {
		if (arg != null) {
			return new Int(arg.length);
		}
		return new Int(0);
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.rpc.demo.RpcService#throwEx()
	 */
	@Override
	public String throwEx() {
		throw new RuntimeException("Demo ex throw from #throwEx()");
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.rpc.demo.RpcService#concat(java.lang.String, java.lang.String)
	 */
	@Override
	public String concat(String a, String b) {
		return a + b;
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.rpc.demo.RpcService#testMe()
	 */
	@Override
	public void testMe() {
		System.out.println("TestMe called at " + DateTimeUtil.formatDate2DateTimeStr());
	}

}
