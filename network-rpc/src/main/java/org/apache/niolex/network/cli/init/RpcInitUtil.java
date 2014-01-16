/**
 * RpcInitUtil.java
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
package org.apache.niolex.network.cli.init;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.niolex.network.cli.IServiceHandler;
import org.apache.niolex.network.cli.RetryHandler;
import org.apache.niolex.network.cli.conf.RpcConfigBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create RPC Client Side Proxy from configuration.
 * We use {@link RetryHandler} to handle client side load balance and fail over.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0, Date: 2012-6-3
 */
public class RpcInitUtil {
	private static final Logger LOG = LoggerFactory.getLogger(RpcInitUtil.class);

	/**
	 * Create RPC Client Side Proxy from configuration.
	 *
	 * @param conf the configuration bean
	 * @return the built retry handler
	 */
	public static RetryHandler buildProxy(RpcConfigBean conf) {
		LOG.info("Start to build rpc proxy: [serverList=" + Arrays.toString(conf.serverList) + ", serviceUrl="
				+ conf.serviceUrl + ", header=" + conf.getHeader() + "]");
		List<IServiceHandler> listHandlers = new ArrayList<IServiceHandler>();
		int serverNum = conf.serverList.length;
		String completeUrl = "";
		for (int i = 0; i < serverNum; ++i) {
			completeUrl = conf.serverList[i] + conf.serviceUrl;
			try {
				ServiceHandlerBuilder builder = ServiceHandlerFactory.getBuilder(conf.serviceType);
				IServiceHandler cli = builder.build(conf, completeUrl);
				listHandlers.add(cli);
			} catch (Exception e) {
				LOG.warn("Failed to build rpc proxy for " + completeUrl + " : " + e.toString());
			}
		}
		if (listHandlers.isEmpty()) {
			throw new IllegalStateException("No rpc server is ready for service: " + conf.getGroupName());
		}

		return new RetryHandler(listHandlers, conf.rpcErrorRetryTimes, conf.rpcSleepBetweenRetry);
	}

}
