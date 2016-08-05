/**
 * BlockingStub.java
 *
 * Copyright 2016 the original author or authors.
 *
 * We licenses this file to you under the Apache License, version 2.0
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
package org.apache.niolex.network.rpc.cli;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.cli.handler.IServiceHandler;
import org.apache.niolex.network.rpc.IConverter;
import org.apache.niolex.network.rpc.RpcException;
import org.apache.niolex.network.rpc.anno.RpcMethod;
import org.apache.niolex.network.rpc.internal.MethodExecuteItem;
import org.apache.niolex.network.rpc.util.RpcUtil;

/**
 * Manage connection to server, generate client blocking stub to do RPC.
 * 
 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
 * @version 0.6.1
 * @since Aug 3, 2016
 */
public class RpcStub implements IServiceHandler {

    /**
     * Save the relationship between Java method and it's RPC method code.
     */
    private final ConcurrentMap<Method, MethodExecuteItem> executeMap = new ConcurrentHashMap<Method, MethodExecuteItem>();

    /**
     * Store all the interfaces.
     */
    private final Set<Class<?>> interfaceSet = new HashSet<Class<?>>();

    /**
     * The serial ID generator.
     */
    private final AtomicInteger auto = new AtomicInteger(-1);

    /**
     * The RPC invoker to do the real method invoke.
     */
    private final RemoteInvoker invoker;

    /**
     * The data translator.
     */
    private final IConverter converter;

    /**
     * Create a Rpc Stub with the specified invoker as the communication tool and the specified converter
     * to translate objects. The specified converter must match the converter at the server side.
     *
     * @param invoker use this to send packets to server and wait for response
     * @param converter use this to serialize data to bytes and vice-versa
     */
    public RpcStub(RemoteInvoker invoker, IConverter converter) {
        super();
        this.invoker = invoker;
        this.converter = converter;
    }

    /**
     * Get the Rpc Service Client Side Stub powered by this blocking stub.
     *
     * @param <T> the interface type
     * @param c the interface you want to have stub with
     * @return the generated stub
     */
    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> c) {
        addInferface(c);
        return (T) Proxy.newProxyInstance(RpcStub.class.getClassLoader(), new Class[] { c }, this);
    }

    /**
     * This method will parse all the configurations in the interface and generate the execute map.
     * <br>
     * We will call this method in {@link #getService(Class)} automatically. If you are not using
     * that method, please call this method before use the interface yourself.
     *
     * @param interfs the interface to be added
     */
    public synchronized void addInferface(Class<?> interfs) {
        if (!interfaceSet.add(interfs)) {
            return;
        }

        Method[] arr = interfs.getDeclaredMethods();
        for (Method m : arr) {
            if (m.isAnnotationPresent(RpcMethod.class)) {
                RpcMethod rp = m.getAnnotation(RpcMethod.class);
                executeMap.put(m, new MethodExecuteItem(rp.value(), rp.oneWay()));
            }
        }
    }

    /**
     * This is the override of super method.
     * 
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        MethodExecuteItem rei = executeMap.get(method);
        if (rei != null) {
            // 1. Prepare parameters
            byte[] arr;
            if (args == null || args.length == 0) {
                arr = new byte[0];
            } else {
                arr = converter.serializeParams(args);
            }
            // 2. Create PacketData
            PacketData reqData = new PacketData(rei.getCode(), arr);
            // 3. Generate serial number
            serialPacket(reqData);

            // 4. Invoke, send packet to server and wait for result
            if (rei.isOneWay()) {
                invoker.sendPacket(reqData);
                return null;
            }

            PacketData respData = invoker.invoke(reqData);

            // 5. Process result.
            if (respData == null) {
                throw new RpcException("Timeout for this remote procedure call.", RpcException.Type.TIMEOUT, null);
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
            throw new RpcException("The method you want to invoke is not a remote procedure call.", RpcException.Type.METHOD_NOT_FOUND, null);
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
     * Delegate the connect request to the internal invoker.
     * 
     * @throws IOException if I / O related error occurred
     */
    public void connect() throws IOException {
        invoker.connect();
    }

    /**
     * Delegate the stop request to the internal invoker.
     */
    public void stop() {
        invoker.stop();
    }

    /**
     * This is the override of super method.
     * 
     * @see org.apache.niolex.network.cli.handler.IServiceHandler#getServiceUrl()
     */
    @Override
    public String getServiceUrl() {
        return invoker.getRemoteAddress();
    }

    /**
     * This is the override of super method.
     * 
     * @see org.apache.niolex.network.cli.handler.IServiceHandler#isReady()
     */
    @Override
    public boolean isReady() {
        return invoker.isReady();
    }

    /**
     * This is the override of super method. We will simply ignore this message.
     * 
     * @see org.apache.niolex.network.cli.handler.IServiceHandler#notReady(java.io.IOException)
     */
    @Override
    public void notReady(IOException ioe) {
        // Ignored.
    }

    /**
     * This is the override of super method.
     * 
     * @see org.apache.niolex.network.cli.handler.IServiceHandler#getHandler()
     */
    @Override
    public InvocationHandler getHandler() {
        return this;
    }

    /**
     * Get the internal invoker.
     * 
     * @return the internal invoker
     */
    public RemoteInvoker getInvoker() {
        return invoker;
    }

}
