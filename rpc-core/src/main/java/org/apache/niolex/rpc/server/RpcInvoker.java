/**
 * RpcInvoker.java
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

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.apache.niolex.commons.reflect.MethodUtil;
import org.apache.niolex.network.Config;
import org.apache.niolex.network.Packet;
import org.apache.niolex.rpc.RpcConfig;
import org.apache.niolex.rpc.RpcException;
import org.apache.niolex.rpc.RpcExecuteItem;
import org.apache.niolex.rpc.RpcMethod;
import org.apache.niolex.rpc.core.Invoker;
import org.apache.niolex.rpc.protocol.IServerProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The base class of Server side RPC packet handler.
 * One can just use it or extend it for special purpose.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-6-1
 */
public class RpcInvoker implements Invoker {
	protected static final Logger LOG = LoggerFactory.getLogger(RpcInvoker.class);

	private Map<Short, RpcExecuteItem> executeMap = new HashMap<Short, RpcExecuteItem>();

	/**
	 * The server side protocol to handle serialization.
	 */
	private IServerProtocol serverProtocol;


	/**
	 * Create a RpcInvoker without protocol.
	 */
	public RpcInvoker() {
		super();
	}

	/**
	 * Create a RpcInvoker with this specified protocol.
	 * @param serverProtocol
	 */
	public RpcInvoker(IServerProtocol serverProtocol) {
		super();
		this.serverProtocol = serverProtocol;
	}

	@Override
	public Packet process(Packet sc) {
		if (sc.getCode() == Config.CODE_HEART_BEAT) {
			// This is heart beat, just return.
			return sc;
		}
		RpcExecuteItem ei = executeMap.get(sc.getCode());
		RpcException rep = null;
		if (ei != null) {
			RpcExecute re = new RpcExecute(ei.getTarget(), ei.getMethod(), sc);
			return re.execute();
		} else {
			rep = new RpcException("The method you want to invoke doesn't exist.",
					RpcException.Type.METHOD_NOT_FOUND, null);
			return handleReturn(sc, rep, 1);
		}
	}

	/**
	 * Internal usage, Run the detailed Rpc execution.
	 *
	 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
	 * @version 1.0.0
	 * @since 2012-6-1
	 */
	private class RpcExecute {
		private Object host;
		private Method method;
		private Packet sc;

		public RpcExecute(Object host, Method method, Packet sc) {
			super();
			this.host = host;
			this.method = method;
			this.sc = sc;
		}

		/**
		 * Do the method execution.
		 * @return
		 */
		private Packet execute() {
			RpcException rep = null;
			Object[] args = null;
			try {
				Type[] generic = method.getGenericParameterTypes();
				if (generic != null && generic.length > 0) {
					args = serverProtocol.prepareParams(sc.getData(), generic);
				}
			} catch (Exception e1) {
				rep = new RpcException("Error occured when prepare params.",
						RpcException.Type.ERROR_PARSE_PARAMS, e1);
				return handleReturn(sc, rep, 1);
			}
			try {
				Object ret = method.invoke(host, args);
				return handleReturn(sc, ret, 0);
			} catch (Exception e1) {
				rep = new RpcException("Error occured when invoke method.",
						RpcException.Type.ERROR_INVOKE, e1);
				return handleReturn(sc, rep, 1);
			}
		}

	}

	/**
	 * Handle how to generate return data.
	 *
	 * @param sc
	 * @param data
	 * @param exception
	 */
	private Packet handleReturn(Packet sc, Object data, int exception) {
		try {
			byte[] arr = null;
			if (data == null) {
				arr = new byte[0];
			} else {
				arr = serverProtocol.serializeReturn(data);
			}
			Packet rc = new Packet(sc.getCode(), arr);
			if (exception != 0) {
				rc.setSerial((short) -sc.getSerial());
			} else {
				rc.setSerial(sc.getSerial());
			}
			return rc;
		} catch (Exception e) {
			LOG.warn("Error occured when handle return. {}", e.toString());
		}
		return null;
	}

	/**
	 * Set the Rpc Configs, this method will parse all the configurations and generate the execute map.
	 * This method can be called multi-times. System will collect all the configurations.
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
	 * Set the server side serialization protocol.
	 * @param serverProtocol
	 */
	public void setServerProtocol(IServerProtocol serverProtocol) {
		this.serverProtocol = serverProtocol;
	}

}
