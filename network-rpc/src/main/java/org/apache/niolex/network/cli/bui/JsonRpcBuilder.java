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
import java.net.URI;

import org.apache.niolex.network.cli.IServiceHandler;
import org.apache.niolex.network.cli.RpcServiceHandler;
import org.apache.niolex.network.cli.conf.RpcConfigBean;
import org.apache.niolex.network.cli.init.ServiceHandlerBuilder;
import org.apache.niolex.network.client.PacketClient;
import org.apache.niolex.network.rpc.PacketInvoker;
import org.apache.niolex.network.rpc.RpcClient;
import org.apache.niolex.network.rpc.ser.JsonConverter;

/**
 * The Json Rpc client Factory.
 * Create RpcClient deal with Json internally.
 * We use {@link org.apache.niolex.network.client.PacketClient} here, so one connection
 * can be used in multiple threads.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0, Date: 2012-6-4
 */
public class JsonRpcBuilder implements ServiceHandlerBuilder {

	/**
	 * Override super method
	 * @see org.apache.niolex.network.cli.init.ServiceHandlerBuilder#build(org.apache.niolex.network.cli.conf.RpcConfigBean, java.lang.String)
	 */
	@Override
	public IServiceHandler build(RpcConfigBean bean, String completeUrl) throws Exception {
		URI u = new URI(completeUrl);
		PacketClient pc = new PacketClient(new InetSocketAddress(u.getHost(), u.getPort()));
		PacketInvoker invoker = new PacketInvoker();
		RpcClient cli = new RpcClient(pc, invoker, new JsonConverter());
		cli.setConnectTimeout(bean.connectTimeout);
		cli.setConnectRetryTimes(bean.connectRetryTimes);
		cli.setSleepBetweenRetryTime(bean.connectSleepBetweenRetry);
		invoker.setRpcHandleTimeout(bean.rpcTimeout);
		// Try to connect now.
		cli.connect();
		// Ready to return.
		return new RpcServiceHandler(completeUrl, cli, bean.rpcErrorBlockTime, true);
	}

}
