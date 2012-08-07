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
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.niolex.commons.reflect.MethodUtil;
import org.apache.niolex.network.Config;
import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Server side RPC packet handler.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-1
 */
public abstract class RpcPacketHandler implements IPacketHandler {
	protected static final Logger LOG = LoggerFactory.getLogger(RpcPacketHandler.class);

	// The Thread pool size, default to 20, which is the majority configurations on servers.
    private ExecutorService tPool;
	private Map<Short, RpcExecuteItem> executeMap = new HashMap<Short, RpcExecuteItem>();
	// The current queue size.
	private AtomicInteger queueSize = new AtomicInteger(0);


	/**
	 * Create an RpcPacketHandler with default pool size.
	 * Constructor
	 */
	public RpcPacketHandler() {
		int threadsNumber = Runtime.getRuntime().availableProcessors() * 4;
		if (threadsNumber > Config.RPC_HANDLER_POOL_SIZE) {
			threadsNumber = Config.RPC_HANDLER_POOL_SIZE;
		}
		tPool = Executors.newFixedThreadPool(threadsNumber);
		LOG.info("RpcPacketHandler started to work with {} threads.", threadsNumber);
	}

	/**
	 * Create an RpcPacketHandler with the specified pool size.
	 * Constructor
	 * @param threadsNumber
	 */
	public RpcPacketHandler(int threadsNumber) {
		super();
		tPool = Executors.newFixedThreadPool(threadsNumber);
		LOG.info("RpcPacketHandler started to work with {} threads.", threadsNumber);
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.IPacketHandler#handleRead(org.apache.niolex.network.PacketData, org.apache.niolex.network.IPacketWriter)
	 */
	@Override
	public void handleRead(PacketData sc, IPacketWriter wt) {
		if (sc.getCode() == Config.CODE_HEART_BEAT) {
			// This is heart beat, just return.
			return;
		}
		RpcExecuteItem ei = executeMap.get(sc.getCode());
		RpcException rep = null;
		if (ei != null) {
			RpcExecute re = new RpcExecute(ei.getTarget(), ei.getMethod(), sc, wt);
			queueSize.incrementAndGet();
			tPool.execute(re);
		} else {
			rep = new RpcException("The method you want to invoke doesn't exist.",
					RpcException.Type.METHOD_NOT_FOUND, null);
			handleReturn(sc, wt, rep, 1);
			return;
		}
	}

	/**
	 * Internal usage, Run the detailed Rpc execution.
	 *
	 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
	 * @version 1.0.0
	 * @Date: 2012-6-1
	 */
	private class RpcExecute implements Runnable {
		private Object host;
		private Method method;
		private PacketData sc;
		private IPacketWriter wt;

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
				execute();
			} finally {
				LOG.debug("Packet handled. key {}, queue size {}.", RpcUtil.generateKey(sc), queueSize.decrementAndGet());
			}
		}

		private void execute() {
			RpcException rep = null;
			Object[] args = null;
			try {
				Type[] generic = method.getGenericParameterTypes();
				if (generic != null && generic.length > 0) {
					args = prepareParams(sc.getData(), generic);
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
				return;
			}
		}


	}

	/**
	 * Handle how to generate return data and send them to client.
	 *
	 * @param sc
	 * @param wt
	 * @param data
	 * @param exception
	 */
	private void handleReturn(PacketData sc, IPacketWriter wt, Object data, int exception) {
		try {
			byte[] arr = null;
			if (data == null) {
				arr = new byte[0];
			} else {
				arr = serializeReturn(data);
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
		// Error is not a big deal.
	}

	/**
	 * Set the Rpc Configs, this method will parse all the configurations and generate execute map.
	 * @param confs
	 */
	public void setRpcConfigs(RpcConfig[] confs) {
		for (RpcConfig conf : confs) {
			Method[] arr = MethodUtil.getMethods(conf.getInterface());
			for (Method m : arr) {
				if (m.isAnnotationPresent(RpcMethod.class)) {
					RpcMethod rp = m.getAnnotation(RpcMethod.class);
					RpcExecuteItem rei = new RpcExecuteItem();
					rei.setMethod(m);
					rei.setTarget(conf.getTarget());
					rei = executeMap.put(rp.value(), rei);
					if (rei != null) {
						LOG.warn("Duplicate configuration for code: {}", rp.value());
					}
				}
			} // End of arr
		} // End of confs
	}

	/**
	 * Read parameters from the data.
	 * generic can not be null, we already checked.
	 *
	 * @param data
	 * @param generic
	 * @return
	 * @throws Exception
	 */
	protected abstract Object[] prepareParams(byte[] data, Type[] generic) throws Exception;

	/**
	 * Serialize returned object into byte array.
	 * ret can not be null, we already checked.
	 *
	 * @param ret
	 * @return
	 * @throws Exception
	 */
	protected abstract byte[] serializeReturn(Object ret) throws Exception;

}
