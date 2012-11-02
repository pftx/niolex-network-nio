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
package org.apache.niolex.rpc;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

import org.apache.niolex.commons.reflect.MethodUtil;
import org.apache.niolex.network.Config;
import org.apache.niolex.network.IClient;
import org.apache.niolex.network.Packet;
import org.apache.niolex.rpc.core.ClientProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The based RpcClient, send and receive Rpc packets.
 * Use getService to Get the Rpc Service Client Stub.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-1
 */
public class RpcClient implements InvocationHandler {
	private static final Logger LOG = LoggerFactory.getLogger(RpcClient.class);

	/**
	 * The PacketClient to send and receive Rpc packets.
	 */
	private IClient client;

	/**
	 * The client protocol to serial data.
	 */
	private ClientProtocol clientProtocol;

	/**
	 * Save the execution map.
	 */
	private Map<Method, Short> executeMap = new HashMap<Method, Short>();

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
	 * Create a RpcClient with this IClient as the backed communication tool.
	 * The Client will be managed internally, please use #RpcClient.connect() and #RpcClient.stop() to connect and stop.
	 *
	 * Constructor
	 * @param client
	 */
	public RpcClient(IClient client) {
		super();
		this.client = client;
		this.connStatus = Status.INNITIAL;
	}

	/**
	 * Create a RpcClient with this IClient as the backed communication tool,
	 * the clientProtocol to do the serialization.
	 *
	 * @param client
	 * @param clientProtocol
	 */
	public RpcClient(IClient client, ClientProtocol clientProtocol) {
		super();
		this.client = client;
		this.clientProtocol = clientProtocol;
	}

	/**
	 * Connect the backed communication Client, and set the internal status.
	 * @throws IOException
	 */
	public void connect() throws IOException {
		this.client.connect();
		this.connStatus = Status.CONNECTED;
	}

	/**
	 * Stop this client, and stop the backed communication Client.
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
	 * Check the client status before doing remote call.
	 */
	private void checkStatus() {
		RpcException rep = null;
		switch (connStatus) {
			case INNITIAL:
				rep = new RpcException("Client not connected.", RpcException.Type.NOT_CONNECTED, null);
				throw rep;
			case CLOSED:
				rep = new RpcException("Client closed.", RpcException.Type.CONNECTION_CLOSED, null);
				throw rep;
		}
	}

	/**
	 * This is the override of super method.
	 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		checkStatus();
		RpcException rep = null;
		Short rei = executeMap.get(method);
		if (rei != null) {
			// 1. Prepare parameters
			byte[] arr;
			if (args == null || args.length == 0) {
				arr = new byte[0];
			} else {
				arr = clientProtocol.serializeParams(args);
			}
			// 2. Create Packet
			Packet rc = new Packet(rei, arr);

			// 3. Invoke client and wait for result.
			Packet sc = null;
			try {
				sc = client.sendAndReceive(rc);
			} catch (Exception e) {
				if (e instanceof RpcException) {
					throw (RpcException)e;
				}
				if (e instanceof SocketTimeoutException) {
					rep = new RpcException("Timeout for this remote procedure call.", RpcException.Type.TIMEOUT, null);
					throw rep;
				} else if (e instanceof IOException) {
					handleConnectionLose();
					throw new RpcException("Failed to write packet to socket or read from socket.",
							RpcException.Type.CONNECTION_LOST, e);
				}
				throw new RpcException("Failed to write packet to socket or read from socket.",
						RpcException.Type.UNKNOWN, e);
			}

			// 4. Process result.
			if (sc == null) {
				checkStatus();
				return null;
			} else {
				boolean isException = sc.getSerial() < 0;
				Object ret = prepareReturn(sc.getData(), method.getGenericReturnType(), isException);
				if (isException) {
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
	 * Handle connection lose, Try to reconnect.
	 */
	public void handleConnectionLose() {
		if (this.connStatus == Status.CLOSED) {
			return;
		}
		// We will retry to connect in this method.
		this.connStatus = Status.INNITIAL;
		client.stop();
		if (!retryConnect()) {
			LOG.error("Exception occured when try to re-connect to server. Client will stop.");
			// Try to shutdown this Client, inform all the threads.
			this.connStatus = Status.CLOSED;
		}
	}

	/**
	 * Try to re-connect to server, iterate connectRetryTimes
	 * @return true if connected to server.
	 */
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
				this.connStatus = Status.CONNECTED;
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

	/**
	 * Set the sleep time between retry, default to 1 second.
	 * @param sleepBetweenRetryTime
	 */
	public void setSleepBetweenRetryTime(int sleepBetweenRetryTime) {
		this.sleepBetweenRetryTime = sleepBetweenRetryTime;
	}

	/**
	 * Set the number of times to retry we connection lost from server.
	 * @param connectRetryTimes
	 */
	public void setConnectRetryTimes(int connectRetryTimes) {
		this.connectRetryTimes = connectRetryTimes;
	}

	/**
	 * Set the socket connection timeout, please set before connect.
	 * @param timeout
	 */
	public void setConnectTimeout(int timeout) {
		this.client.setConnectTimeout(timeout);
	}

	/**
	 * Set the Rpc Configs, this method will parse all the configurations and generate execute map.
	 * @param interfs
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
	 * @param exp false for returned objects, true for RpcException.
	 * @return
	 * @throws Exception
	 */
	protected Object prepareReturn(byte[] ret, Type type, boolean isException) throws Exception {
		if (isException) {
			type = RpcException.class;
		} else if (type == null || type.toString().equalsIgnoreCase("void")) {
			return null;
		}
		if (ret == null || ret.length == 0) {
			return null;
		}
		return clientProtocol.prepareReturn(ret, type);
	}

	/**
	 * set the client protocol do manage bean serialization.
	 *
	 * @param clientProtocol
	 */
	public void setClientProtocol(ClientProtocol clientProtocol) {
		this.clientProtocol = clientProtocol;
	}

}
