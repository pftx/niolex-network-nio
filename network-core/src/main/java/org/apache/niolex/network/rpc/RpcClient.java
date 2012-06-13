/**
 * RpcClient.java
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

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.niolex.commons.reflect.MethodUtil;
import org.apache.niolex.network.Config;
import org.apache.niolex.network.IClient;
import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The based RpcClient, send and receive Rpc packets.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-1
 */
public abstract class RpcClient implements InvocationHandler, IPacketHandler {
	private static final Logger LOG = LoggerFactory.getLogger(RpcClient.class);

	/**
	 * The PacketClient to send and receive Rpc packets.
	 */
	private IClient client;

	/**
	 * Save the execution map.
	 */
	private Map<Method, Short> executeMap = new HashMap<Method, Short>();

	/**
	 * The current waiting map.
	 */
	private Map<Integer, RpcWaitItem> waitMap = new ConcurrentHashMap<Integer, RpcWaitItem>();

	/**
	 * The serial generator.
	 */
	private AtomicInteger auto;

	/**
	 * The rpc handle timeout in milliseconds.
	 */
	private int rpcHandleTimeout = Config.RPC_HANDLE_TIMEOUT;

	/**
	 * The time to sleep between retry.
	 */
	private int sleepBetweenRetryTime = Config.RPC_SLEEP_BT_RETRY;

	/**
	 * Times to retry get connected.
	 */
	private int connectRetryTimes = Config.RPC_CONNECT_RETRY_TIMES;

	/**
	 * The status of this Client.
	 */
	private Status connStatus;

	/**
	 * The connections status of this RpcClient.
	 *
	 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
	 * @version 1.0.0
	 * @Date: 2012-6-2
	 */
	public static enum Status {
		INNITIAL, CONNECTED, CLOSED
	}

	/**
	 * Create a RpcClient with this PacketClient as the backed communication tool.
	 * The PacketClient will be managed internally, please use this.connect() to connect.
	 *
	 * Constructor
	 * @param client
	 */
	public RpcClient(IClient client) {
		super();
		this.client = client;
		this.client.setPacketHandler(this);
		this.auto = new AtomicInteger(1);
		this.connStatus = Status.INNITIAL;
	}

	/**
	 * Connect the backed communication PacketClient, and set the internal status.
	 * @throws IOException
	 */
	public void connect() throws IOException {
		this.client.connect();
		this.connStatus = Status.CONNECTED;
	}

	/**
	 * Stop this client, and stop the backed communication PacketClient.
	 */
	public void stop() {
		this.connStatus = Status.CLOSED;
		this.client.stop();
	}

	/**
	 * Get the Rpc Service Client Stub.
	 * @param c The interface you want to invoke.
	 * @return
	 */
	@SuppressWarnings("unchecked")
    public <T> T getService(Class<T> c) {
		this.addInferface(c);
		return (T) Proxy.newProxyInstance(RpcClient.class.getClassLoader(),
                new Class[] {c}, this);
	}

	/**
	 * This is the override of super method.
	 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		RpcException rep = null;
		switch (connStatus) {
			case INNITIAL:
				rep = new RpcException("Client not connected.", RpcException.Type.NOT_CONNECTED, null);
				throw rep;
			case CLOSED:
				rep = new RpcException("Client closed.", RpcException.Type.CONNECTION_CLOSED, null);
				throw rep;
		}
		Short rei = executeMap.get(method);
		if (rei != null) {
			// 1. Prepare parameters
			byte[] arr;
			if (args == null || args.length == 0) {
				arr = new byte[0];
			} else {
				arr = serializeParams(args);
			}
			// 2. Create PacketData
			PacketData rc = new PacketData(rei, arr);
			// 3. Generate serial number
			serialPacket(rc);
			// 4. Set up the waiting information
			RpcWaitItem wi = new RpcWaitItem();
			wi.setThread(Thread.currentThread());
			Integer key = RpcUtil.generateKey(rc);
			waitMap.put(key, wi);
			// 5. Send request to remote server
			client.handleWrite(rc);
			// 6. Wait for result.
			long in = System.currentTimeMillis();
			while (System.currentTimeMillis() - in < rpcHandleTimeout) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// Do not care.
				}
				if (wi.getReceived() != null) {
					break;
				}
			}
			// 7. Clean the map.
			waitMap.remove(key);
			// 8. Process result.
			if (wi.getReceived() == null) {
				switch (connStatus) {
					case INNITIAL:
						rep = new RpcException("Client not connected.", RpcException.Type.NOT_CONNECTED, null);
						throw rep;
					case CLOSED:
						rep = new RpcException("Client closed.", RpcException.Type.CONNECTION_CLOSED, null);
						throw rep;
				}
				rep = new RpcException("Timeout for this remote procedure call.", RpcException.Type.TIMEOUT, null);
				throw rep;
			} else {
				PacketData sc = wi.getReceived();
				int exp = sc.getReserved() - rc.getReserved();
				Object ret = prepareReturn(sc.getData(), method.getGenericReturnType(), exp);
				if (exp == 1) {
					rep = (RpcException) ret;
					throw rep;
				}
				return ret;
			}
		} else {
			rep = new RpcException("The method you want to invoke is not a remote procedure call.",
					RpcException.Type.METHOD_NOT_FOUND, null);
			throw rep;
		}
	}

	/**
	 * Generate serial number
	 * @param rc
	 */
	private void serialPacket(PacketData rc) {
		short seri = (short) (auto.getAndAdd(2));
		rc.setReserved((byte) seri);
		rc.setVersion((byte) (seri >> 8));
	}

