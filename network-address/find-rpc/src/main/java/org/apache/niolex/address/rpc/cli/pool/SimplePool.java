/**
 * SimplePool.java
 *
 * Copyright 2012 Niolex, Inc.
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
package org.apache.niolex.address.rpc.cli.pool;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.niolex.address.rpc.cli.BaseStub;
import org.apache.niolex.address.rpc.cli.NodeInfo;
import org.apache.niolex.commons.bean.MutableOne;
import org.apache.niolex.network.rpc.cli.RpcStub;
import org.apache.niolex.network.rpc.util.RpcUtil;

/**
 * The Rpc Simple Client Pool, Manage all the clients for one service under one state.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5, $Date: 2013-03-30$
 */
public class SimplePool<T> extends BaseStub<T> {

    /**
     * The internal pool size, could not be changed after creation.
     */
    protected final int poolSize;

    /**
     * The real pool handler.
     */
    protected MultiplexPoolHandler poolHandler;

    /**
     * Create a SimplePool with this pool size and interface.
     *
     * @param poolSize the client pool size.
     * @param interfaze the service interface.
     * @param mutableOne the server address list of this service.
     */
    public SimplePool(int poolSize, Class<T> interfaze, MutableOne<List<String>> mutableOne) {
        super(interfaze, mutableOne);
        this.poolSize = poolSize;
    }

    /**
     * This is the override of super method.
     * @see org.apache.niolex.address.rpc.cli.BaseStub#fireChanges(java.util.Set, java.util.Set)
     */
    @Override
    protected void fireChanges(Set<NodeInfo> delSet, Set<NodeInfo> addSet) {
        markDeleted(delSet);
        markNew(addSet);
    }

    /**
     * Mark the deleted node as not retry, and remove them from ready set.
     * The deleted connections will be removed by {@link MultiplexPoolHandler}
     *
     * @param delSet the nodes been deleted
     */
    protected void markDeleted(Set<NodeInfo> delSet) {
        // Remove them all from the ready set.
        readySet.removeAll(delSet);
        for (NodeInfo info : delSet) {
            Set<RpcStub> clientSet = POOL.getClients(info.getAddress());
            for (RpcStub stub : clientSet) {
                // We do not retry the deleted address.
                RpcUtil.markAbandon(stub);
            }
        }
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
        // Offer all the new clients.
        poolHandler.addNew(cliList);
    }

    /**
     * Build this pool for use. This method can be called multiple times.
     */
    @SuppressWarnings("unchecked")
    @Override
    public synchronized SimplePool<T> build() {
        // Check duplicate call.
        if (isWorking) {
            return this;
        }
        // Calculate pool weightShare.
        if (poolSize != 0) {
            double totalWeight = 0;
            for (NodeInfo info : readySet) {
                totalWeight += info.getWeight();
            }
            weightShare = poolSize / totalWeight;
        } else {
            weightShare = 2;
        }
        // Build clients.
        ArrayList<RpcStub> cliList = new ArrayList<RpcStub>();
        for (NodeInfo info : readySet) {
            cliList.addAll(buildClients(info));
        }
        // Rpc client is ready, let's create the pool handler.
        Collections.shuffle(cliList);
        poolHandler = new MultiplexPoolHandler(cliList, rpcErrorRetryTimes, 100);
        // Pool creation done.
        stub = (T) Proxy.newProxyInstance(SimplePool.class.getClassLoader(),
                new Class[] {interfaze}, poolHandler);
        isWorking = true;
        return this;
    }

    /**
     * Override super method
     * @see org.apache.niolex.find.rpc.cli.Basestub#destroy()
     */
    @Override
    public synchronized void destroy() {
        if (isWorking) {
            poolHandler.destroy();
            isWorking = false;
        }
    }

}
