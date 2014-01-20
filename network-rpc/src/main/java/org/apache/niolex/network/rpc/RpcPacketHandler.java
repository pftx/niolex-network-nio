/**
 * RpcPacketHandler.java
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
package org.apache.niolex.network.rpc;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.niolex.commons.reflect.MethodUtil;
import org.apache.niolex.network.Config;
import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.rpc.anno.RpcMethod;
import org.apache.niolex.network.rpc.util.RpcUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Server side RPC packet handler.
 * We use and internal thread pool to invoke real methods on server side
 * target, and use a queue size to track the server status.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0, Date: 2012-6-1
 */
public class RpcPacketHandler implements IPacketHandler {
	protected static final Logger LOG = LoggerFactory.getLogger(RpcPacketHandler.class);

	// The Thread pool size, default to 5 * CPU, which is the majority configurations on servers.
    private final ExecutorService tPool;

    // Save all the mapping between packet code and real method instance.
	private final Map<Short, RpcExecuteItem> executeMap = new HashMap<Short, RpcExecuteItem>();

	// The current queue size.
	private final AtomicInteger queueSize = new AtomicInteger(0);

	// The data translator.
	private IConverter converter;


	/**
	 * Create an RpcPacketHandler with default pool size.
	 */
	public RpcPacketHandler() {
		int threadsNumber = Runtime.getRuntime().availableProcessors() * 5;
		if (threadsNumber > Config.RPC_HANDLER_POOL_SIZE) {
			threadsNumber = Config.RPC_HANDLER_POOL_SIZE;
		}
		tPool = Executors.newFixedThreadPool(threadsNumber);
		LOG.info("RpcPacketHandler started to work with {} threads.", threadsNumber);
	}

	/**
	 * Create an RpcPacketHandler with the specified pool size.
	 *
	 * @param threadsNumber the threads number of the execute pool
	 */
	public RpcPacketHandler(int threadsNumber) {
		super();
		tPool = Executors.newFixedThreadPool(threadsNumber);
		LOG.info("RpcPacketHandler started to work with {} threads.", threadsNumber);
	}

