/**
 * ServerInterceptor.java
 *
 * Copyright 2013 The original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.niolex.network.svr.interceptor;

import java.lang.reflect.Method;

import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.rpc.RpcPacketHandler;

/**
 * The server side intercepter interface.
 * <p>
 * Workflow interface that allows for customized handler execution chains.
 * Applications can register any number of existing or custom interceptors
 * for server side ({@link RpcPacketHandler} to add common preprocessing behavior.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5
 * @since 2013-1-21
 */
public interface ServerInterceptor {

    /**
     * Called before server is about to prepare parameters.
     *
     * @param host the destination object to invoke in this time
     * @param method the target method to invoke in this time
     * @param sc the packet data received from client
     * @param wt the packet writer for this client
     * @return <code>true</code> if the execution chain should proceed with the
     * next interceptor or the next task. Else, We assumes
     * that this interceptor has already dealt with the response itself.
     */
    public boolean beforePrepareParams(Object host, Method method, PacketData sc, IPacketWriter wt);

    /**
     * Called after we already prepared parameters and about to invoke the target method.
     *
     * @param host the destination object to invoke in this time
     * @param method the target method to invoke in this time
     * @param args the arguments we parsed from request data
     * @param wt the packet writer for this client
     * @return <code>true</code> if the execution chain should proceed with the
     * next interceptor or the next task. Else, We assumes
     * that this interceptor has already dealt with the response itself.
     */
    public boolean beforeInvoke(Object host, Method method, Object[] args, IPacketWriter wt);

    /**
     * Called after we already invoked the target method and about to prepare response.
     *
     * @param host the destination object to invoke in this time
     * @param method the target method to invoke in this time
     * @param args the arguments we parsed from request data
     * @param ret the result return from the target method
     * @param wt the packet writer for this client
     * @return <code>true</code> if the execution chain should proceed with the
     * next interceptor or the next task. Else, We assumes
     * that this interceptor has already dealt with the response itself.
     */
    public boolean afterInvoke(Object host, Method method, Object[] args, Object ret, IPacketWriter wt);

    /**
     * Called after we already generated the result and about to send it to client.
     *
     * @param rc the packet data about to send to client
     * @param wt the packet writer for this client
     * @param ret the result or exception about to return to the client
     * @param exception o if success, 1 if exception occurred
     * @return <code>true</code> if the execution chain should proceed with the
     * next interceptor or the next task. Else, We assumes
     * that this interceptor has already dealt with the response itself.
     */
    public boolean beforeSend(PacketData rc, IPacketWriter wt, Object ret, int exception);

}
