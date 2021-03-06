/**
 * RpcServiceFactory.java
 *
 * Copyright 2011 Niolex, Inc.
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

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.niolex.network.cli.RetryHandler;
import org.apache.niolex.network.cli.conf.BaseConfiger;
import org.apache.niolex.network.cli.conf.RpcConfigBean;
import org.apache.niolex.network.cli.conf.RpcConfiger;
import org.apache.niolex.network.cli.handler.IServiceHandler;
import org.apache.niolex.network.rpc.anno.RpcConfig;
import org.apache.niolex.network.rpc.cli.RpcStub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create RPC Service Stub from this Factory.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0, Date: 2012-6-3
 */
public class RpcServiceFactory {
	private static final Logger LOG = LoggerFactory.getLogger(RpcServiceFactory.class);

	/**
	 * Create a new factory with a property file.
	 *
	 * @param fileName the property file path
	 * @return the created instance for this property file
	 * @throws IOException if I / O related error occurred
	 */
	public static final RpcServiceFactory getInstance(String fileName) throws IOException {
	    return new RpcServiceFactory(fileName);
	}

	private Map<String, RetryHandler<IServiceHandler>> handlers = new HashMap<String, RetryHandler<IServiceHandler>>();
	private RpcConfiger configer;

	/**
	 * Init this Factory with a property file.
	 *
	 * @param fileName the property file path
	 * @throws IOException if I / O related error occurred
	 */
	protected RpcServiceFactory(String fileName) throws IOException {
		configer = new RpcConfiger(fileName);
		Map<String, RpcConfigBean> confs = configer.getConfigs();
		for (Entry<String, RpcConfigBean> entry : confs.entrySet()) {
			RpcConfigBean conf = entry.getValue();
			RetryHandler<IServiceHandler> handler = RpcInitUtil.buildProxy(conf);
			handlers.put(entry.getKey(), handler);
			StringBuilder sb = new StringBuilder();
			sb.append("\n===>Api server list for [" + entry.getKey() + "]:\n");
				sb.append("    ").append(handler).append("\n");
			LOG.info(sb.toString());
		}
	}

	/**
	 * Create a service stub by this specified groupName.
	 *
	 * @param groupName the config group name to use
	 * @param c the rpc service interface
	 * @return the created rpc service stub
	 * @throws IllegalArgumentException If Rpc server config not found for your interface
	 */
	@SuppressWarnings("unchecked")
    public <T> T getService(String groupName, Class<T> c) {
		RetryHandler<IServiceHandler> rh = handlers.get(groupName);
	    if (rh == null)
	        throw new IllegalArgumentException("Rpc server config not found for your interface!");
	    /**
	     * Register this interface into the backed RpcClient.
	     * Or otherwise you can't invoke methods on this interface at all.
	     */
    	List<IServiceHandler> list = rh.getHandlers();
    	for (IServiceHandler is : list) {
    		InvocationHandler h = is.getHandler();
            if (h instanceof RpcStub) {
                RpcStub rpc = (RpcStub) h;
    			rpc.addInferface(c);
    		}
    	}
		return (T) Proxy.newProxyInstance(RpcServiceFactory.class.getClassLoader(),
                new Class[] {c}, rh);
	}

	/**
	 * Create a service stub, parse config from annotation.
	 *
	 * @param c the rpc service interface
	 * @return the created rpc service stub
	 */
	public <T> T getService(Class<T> c) {
	    if (c.isAnnotationPresent(RpcConfig.class)) {
	        String groupName = c.getAnnotation(RpcConfig.class).value();
	        LOG.info("Use config [" + groupName + "] for interface [" + c.getName() + "].");
	        return getService(groupName, c);
	    } else {
    	    LOG.info("Annotation not found for interface [" + c.getName() + "], default config is used instead.");
    		return getService(BaseConfiger.DEFAULT, c);
	    }
	}

	/**
	 * Get the internal configer.
	 *
	 * @return the internal configer.
	 */
	public RpcConfiger getConfiger() {
		return configer;
	}
}
