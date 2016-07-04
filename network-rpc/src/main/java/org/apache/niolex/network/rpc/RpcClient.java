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
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.niolex.commons.util.SystemUtil;
import org.apache.niolex.network.Config;
import org.apache.niolex.network.ConnStatus;
import org.apache.niolex.network.IClient;
import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.rpc.anno.RpcMethod;
import org.apache.niolex.network.rpc.util.RpcUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The basic RpcClient, send and receive Rpc packets, generate client RPC stubs here too.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0, Date: 2012-6-1
 */
public class RpcClient implements IPacketHandler, InvocationHandler {
	private static final Logger LOG = LoggerFactory.getLogger(RpcClient.class);

	/**
	 * Save the relationship between Java method and it's RPC method code.
	 */
	private final Map<Method, Short> executeMap = new HashMap<Method, Short>();

	/**
	 * The serial ID generator.
	 */
	private final AtomicInteger auto = new AtomicInteger(-1);

	/**
	 * The time to sleep between retry.
	 */
	private int sleepBetweenRetryTime = Config.RPC_SLEEP_BT_RETRY;

	/**
	 * The number of times to retry to get connected.
	 */
	private int connectRetryTimes = Config.RPC_CONNECT_RETRY_TIMES;

	/**
	 * The low layer client to send and receive Rpc packets.
	 */
	private final IClient client;

	/**
	 * The RPC invoker to do the real method invoke.
	 */
	private final RemoteInvoker invoker;

	/**
	 * The data translator.
	 */
	private final IConverter converter;

	/**
	 * The status of this Client.
	 */
	private volatile ConnStatus connStatus;

	/**
	 * Create a RpcClient with the specified low level client as the backed communication tool and
	 * the invoker must match the specified low level client. The converter must match the converter
	 * at the server side.
	 * <br>
	 * The client will be managed internally, please use this class's {@link #connect()} to connect
	 * and leave the low level client for us to manage.
	 *
	 * @param client the backed communication tool, low level client
	 * @param invoker use this to send packets to server and wait for response
	 * @param converter use this to serialize data to bytes and vice-versa
	 */
	public RpcClient(IClient client, RemoteInvoker invoker, IConverter converter) {
		super();
		this.client = client;
		this.invoker = invoker;
		this.converter = converter;
		this.client.setPacketHandler(this);
		this.connStatus = ConnStatus.INNITIAL;
	}

	/**
	 * Connect the backed communication client, and set the internal status.
	 *
	 * @throws IOException if necessary
	 */
	public void connect() throws IOException {
	    if (connStatus != ConnStatus.CONNECTED) {
	        client.connect();
	        connStatus = ConnStatus.CONNECTED;
	    }
	}

	/**
	 * Stop this client, and stop the backed communication client.
	 */
	public void stop() {
	    if (connStatus != ConnStatus.CLOSED) {
	        closeClient();
	    }
	}

	/**
	 * Close the backed client, and invoke the handle close method on the invoker.
	 */
	protected void closeClient() {
	    connStatus = ConnStatus.CLOSED;
	    client.stop();
	    invoker.handleClose(client);
	}

	/**
	 * Get the Rpc Service Client Side Stub powered by this rpc client.
	 *
	 * @param <T> the interface type
	 * @param c the interface you want to have stub with
	 * @return the generated stub
	 */
	@SuppressWarnings("unchecked")
    public <T> T getService(Class<T> c) {
		addInferface(c);
		return (T) Proxy.newProxyInstance(RpcClient.class.getClassLoader(),
                new Class[] {c}, this);
	}

    /**
     * This method will parse all the configurations in the interface and generate the execute map.
     * <br> We will call this method in {@link #getService(Class)} automatically. If you are not using
     * that method, please call this method before use the interface yourself.
     *
     * @param interfs the interface
     */
    public void addInferface(Class<?> interfs) {
        Method[] arr = interfs.getDeclaredMethods();
        for (Method m : arr) {
            if (m.isAnnotationPresent(RpcMethod.class)) {
                RpcMethod rp = m.getAnnotation(RpcMethod.class);
                executeMap.put(m, rp.value());
            }
        }
    }
    
	/**
	 * Check the client status before doing remote call and after got response.
	 */
	private void checkStatus() {
		switch (connStatus) {
			case INNITIAL:
			    throw new RpcException("Client not connected.", RpcException.Type.NOT_CONNECTED, null);
			case CLOSED:
			    throw new RpcException("Client closed.", RpcException.Type.CONNECTION_CLOSED, null);
		}
	}

	/**
	 * This is the override of super method.
	 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		checkStatus();
		Short rei = executeMap.get(method);
		if (rei != null) {
			// 1. Prepare parameters
			byte[] arr;
			if (args == null || args.length == 0) {
				arr = new byte[0];
			} else {
				arr = converter.serializeParams(args);
			}
			// 2. Create PacketData
			PacketData reqData = new PacketData(rei, arr);
			// 3. Generate serial number
			serialPacket(reqData);

			// 4. Invoke, send packet to server and wait for result
			PacketData respData = invoker.invoke(reqData, client);

			// 5. Process result.
			if (respData == null) {
				checkStatus();
				throw new RpcException("Timeout for this remote procedure call.",
						RpcException.Type.TIMEOUT, null);
			} else {
				boolean isEx = isException(respData.getReserved() - reqData.getReserved());

				if (respData.getLength() == 0) {
				    return null;
				}
				Object ret = prepareReturn(respData.getData(), method.getGenericReturnType(), isEx);
				if (isEx) {
				    throw (RpcException) ret;
				}
				return ret;
			}
		} else {
		    throw new RpcException("The method you want to invoke is not a remote procedure call.",
					RpcException.Type.METHOD_NOT_FOUND, null);
		}
	}

	/**
	 * Generate serial number
	 * The serial number will be 1, 3, 5, ...
	 *
	 * @param rc the request packet
	 */
	private void serialPacket(PacketData rc) {
		short seri = (short) (auto.addAndGet(2));
		rc.setReserved((byte) seri);
		rc.setVersion((byte) (seri >> 8));
	}

