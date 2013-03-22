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
package org.apache.niolex.address.rpc.cli;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.niolex.address.rpc.ConverterCenter;
import org.apache.niolex.commons.bean.MutableOne;
import org.apache.niolex.network.client.BlockingClient;
import org.apache.niolex.network.rpc.IConverter;
import org.apache.niolex.network.rpc.PacketInvoker;
import org.apache.niolex.network.rpc.RpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The Rpc Simple Client Pool, Manage all the clients for one service under one state.
 * 
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5, $Date: 2013-03-30$
 */
public class SimplePool<T> extends BasePool<T> {
    private static final Logger LOG = LoggerFactory.getLogger(SimplePool.class);
    
    /**
     * Create a SimplePool with this pool size and interface.
     * 
     * @param poolSize the client pool size.
     * @param interfaze the service interface.
     * @param mutableOne the server address list of this service.
     */
    public SimplePool(int poolSize, Class<T> interfaze, MutableOne<List<String>> mutableOne) {
        super(poolSize, interfaze, mutableOne);
    }

    /**
     * Mark the deleted node as not retry, and remove them from ready set.
     * The deleted connections will be removed by {@link SimplePoolHandler}
     * 
     * @param delSet
     */
    @Override
    protected void markDeleted(HashSet<NodeInfo> delSet) {
        // Remove them all from the ready set.
        readySet.removeAll(delSet);
        for (NodeInfo info : delSet) {
            Set<RpcClient> clientSet = info.clientSet;
            for (RpcClient sc : clientSet) {
                // We do not retry the deleted address.
                sc.setConnectRetryTimes(0);
            }
        }
    }
    
    /**
     * Connect to new servers, add add them into ready set.
     * 
     * @param addSet
     */
    @Override
    protected void markNew(HashSet<NodeInfo> addSet) {
        // Put them into ready set.
        readySet.addAll(addSet);
        // Save all the new clients.
        ArrayList<RpcClient> cliList = new ArrayList<RpcClient>();
        // Add the current new server.
        for (NodeInfo info : addSet) {
            Set<RpcClient> clientSet = info.clientSet;
            buildClients(info);
            cliList.addAll(clientSet);
        }
        // Offer all the new clients.
        if (poolHandler instanceof SimplePoolHandler) {
            SimplePoolHandler simPool = (SimplePoolHandler)poolHandler;
            simPool.addNew(cliList);
        } else {
            LOG.error("New server can not be added, pool type: {}.", poolHandler.getClass());
        }
    }
    
    /**
     * Build this pool for use. This method can only be called once.
     */
    @SuppressWarnings("unchecked")
    @Override
    public synchronized BasePool<T> build() {
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
            weightShare = totalWeight / poolSize;
        } else {
            weightShare = 2;
        }
        // Build clients.
        ArrayList<RpcClient> cliList = new ArrayList<RpcClient>();
        for (NodeInfo info : readySet) {
            Set<RpcClient> clientSet = info.clientSet;
            buildClients(info);
            cliList.addAll(clientSet);
        }
        // Rpc client is ready, let's create the pool handler.
        Collections.shuffle(cliList);
        poolHandler = new SimplePoolHandler(rpcErrorRetryTimes, cliList);
        poolHandler.setWaitTimeout(rpcTimeout);
        // Pool creation done.
        stub = (T) Proxy.newProxyInstance(SimplePool.class.getClassLoader(),
                new Class[] {interfaze}, poolHandler);
        isWorking = true;
        return this;
    }
    
    /**
     * Build all the clients for this server address.
     * 
     * @param info the node info
     */
    protected void buildClients(NodeInfo info) {
        // omitting decimal fractions smaller than 0.5 and counting all others, including 0.5, as 1
        final int curMax = (int) (info.getWeight() * weightShare + 0.5);
        // Set converter
        final IConverter converter = ConverterCenter.getConverter(info.getProtocol());
        if (converter == null) {
            LOG.error("The converter for service address [{}] type [{}] not found.", info.getAddress(), info.getProtocol());
            return;
        }
        Set<RpcClient> clientSet = info.clientSet;
        for (int i = 0; i < curMax; ++i) {
            try {
                // Let's create the connection here.
                BlockingClient bk = new BlockingClient(info.getAddress());
                // Create the Rpc.
                PacketInvoker pk = new PacketInvoker();
                pk.setRpcHandleTimeout(rpcTimeout);
                RpcClient cli = new RpcClient(bk, pk, converter);
                cli.setConnectTimeout(connectTimeout);
                cli.setConnectRetryTimes(connectRetryTimes);
                cli.setSleepBetweenRetryTime(connectSleepBetweenRetry);
                cli.connect();
                cli.addInferface(interfaze);
                // Ready to add.
                clientSet.add(cli);
            } catch (IOException e) {
                LOG.error("Error occured when create client for {}.", info, e);
            }
        }
    }

    /**
     * Override super method
     * @see org.apache.niolex.find.rpc.cli.BasePool#destroy()
     */
    @Override
    public void destroy() {
        if (poolHandler instanceof SimplePoolHandler) {
            SimplePoolHandler simPool = (SimplePoolHandler)poolHandler;
            simPool.destroy();
        } else {
            LOG.error("Pool can not be destroyed, pool type: {}.", poolHandler.getClass());
        }
    }
    
}
