/**
 * RpcProxy.java
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

import org.apache.niolex.commons.codec.StringUtil;
import org.apache.niolex.commons.reflect.MethodUtil;
import org.apache.niolex.network.Config;
import org.apache.niolex.network.IClient;
import org.apache.niolex.network.Packet;
import org.apache.niolex.rpc.protocol.IClientProtocol;

/**
 * The based RpcProxy, send and receive Rpc packets.
 * Use getService to Get the Rpc Service Client Stub.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-6-1
 */
public class RpcProxy implements InvocationHandler {

    /**
     * Save the execution map.
     */
    private Map<Method, Short> executeMap = new HashMap<Method, Short>();

	/**
	 * The PacketClient to send and receive Rpc packets.
	 */
	private IClient client;

	/**
	 * The client protocol to serial data.
	 */
	private IClientProtocol clientProtocol;

	/**
	 * Create a RpcProxy with this IClient as the backed communication tool.
	 * The Client will be managed internally, please use #RpcProxy.connect() and #RpcProxy.stop() to connect and stop.
	 *
	 * Constructor
	 * @param client
	 */
	public RpcProxy(IClient client) {
		super();
		this.client = client;
	}

	/**
	 * Create a RpcProxy with this IClient as the backed communication tool,
	 * the clientProtocol to do the serialization.
	 *
	 * @param client
	 * @param clientProtocol
	 */
	public RpcProxy(IClient client, IClientProtocol clientProtocol) {
		super();
		this.client = client;
		this.clientProtocol = clientProtocol;
	}

	/**
	 * Get the Rpc Service Client Stub.
	 * @param c The interface you want to invoke.
	 * @return
	 */
	@SuppressWarnings("unchecked")
    public <T> T getService(Class<T> c) {
		this.addInferface(c);
		return (T) Proxy.newProxyInstance(RpcProxy.class.getClassLoader(),
                new Class[] {c}, this);
	}

	/**
	 * Check the client status before doing remote call.
	 */
	private void checkStatus() {
		switch (client.getStatus()) {
			case INNITIAL:
			    throw new RpcException("Client not connected.", RpcException.Type.NOT_CONNECTED, null);
			case RETRYING:
			    throw new RpcException("Client is retrying to connect to server.", RpcException.Type.NOT_CONNECTED, null);
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
				arr = clientProtocol.serializeParams(args);
			}
			// 2. Create Packet
			Packet sendPk = new Packet(rei, arr);

			// 3. Invoke client and wait for result. Client will serialize the packet.
			Packet recvPk = null;
			try {
				recvPk = client.sendAndReceive(sendPk);
			} catch (RpcException e) {
			    throw e;
			} catch (SocketTimeoutException e) {
			    throw new RpcException("Timeout for this remote procedure call.", RpcException.Type.TIMEOUT, e);
			} catch (IOException e) {
			    throw new RpcException("Failed to write packet to socket or read from socket.",
			            RpcException.Type.CONNECTION_LOST, e);
			} catch (Exception e) {
				throw new RpcException("Failed due to unknown error.", RpcException.Type.UNKNOWN, e);
			}

			// 4. Process result.
			if (recvPk == null) {
				checkStatus();
				return null;
			} else {
			    if (recvPk.getCode() == Config.CODE_RPC_ERROR) {
			        throw new RpcException(StringUtil.utf8ByteToStr(recvPk.getData()),
			                RpcException.Type.ERROR_SERVER, null);
			    }
				boolean isException = recvPk.getSerial() < 0;
				Object ret = prepareReturn(recvPk.getData(), method.getGenericReturnType(), isException);
				if (isException) {
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
     * De-serialize returned byte array into objects.
     * @param ret
     * @param type
     * @param isException false for returned objects, true for RpcException.
     * @return the result
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
     * Connect the backed communication Client, and set the internal status.
     * @throws IOException
     */
    public void connect() throws IOException {
        this.client.connect();
    }

    /**
     * Stop this client, and stop the backed communication Client.
     */
    public void stop() {
        this.client.stop();
    }

	/**
	 * Set the socket connection timeout, please set before connect.
	 * @param timeout
	 */
	public void setConnectTimeout(int timeout) {
		this.client.setConnectTimeout(timeout);
	}

	/**
	 * Set the Rpc Configurations, this method will parse all the configurations and generate execute map.
	 * @param interfs
	 */
	public void addInferface(Class<?> interfs) {
		Method[] arr = MethodUtil.getMethods(interfs);
		for (Method m : arr) {
			if (m.isAnnotationPresent(RpcMethod.class)) {
				RpcMethod rp = m.getAnnotation(RpcMethod.class);
				executeMap.put(m, rp.value());
			}
		}
	}

	/**
	 * set the client protocol do manage bean serialization.
	 *
	 * @param clientProtocol
	 */
	public void setClientProtocol(IClientProtocol clientProtocol) {
		this.clientProtocol = clientProtocol;
	}

}
