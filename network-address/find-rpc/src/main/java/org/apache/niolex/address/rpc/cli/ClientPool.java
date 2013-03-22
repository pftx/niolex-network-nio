/**
 * ClientPool.java
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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.niolex.address.rpc.ConverterCenter;
import org.apache.niolex.commons.bean.MutableOne;
import org.apache.niolex.commons.bean.Pair;
import org.apache.niolex.network.ConnStatus;
import org.apache.niolex.network.cli.PoolHandler;
import org.apache.niolex.network.client.SocketClient;
import org.apache.niolex.network.rpc.IConverter;
import org.apache.niolex.network.rpc.RpcClient;
import org.apache.niolex.network.rpc.SingleInvoker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The Rpc Client Pool, Manage all the clients for one service under one state.
 * 
 * Notion!!! This class need to be refined latter.
 * 
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5, $Date: 2012-11-30$
 */
public class ClientPool<T> extends BasePool<T> {
    private static final Logger LOG = LoggerFactory.getLogger(ClientPool.class);
    
    protected final Set<NodeInfo> newlySet = new HashSet<NodeInfo>();
    protected final Set<NodeInfo> deleySet = new HashSet<NodeInfo>();
    
    /**
     * Create a ClientPool with this pool size and interface.
     * 
     * @param poolSize the client pool size.
     * @param interfaze the service interface.
     */
    public ClientPool(int poolSize, Class<T> interfaze, MutableOne<List<String>> mutableOne) {
        super(poolSize, interfaze, mutableOne);
    }
    
    /**
     * Mark the deleted node as not retry, and move it from ready set into delete set.
     * 
     * @param delSet
     */
    protected void markDeleted(HashSet<NodeInfo> delSet) {
        for (NodeInfo info : delSet) {
            Set<RpcClient> clientSet = info.clientSet;
            for (RpcClient sc : clientSet) {
                // We do not retry the deleted address.
                sc.setConnectRetryTimes(0);
            }
            deleySet.add(info);
            readySet.remove(info);
        }
    }
    
    /**
     * Take out the next rpc client from the deleted set.
     * 
     * @param delIter the iterator of deleySet
     * @param rpcIter the iterator of clientSet
     * @return the next rpc client, null if not found.
     */
    protected Pair<RpcClient, Iterator<RpcClient>> nextDelRpcClient(Iterator<NodeInfo> delIter,
            Iterator<RpcClient> rpcIter) {
        while (!rpcIter.hasNext()) {
            // The current node info is empty.
            delIter.remove();
            if (delIter.hasNext()) {
                rpcIter = delIter.next().clientSet.iterator();
                // This rpcIter maybe empty too, so we recursively call itself.
                return nextDelRpcClient(delIter, rpcIter);
            } else {
                return null;
            }
        }
        // Take the next rpc client out and delete it.
        RpcClient r = rpcIter.next();
        rpcIter.remove();
        // Return the pair.
        return new Pair<RpcClient, Iterator<RpcClient>>(r, rpcIter);
    }
    
    /**
     * Add new server to this pool, and move those connections connecting to deleted
     * server into this new server.
     * 
     * Notion! This may cause the client address different from the real connecting
     * address. But we will not consider it as a bug.
     * 
     * @param addSet
     */
    protected void markNew(HashSet<NodeInfo> addSet) {
        // Add the current new server.
        newlySet.addAll(addSet);
        Iterator<NodeInfo> newIter = newlySet.iterator();
        Iterator<NodeInfo> delIter = deleySet.iterator();
        
        NodeInfo xxxx = null;
        Iterator<RpcClient> rpcIter = null;
        if (delIter.hasNext()) {
            xxxx = delIter.next();
            rpcIter = xxxx.clientSet.iterator();
        }
        // Iterate the newly set.
        while (newIter.hasNext() && xxxx != null) {
            NodeInfo ninfo = newIter.next();
            final int curMax = (int) (ninfo.getWeight() * weightShare + 0.5);
            
            // Move all the deleted info into this newly added set.
            for (int i = ninfo.clientSet.size(); i < curMax; ++i) {
                Pair<RpcClient,Iterator<RpcClient>> pair = nextDelRpcClient(delIter, rpcIter);
                if (pair == null) {
                    // There is no deleted item.
                    xxxx = null;
                    break;
                }
                RpcClient rpc = pair.a;
                rpcIter = pair.b;
                // Make the deleted info ready again.
                rpc.setServerAddress(ninfo.getAddress());
                rpc.setConnectRetryTimes(connectRetryTimes);
                ninfo.clientSet.add(rpc);
                if (rpc.getConnStatus() == ConnStatus.CLOSED) {
                    try {
                        rpc.connect();
                    } catch (IOException e) {
                        LOG.error("Error occured when try to connect to new server.", e);
                    }
                }
            }
            // Add it into ready set.
            readySet.add(ninfo);
            if (xxxx != null) {
                // This address is done, remove it from new set.
                newIter.remove();
            }
        }
        
    }

    /**
     * Build this pool for use. This method can only be called once.
     */
    @SuppressWarnings("unchecked")
    public BasePool<T> build() {
        if (poolSize != 0) {
            double totalWeight = 0;
            for (NodeInfo info : readySet) {
                totalWeight += info.getWeight();
            }
            weightShare = totalWeight / poolSize;
        } else {
            weightShare = 2;
        }
        ArrayList<RpcClient> cliList = new ArrayList<RpcClient>();
        for (NodeInfo info : readySet) {
            final int curMax = (int) (info.getWeight() * weightShare + 0.5);
            // Set converter
            final IConverter converter = ConverterCenter.getConverter(info.getProtocol());
            if (converter == null) {
                LOG.error("The converter for service type [{}] not found.", info.getProtocol());
                continue;
            }
            Set<RpcClient> clientSet = info.clientSet;
            for (int i = 0; i < curMax; ++i) {
                try {
                    // Let's create the connection here.
                    SocketClient sk = new SocketClient(info.getAddress());
                    // Create the Rpc.
                    RpcClient cli = new RpcClient(sk, new SingleInvoker(), converter);
                    cli.setConnectTimeout(connectTimeout);
                    cli.setConnectRetryTimes(connectRetryTimes);
                    cli.setSleepBetweenRetryTime(connectSleepBetweenRetry);
                    cli.connect();
                    cli.addInferface(interfaze);
                    // Ready to add.
                    clientSet.add(cli);
                    cliList.add(cli);
                } catch (IOException e) {
                    LOG.error("Error occured when create client for {}.", info, e);
                }
            }
        }
        // Rpc client is ready, let's create the pool handler.
        Collections.shuffle(cliList);
        poolHandler = new PoolHandler<RpcClient>(rpcErrorRetryTimes, cliList);
        poolHandler.setWaitTimeout(rpcTimeout);
        // Pool creation done.
        stub = (T) Proxy.newProxyInstance(ClientPool.class.getClassLoader(),
                new Class[] {interfaze}, poolHandler);
        isWorking = true;
        return this;
    }
    
    /**
     * Destroy this pool.
     */
    public void destroy() {
        throw new UnsupportedOperationException("Pool can not be destroyed for now.");
    }
    
    /**
     * Get the Rpc Service Client Stub powered by this rpc client pool.
     *
     * @return the stub
     */
    public T getService() {
        return stub;
    }
    
}