	/**
	 * Check whether this code is an exception.
	 *
	 * @param exp the exception code
	 * @return true if it's exception
	 */
	private boolean isException(int exp) {
	    // 127 + 1 = -128
        // -128 - 127 = -255
	    return exp == 1 || exp == -255;
	}

	/**
	 * De-serialize returned byte array into objects.
	 *
	 * @param ret the returned byte array
	 * @param type the return type
	 * @param isEx is the returned type an exception?
	 * @return the object the object parsed from the byte array
	 * @throws Exception if necessary
	 */
	protected Object prepareReturn(byte[] ret, Type type, boolean isEx) throws Exception {
		if (isEx) {
			return RpcUtil.parseRpcException(ret);
		} else if (type == null || type == void.class) {
			return null;
		}
		return converter.prepareReturn(ret, type);
	}

	/**
	 * We delegate all read packets to invoker.
	 *
	 * Override super method
	 * @see org.apache.niolex.network.IPacketHandler#handlePacket(org.apache.niolex.network.PacketData, org.apache.niolex.network.IPacketWriter)
	 */
	@Override
	public void handlePacket(PacketData sc, IPacketWriter wt) {
		this.invoker.handlePacket(sc, wt);
	}

	/**
	 * We will retry to connect to server in this method.
	 *
	 * Override super method
	 * @see org.apache.niolex.network.IPacketHandler#handleClose(org.apache.niolex.network.IPacketWriter)
	 */
	@Override
	public void handleClose(IPacketWriter wt) {
		if (connStatus == ConnStatus.CLOSED) {
			return;
		}
		this.connStatus = ConnStatus.CONNECTING;
		if (!retryConnect()) {
			LOG.error("We can not re-connect to server after retry times, RpcClient with stop.");
			// Try to shutdown this Client, inform all the threads.
			closeClient();
		}
	}

	/**
	 * Try to re-connect to server.
	 *
	 * @return true if connected
	 */
	private boolean retryConnect() {
		for (int i = 0; i < connectRetryTimes; ++i) {
		    SystemUtil.sleep(sleepBetweenRetryTime);
			LOG.info("RPC Client try to reconnect to server round {} ...", i);
			try {
				client.connect();
				this.connStatus = ConnStatus.CONNECTED;
				return true;
			} catch (IOException e) {
				// Not connected.
				LOG.info("Try to re-connect to server failed. {}", e.toString());
			}
		}
		return false;
	}

	/**
	 * @return The string representation of the remote peer. i.e. The IP address.
	 */
	public String getRemoteName() {
	    return client.getRemoteName();
	}

	/**
	 * Set the socket connect timeout.
	 * This method must be called before {@link #connect()}
	 *
	 * @param timeout the connect timeout to set
	 */
	public void setConnectTimeout(int timeout) {
	    this.client.setConnectTimeout(timeout);
	}

	/**
	 * Set the server Internet address this client want to connect
	 * This method must be called before {@link #connect()}
	 *
	 * @param serverAddress the server address
	 */
	public void setServerAddress(InetSocketAddress serverAddress) {
	    client.setServerAddress(serverAddress);
	}
	
	/**
	 * Set the server Internet address this client want to connect
	 * This method must be called before {@link #connect()}
	 *
	 * @param serverAddress the server address
	 */
	public void setServerAddress(String serverAddress) {
	    client.setServerAddress(serverAddress);
	}

	/**
	 * Get Connection Status of this rpc client.
	 *
	 * @return current connection status
	 */
	public ConnStatus getConnStatus() {
		return connStatus;
	}

	/**
	 * Whether the connection is valid.
	 *
	 * @return true if this RpcClient is valid and ready to work.
	 */
	public boolean isValid() {
        return connStatus == ConnStatus.CONNECTED;
    }

	/**
	 * Get the time in milliseconds that client with sleep between retry to connect
	 * to server.
	 *
	 * @return the time in milliseconds
	 */
	public int getSleepBetweenRetryTime() {
	    return sleepBetweenRetryTime;
	}

    /**
	 * Set the time in milliseconds that client with sleep between retry to connect
	 * to server.
	 *
	 * @param sleepBetweenRetryTime the time to set
	 */
	public void setSleepBetweenRetryTime(int sleepBetweenRetryTime) {
		this.sleepBetweenRetryTime = sleepBetweenRetryTime;
	}

	/**
	 * Get the retry times
	 *
	 * @return the retry times
	 */
    public int getConnectRetryTimes() {
        return connectRetryTimes;
    }

    /**
	 * Set retry times.
	 *
	 * @param connectRetryTimes the connect retry times to set
	 */
	public void setConnectRetryTimes(int connectRetryTimes) {
		this.connectRetryTimes = connectRetryTimes;
	}

}