	@Override
	public void handleRead(PacketData sc, IPacketWriter wt) {
		if (sc.getCode() == Config.CODE_HEART_BEAT) {
			// This is heart beat, just return.
			return;
		}
		int key = RpcUtil.generateKey(sc);
		RpcWaitItem wi = waitMap.get(key);

		if (wi == null) {
			LOG.warn("Packet received for key [{}] have no handler, just ignored.", key);
		} else {
			waitMap.remove(key);
			wi.setReceived(sc);
			wi.getThread().interrupt();
		}
	}

	@Override
	public void handleClose(IPacketWriter wt) {
		if (this.connStatus == Status.CLOSED) {
			return;
		}
		// We will retry to connect in this method.
		client.stop();
		if (!retryConnect()) {
			LOG.error("Exception occured when try to re-connect to server.");
			// Try to shutdown this Client, inform all the threads.
			this.connStatus = Status.CLOSED;
			for (RpcWaitItem wi : waitMap.values()) {
				wi.getThread().interrupt();
			}
		}
	}

	private boolean retryConnect() {
		for (int i = 0; i < connectRetryTimes; ++i) {
			try {
				Thread.sleep(sleepBetweenRetryTime);
			} catch (InterruptedException e1) {
				// It's OK.
			}
			LOG.info("RPC Client try to reconnect to server round {} ...", i);
			try {
				client.connect();
				return true;
			} catch (IOException e) {
				// Not connected.
				LOG.info("Try to re-connect to server failed. {}", e.toString());
			}
		}
		return false;
	}

	/**
	 * Get Connection Status of this client.
	 * @return
	 */
	public Status getConnStatus() {
		return connStatus;
	}

	public int getRpcHandleTimeout() {
		return rpcHandleTimeout;
	}

	public void setRpcHandleTimeout(int rpcHandleTimeout) {
		this.rpcHandleTimeout = rpcHandleTimeout;
	}

	public void setSleepBetweenRetryTime(int sleepBetweenRetryTime) {
		this.sleepBetweenRetryTime = sleepBetweenRetryTime;
	}

	public void setConnectRetryTimes(int connectRetryTimes) {
		this.connectRetryTimes = connectRetryTimes;
	}

	public void setConnectTimeout(int timeout) {
		this.client.setConnectTimeout(timeout);
	}

	/**
	 * Set the Rpc Configs, this method will parse all the configurations and generate execute map.
	 * @param confs
	 */
	public void addInferface(Class<?> interfs) {
		Method[] arr = MethodUtil.getMethods(interfs);
		for (Method m : arr) {
			if (m.isAnnotationPresent(RpcMethod.class)) {
				RpcMethod rp = m.getAnnotation(RpcMethod.class);
				Short rei = executeMap.put(m, rp.value());
				if (rei != null) {
					LOG.warn("Duplicate configuration for code: {}", rp.value());
				}
			}
		} // End of arr
	}

	/**
	 * De-serialize returned byte array into objects.
	 * @param ret
	 * @param type
	 * @param exp 0 for returned objects, 1 for RpcException.
	 * @return
	 * @throws Exception
	 */
	protected Object prepareReturn(byte[] ret, Type type, int exp) throws Exception {
		if (exp == 1) {
			type = RpcException.class;
		} else if (type == null || type.toString().equalsIgnoreCase("void")) {
			return null;
		}
		return prepareReturn(ret, type);
	}

	/**
	 * Serialize arguments objects into byte array.
	 * @param data
	 * @param generic
	 * @return
	 * @throws Exception
	 */
	protected abstract byte[] serializeParams(Object[] args) throws Exception;

	/**
	 * De-serialize returned byte array into objects.
	 * @param ret
	 * @param type
	 * @return
	 * @throws Exception
	 */
	protected abstract Object prepareReturn(byte[] ret, Type type) throws Exception;

}
