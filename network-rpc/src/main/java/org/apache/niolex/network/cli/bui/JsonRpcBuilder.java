/**
 * JsonRpcBuilder.java
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
package org.apache.niolex.network.cli.bui;

import java.net.InetSocketAddress;

import org.apache.niolex.network.cli.init.RpcClientBuilder;
import org.apache.niolex.network.client.PacketClient;
import org.apache.niolex.network.rpc.PacketInvoker;
import org.apache.niolex.network.rpc.RpcClient;
import org.apache.niolex.network.rpc.ser.JsonConverter;

/**
 * The Json Rpc client Factory.
 * Create JsonRpcClient internally.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-4
 */
public class JsonRpcBuilder implements RpcClientBuilder {

	private String clientUrl;
	private int connectTimeout;
	private int rpcHandleTimeout;

	/**
	 * Override super method
	 * @see org.apache.niolex.network.cli.init.RpcClientBuilder#setClientUrl(java.lang.String)
	 */
	@Override
	public RpcClientBuilder setClientUrl(String url) {
		this.clientUrl = url;
		return this;
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.cli.init.RpcClientBuilder#setConnectTimeout(int)
	 */
	@Override
	public RpcClientBuilder setConnectTimeout(int time) {
		this.connectTimeout = time;
		return this;
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.cli.init.RpcClientBuilder#setRpcHandleTimeout(int)
	 */
	@Override
	public RpcClientBuilder setRpcHandleTimeout(int time) {
		this.rpcHandleTimeout = time;
		return this;
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.cli.init.RpcClientBuilder#build()
	 */
	@Override
	public RpcClient build() {
		String[] arr = this.clientUrl.split(":");
		if (arr.length != 2) {
			throw new IllegalArgumentException("Invalid url: " + this.clientUrl);
		}
		int port = Integer.parseInt(arr[1]);
		PacketClient pc = new PacketClient(new InetSocketAddress(arr[0], port));
		PacketInvoker invoker = new PacketInvoker();
		invoker.setRpcHandleTimeout(rpcHandleTimeout);
		RpcClient rc = new RpcClient(pc, invoker, new JsonConverter());
		rc.setConnectTimeout(connectTimeout);
		return rc;
	}
}
