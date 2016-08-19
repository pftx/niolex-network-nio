/**
 * RetryStub.java
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
package org.apache.niolex.address.rpc.cli.pool;

import static org.apache.niolex.network.rpc.util.RpcUtil.isInUse;
import static org.apache.niolex.network.rpc.util.RpcUtil.markAbandon;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.niolex.address.rpc.cli.BaseStub;
import org.apache.niolex.address.rpc.cli.NodeInfo;
import org.apache.niolex.commons.bean.MutableOne;
import org.apache.niolex.network.cli.RetryHandler;
import org.apache.niolex.network.rpc.cli.RpcStub;

/**
 * We use the retry handler to generate client stub.
 * 
 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
 * @version 2.1.2
 * @since Jul 15, 2016
 */
public class RetryStub<T> extends BaseStub<T> {

    private final InvocationHandler proxyHander = new InvocationHandler() {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return handler.invoke(proxy, method, args);
        }
        
    };

    private volatile RetryHandler<RpcStub> handler;

    /**
     * Create a RetryStub with this pool size and interface.
     *
     * @param interfaze the service interface.
     * @param mutableOne the server address list of this service.
     */
    public RetryStub(Class<T> interfaze, MutableOne<List<String>> mutableOne) {
        super(interfaze, mutableOne);
    }

    /**
     * This is the override of super method.
     * @see org.apache.niolex.address.rpc.cli.BaseStub#build()
     */
    @SuppressWarnings("unchecked")
    @Override
    public synchronized RetryStub<T> build() {
        // Check duplicate call.
        if (isWorking) {
            return this;
        }

        // Build clients.
        ArrayList<RpcStub> cliList = new ArrayList<RpcStub>();
        for (NodeInfo info : readySet) {
            cliList.addAll(buildClients(info));
        }
        // Rpc client is ready, let's create the pool handler.
        handler = new RetryHandler<RpcStub>(cliList, rpcErrorRetryTimes, 15);

        // Pool creation done.
        stub = (T) Proxy.newProxyInstance(SimplePool.class.getClassLoader(), new Class[] { interfaze }, proxyHander);
        isWorking = true;
        return this;
    }

    /**
     * This is the override of super method.
     * @see org.apache.niolex.address.rpc.cli.BaseStub#destroy()
     */
    @Override
    public synchronized void destroy() {
        if (isWorking) {
            for (RpcStub h : handler.getHandlers()) {
                markAbandon(h);
                h.stop();
            }
            isWorking = false;
        }
    }

    /**
     * This is the override of super method.
     * 
     * @see org.apache.niolex.address.rpc.cli.BaseStub#fireChanges(java.util.Set, java.util.Set)
     */
    @Override
    protected void fireChanges(Set<NodeInfo> delSet, Set<NodeInfo> addSet) {
        markDeleted(delSet);
        markNew(addSet);
    }

    /**
     * Connect to new servers, add add them into ready set.
     *
     * @param addSet the nodes been added
     */
    protected void markNew(Set<NodeInfo> addSet) {
        // Put them into ready set.
        readySet.addAll(addSet);
        // Save all the new clients.
        ArrayList<RpcStub> cliList = new ArrayList<RpcStub>();
        // Add the current new server.
        for (NodeInfo info : addSet) {
            cliList.addAll(buildClients(info));
        }
        // Offer all the old clients.
        for (RpcStub rpcc : handler.getHandlers()) {
            if (isInUse(rpcc)) {
                cliList.add(rpcc);
            }
        }

        // Create new handler.
        handler = new RetryHandler<RpcStub>(cliList, rpcErrorRetryTimes, 15);
    }

}