	/**
	 * Create an RpcPacketHandler with the specified pool size & converter.
	 *
	 * @param threadsNumber the threads number of the execute pool
	 * @param converter the packet converter
	 */
	public RpcPacketHandler(int threadsNumber, IConverter converter) {
		super();
		this.tPool = Executors.newFixedThreadPool(threadsNumber);
		this.converter = converter;
		LOG.info("RpcPacketHandler started to work with {} threads.", threadsNumber);
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.IPacketHandler#handlePacket(org.apache.niolex.network.PacketData, org.apache.niolex.network.IPacketWriter)
	 */
	@Override
	public void handlePacket(PacketData sc, IPacketWriter wt) {
		// Heart beat will be handled in FastCore, so we will not encounter it here.
		RpcExecuteItem ei = executeMap.get(sc.getCode());
		if (ei != null) {
			RpcExecute re = new RpcExecute(ei.getTarget(), ei.getMethod(), sc, wt);
			queueSize.incrementAndGet();
			tPool.execute(re);
		} else {
		    RpcException rep = new RpcException("The method you want to invoke doesn't exist.",
					RpcException.Type.METHOD_NOT_FOUND, null);
			handleReturn(sc, wt, rep, 1);
		}
	}

	/**
	 * Internal usage, Run the detailed Rpc execution.
	 *
	 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
	 * @version 1.0.0, Date: 2012-6-1
	 */
	private class RpcExecute implements Runnable {
		private Object host;
		private Method method;
		private PacketData sc;
		private IPacketWriter wt;

		/**
		 * The only constructor.
		 *
		 * @param host the host object for the method to be invoked
		 * @param method the method to be invoked
		 * @param sc the parameter packet
		 * @param wt the result packet writer
		 */
		public RpcExecute(Object host, Method method, PacketData sc, IPacketWriter wt) {
			super();
			this.host = host;
			this.method = method;
			this.sc = sc;
			this.wt = wt;
		}

		/**
		 * Override super method
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			try {
				RpcException rep = null;
				Object[] args = null;
				try {
				    Type[] generic = method.getGenericParameterTypes();
				    if (generic != null && generic.length > 0) {
				        args = converter.prepareParams(sc.getData(), generic);
				    }
				} catch (Exception e1) {
				    rep = new RpcException("Error occured when prepare params.",
				            RpcException.Type.ERROR_PARSE_PARAMS, e1);
				    handleReturn(sc, wt, rep, 1);
				    return;
				}
				try {
				    Object ret = method.invoke(host, args);
				    handleReturn(sc, wt, ret, 0);
				} catch (Exception e1) {
				    rep = new RpcException("Error occured when invoke method.",
				            RpcException.Type.ERROR_INVOKE, e1);
				    handleReturn(sc, wt, rep, 1);
				}
			} finally {
				LOG.debug("Packet handled. key {}, queue size {}.", RpcUtil.generateKey(sc),
						queueSize.decrementAndGet());
			}
		}

	}// End of RpcExecute

	/**
	 * Handle how to generate return data and send them to client.
	 *
	 * @param sc the input packet
	 * @param wt the packet writer
	 * @param data the object want to serialize
	 * @param exception 0 for result, 1 for exception
	 */
	private void handleReturn(PacketData sc, IPacketWriter wt, Object data, int exception) {
		try {
			byte[] arr = null;
			if (data == null) {
				arr = new byte[0];
			} else {
				arr = converter.serializeReturn(data);
			}
			PacketData rc = new PacketData(sc.getCode(), arr);
			rc.setReserved((byte) (sc.getReserved() + exception));
			rc.setVersion(sc.getVersion());
			wt.handleWrite(rc);
		} catch (Exception e) {
			LOG.warn("Error occured when handle return. {}", e.toString());
		}
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.IPacketHandler#handleClose(org.apache.niolex.network.IPacketWriter)
	 */
	@Override
	public void handleClose(IPacketWriter wt) {
		/**
		 * Error is not a big deal.
		 * Those methods not finished will still run, but client can not get their answers.
		 */
	}

	/**
	 * Set the Rpc Configurations, this method will parse all the configurations and generate the execute map.
	 * One can call this method repeatedly.
	 *
	 * @param confs the configuration item array
	 */
	public void setRpcConfigs(ConfigItem[] confs) {
		for (ConfigItem conf : confs) {
		    addRpcConfig(conf);
		}
	}

	/**
	 * Add the Rpc Config, this method will parse the configuration and generate execute map.
     * One can call this method repeatedly to add more Rpc Config.
     *
	 * @param conf the configuration item
	 */
	public void addRpcConfig(ConfigItem conf) {
	    List<Method> methods = MethodUtil.getMethods(conf.getInterface());
	    addRpcConfig(methods, conf.getTarget());
	}

	/**
	 * Parse all the methods of this target, find rpc methods and add them into the
	 * execute map.
	 *
	 * @param target the target object
	 */
	public void addRpcConfig(Object target) {
	    List<Method> methods = MethodUtil.getAllMethodsIncludeInterfaces(target.getClass());
        addRpcConfig(methods, target);
	}

	/**
	 * Parse this methods list, find rpc methods and generate execute map.
	 *
	 * @param methods the methods list to be checked
	 * @param target the target object
	 */
    public void addRpcConfig(List<Method> methods, Object target) {
        for (Method m : methods) {
            if (m.isAnnotationPresent(RpcMethod.class)) {
                RpcMethod rp = m.getAnnotation(RpcMethod.class);
                RpcExecuteItem rei = new RpcExecuteItem();
                rei.setMethod(m);
                rei.setTarget(target);
                rei = executeMap.put(rp.value(), rei);
                if (rei != null) {
                    LOG.warn("Duplicate configuration for code: {}, old method: {}, new method: {}.",
                            rp.value(), rei.getMethod(), m);
                }
            }
        }
	}

	/**
	 * Get the number of methods invocations we are handling currently.
	 *
	 * @return current queue size
	 */
	public int getQueueSize() {
		return queueSize.get();
	}

	/**
	 * Set the converter to translate data.
	 *
	 * @param converter the packet data converter
	 */
	public void setConverter(IConverter converter) {
		this.converter = converter;
	}

}
