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

import org.apache.niolex.network.cli.conf.RpcConfigBean;
import org.apache.niolex.network.cli.handler.IServiceHandler;
import org.apache.niolex.network.cli.init.ServiceHandlerBuilder;
import org.apache.niolex.network.client.BlockingClient;
import org.apache.niolex.network.client.PacketClient;
import org.apache.niolex.network.rpc.cli.BaseInvoker;
import org.apache.niolex.network.rpc.cli.RpcStub;
import org.apache.niolex.network.rpc.cli.SingleInvoker;
import org.apache.niolex.network.rpc.conv.JsonConverter;

/**
 * The Json Rpc Builder, and also used as the base class for other builders.
 * <br>
 * Create BaseInvoker to deal with Json internally.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0, Date: 2012-6-4
 * @see #buildClient(RpcConfigBean, String)
 */
public class JsonRpcBuilder implements ServiceHandlerBuilder {

    public static final JsonConverter CONV = new JsonConverter();

    /**
     * Build the network connection client and remote invoker according to the configuration.
     *
     * @param bean the configuration bean
     * @param completeUrl the complete URL
     * @return the built network connection client and remote invoker
     * @throws Exception if necessary
     */
    protected BaseInvoker buildClient(RpcConfigBean bean, String completeUrl) throws Exception {
        URI u = new URI(completeUrl);
        InetSocketAddress serverAddress = new InetSocketAddress(u.getHost(), u.getPort());

        BaseInvoker invoker = null;
        switch (bean.clientType.charAt(0)) {
            case 'P':
            case 'p':
                invoker = new BaseInvoker(new PacketClient(serverAddress));
                break;
            case 'S':
            case 's':
                invoker = new SingleInvoker(serverAddress);
                break;
            case 'B':
            default:
                invoker = new BaseInvoker(new BlockingClient(serverAddress));
        }
        invoker.setRpcHandleTimeout(bean.rpcTimeout);
        invoker.setConnectTimeout(bean.connectTimeout);
        invoker.setConnectRetryTimes(bean.connectRetryTimes);
        invoker.setSleepBetweenRetryTime(bean.connectSleepBetweenRetry);


        return invoker;
    }

	/**
	 * Override super method
	 * @see org.apache.niolex.network.cli.init.ServiceHandlerBuilder#build(RpcConfigBean, String)
	 */
	@Override
	public IServiceHandler build(RpcConfigBean bean, String completeUrl) throws Exception {
        BaseInvoker invoker = buildClient(bean, completeUrl);
        // Try to connect now.
        invoker.connect();

		// Ready to return.
        return new RpcStub(invoker, CONV);
	}

}
