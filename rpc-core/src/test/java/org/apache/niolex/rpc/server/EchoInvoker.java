/**
 * EchoInvoker.java
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
package org.apache.niolex.rpc.server;

import org.apache.niolex.network.Packet;
import org.apache.niolex.rpc.core.Invoker;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-8-19
 */
public class EchoInvoker implements Invoker {

	/**
	 * This is the override of super method.
	 * @see org.apache.niolex.rpc.core.Invoker#process(org.apache.niolex.network.Packet)
	 */
	@Override
	public Packet process(Packet sc) {
		return sc;
	}

}
