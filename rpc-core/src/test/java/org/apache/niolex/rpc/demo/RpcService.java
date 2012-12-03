/**
 * RpcService.java
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
import org.apache.niolex.rpc.RpcMethod;


/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-1
 */
public interface RpcService {

	@RpcMethod(14)
	public Int benchmark(Benchmark ben, String mark);

	@RpcMethod(15)
	public Int size(String[] arg);

	@RpcMethod(16)
	public String throwEx();

	@RpcMethod(17)
	public String concat(String a, String b);

	public void testMe();

	public static class Int {

		public int i;

		public Int() {
			super();
		}

		public Int(int i) {
			super();
			this.i = i;
		}

		public int getI() {
			return i;
		}

		public void setI(int i) {
			this.i = i;
		}

	}
}
