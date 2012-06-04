/**
 * JsonRpcFactory.java
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
package org.apache.niolex.network.rpc.json;

import java.net.InetSocketAddress;

import org.apache.niolex.network.PacketClient;
import org.apache.niolex.network.rpc.RpcClient;
import org.apache.niolex.network.rpc.init.RpcClientFactory;
import org.apache.niolex.network.rpc.init.RpcServiceFactory;

/**
 * The Json Rpc client Factory.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-4
 */
public class JsonRpcFactory extends RpcServiceFactory {

	/**
	 * @param fileName
	 * @param factory
	 */
	protected JsonRpcFactory(String fileName) {
		super(fileName, new JsonRpcClientFactory());
	}

	/**
	 * Get instance of RpcServiceFactory by server address configuration file.
	 *
	 * @param fileName
	 * @return
	 */
	public static final RpcServiceFactory getInstance(String fileName) {
		return new JsonRpcFactory(fileName);
	}

	/**
	 * Create JsonRpcClient internally.
	 *
	 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
	 * @version 1.0.0
	 * @Date: 2012-6-4
	 */
	private static class JsonRpcClientFactory implements RpcClientFactory {

		/**
		 * Override super method
		 * @see org.apache.niolex.network.rpc.init.RpcClientFactory#createRpcClient(java.lang.String)
		 */
		@Override
		public RpcClient createRpcClient(String url) {
			String[] arr = url.split(":");
			if (arr.length != 2) {
				throw new IllegalArgumentException("Invalid url: " + url);
			}
			int port = Integer.parseInt(arr[1]);
			PacketClient pc = new PacketClient(new InetSocketAddress(arr[0], port));
			RpcClient rc = new JsonRpcClient(pc);
			return rc;
		}

	}
}
