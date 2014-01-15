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

import org.apache.niolex.commons.bean.Pair;
import org.apache.niolex.network.IClient;
import org.apache.niolex.network.cli.IServiceHandler;
import org.apache.niolex.network.cli.RpcClientHandler;
import org.apache.niolex.network.cli.conf.RpcConfigBean;
import org.apache.niolex.network.cli.init.ServiceHandlerBuilder;
import org.apache.niolex.network.client.BlockingClient;
import org.apache.niolex.network.client.PacketClient;
import org.apache.niolex.network.client.SocketClient;
import org.apache.niolex.network.rpc.PacketInvoker;
import org.apache.niolex.network.rpc.RemoteInvoker;
import org.apache.niolex.network.rpc.RpcClient;
import org.apache.niolex.network.rpc.SingleInvoker;
import org.apache.niolex.network.rpc.conv.JsonConverter;

/**
 * The Json Rpc client Builder, and also used as the base class for other builders.
 * <br>
 * Create RpcClient deal with Json internally.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0, Date: 2012-6-4
 * @see #buildClient(RpcConfigBean, String)
 */
public class JsonRpcBuilder implements ServiceHandlerBuilder {

    /**
     * Build the network connection client and remote invoker according to the configuration.
     *
     * @param bean the configuration bean
     * @param completeUrl the complete URL
     * @return the built network connection client and remote invoker
     * @throws Exception if necessary
     */
    protected Pair<IClient, RemoteInvoker> buildClient(RpcConfigBean bean, String completeUrl) throws Exception {
        URI u = new URI(completeUrl);
        InetSocketAddress serverAddress = new InetSocketAddress(u.getHost(), u.getPort());

        IClient client = null;
        RemoteInvoker invoker = null;
        switch (bean.clientType.charAt(0)) {
            case 'P':
            case 'p':
                client = new PacketClient(serverAddress);
                invoker = new PacketInvoker();
                break;
            case 'S':
            case 's':
                client = new SocketClient(serverAddress);
                invoker = new SingleInvoker();
                break;
            case 'B':
            default:
                client = new BlockingClient(serverAddress);
                invoker = new PacketInvoker();
        }
        invoker.setRpcHandleTimeout(bean.rpcTimeout);
        return Pair.create(client, invoker);
    }

	/**
	 * Override super method
	 * @see org.apache.niolex.network.cli.init.ServiceHandlerBuilder#build(RpcConfigBean, String)
	 */
	@Override
	public IServiceHandler build(RpcConfigBean bean, String completeUrl) throws Exception {
	    Pair<IClient, RemoteInvoker> p = buildClient(bean, completeUrl);

		RpcClient cli = new RpcClient(p.a, p.b, new JsonConverter());
		cli.setConnectTimeout(bean.connectTimeout);
		cli.setConnectRetryTimes(bean.connectRetryTimes);
		cli.setSleepBetweenRetryTime(bean.connectSleepBetweenRetry);

		// Try to connect now.
		cli.connect();
		// Ready to return.
		return new RpcClientHandler(completeUrl, cli);
	}

}
