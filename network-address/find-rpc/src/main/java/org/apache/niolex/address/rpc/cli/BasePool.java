/**
 * BasePool.java
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

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.niolex.commons.bean.MutableOne;
import org.apache.niolex.network.cli.Constants;
import org.apache.niolex.network.cli.PoolHandler;
import org.apache.niolex.network.rpc.RpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Base Client Pool, Manage the server addresses here.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5, $Date: 2013-03-30$
 */
public abstract class BasePool<T> implements MutableOne.DataChangeListener<List<String>> {
    private static final Logger LOG = LoggerFactory.getLogger(BasePool.class);

    /**
     * The network connection parameters.
     */
    protected int connectTimeout = Constants.CLIENT_CONNECT_TIMEOUT;
    protected int connectRetryTimes = Constants.CLIENT_CONNECT_RETRY_TIMES;
    protected int connectSleepBetweenRetry = Constants.CLIENT_CONNECT_SLEEP_TIME;

    /**
     * The Rpc parameters.
     */
    protected int rpcTimeout = Constants.CLIENT_RPC_TIMEOUT;
    protected int rpcErrorRetryTimes = Constants.CLIENT_RPC_RETRY_TIMES;

    /**
     * The internal pool size, could not be changed after creation.
     */
    protected final int poolSize;
    protected double weightShare;

    protected final Set<NodeInfo> readySet = new HashSet<NodeInfo>();

    protected final Class<T> interfaze;
    protected T stub;

    protected PoolHandler<RpcClient> poolHandler;

    protected boolean isWorking;

    /**
     * Create a Base Pool with this pool size and interface.
     *
     * @param poolSize the client pool size.
     * @param interfaze the service interface.
     * @param mutableOne the server address list of this service.
     */
    public BasePool(int poolSize, Class<T> interfaze, MutableOne<List<String>> mutableOne) {
        super();
        this.poolSize = poolSize;
        this.interfaze = interfaze;
        this.isWorking = false;
        mutableOne.addListener(this);
        // Fire the first change.
        this.onDataChange(null, mutableOne.data());
    }

    /**
     * This method scan for the change of server list, and try to maintain a stable
     * client pool. We will not build the client pool here.
     *
     * Please Fire Rpc client build manually.
     *
     * Override super method
     * @see org.apache.niolex.commons.bean.MutableOne.DataChangeListener#onDataChange(java.lang.Object, java.lang.Object)
     */
    @Override
    public void onDataChange(List<String> oldList, List<String> nodeList) {
        HashSet<NodeInfo> infoSet = new HashSet<NodeInfo>();
        HashSet<NodeInfo> delSet = new HashSet<NodeInfo>();
        HashSet<NodeInfo> addSet = new HashSet<NodeInfo>();
        // Parse config information.
        for (String node : nodeList) {
            NodeInfo info = new NodeInfo();
            String[] pr = node.split(":");
            if (pr.length < 4) {
                LOG.info("Invalid address format: {}.", node);
                continue;
            }
            info.setProtocol(pr[0]);
            info.setAddress(new InetSocketAddress(pr[1], Integer.parseInt(pr[2])));
            info.setWeight(Integer.parseInt(pr[3]));
            infoSet.add(info);
        }
        // Check deleted items.
        for (NodeInfo info : readySet) {
            if (!infoSet.contains(info)) {
                delSet.add(info);
            }
        }
        // Check added items.
        for (NodeInfo info : infoSet) {
            if (!readySet.contains(info)) {
                addSet.add(info);
            }
        }
        // make the change now.
        if (isWorking) {
            if (!delSet.isEmpty())
                markDeleted(delSet);
            if (!addSet.isEmpty())
                markNew(addSet);
        } else {
            readySet.removeAll(delSet);
            readySet.addAll(addSet);
        }
    }

    /**
     * Mark the deleted node as not retry, and move it from ready set into delete set.
     *
     * @param delSet the node info list waiting to be deleted
     */
    protected abstract void markDeleted(HashSet<NodeInfo> delSet);

    /**
     * Add new server addresses to this pool, subclass need to connect to these new servers.
     *
     * @param addSet the node info list waiting to be added
     */
    protected abstract void markNew(HashSet<NodeInfo> addSet);

    /**
     * Build this pool for use. This method can only be called once.
     */
    public abstract BasePool<T> build();

    /**
     * Destroy this pool, disconnect all the connections.
     */
    public abstract void destroy();

    /**
     * Get the Rpc Service Client Side Stub powered by this rpc client pool.
     *
     * @return the stub
     */
    public T getService() {
        if (!this.isWorking) {
            throw new IllegalStateException("Please build this pool first!");
        }
        return stub;
    }


    //-------------------------------------------------------------------------
    // GETTERS & SETTERS
    //-------------------------------------------------------------------------

    /**
     * @return the connectTimeout
     */
    public int getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * @param connectTimeout the connectTimeout to set
     */
    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    /**
     * @return the connectRetryTimes
     */
    public int getConnectRetryTimes() {
        return connectRetryTimes;
    }

    /**
     * @param connectRetryTimes the connectRetryTimes to set
     */
    public void setConnectRetryTimes(int connectRetryTimes) {
        this.connectRetryTimes = connectRetryTimes;
    }

    /**
     * @return the connectSleepBetweenRetry
     */
    public int getConnectSleepBetweenRetry() {
        return connectSleepBetweenRetry;
    }

    /**
     * @param connectSleepBetweenRetry the connectSleepBetweenRetry to set
     */
    public void setConnectSleepBetweenRetry(int connectSleepBetweenRetry) {
        this.connectSleepBetweenRetry = connectSleepBetweenRetry;
    }

    /**
     * @return the rpcTimeout
     */
    public int getRpcTimeout() {
        return rpcTimeout;
    }

    /**
     * @param rpcTimeout the rpcTimeout to set
     */
    public void setRpcTimeout(int rpcTimeout) {
        this.rpcTimeout = rpcTimeout;
    }

    /**
     * @return the rpcErrorRetryTimes
     */
    public int getRpcErrorRetryTimes() {
        return rpcErrorRetryTimes;
    }

    /**
     * @param rpcErrorRetryTimes the rpcErrorRetryTimes to set
     */
    public void setRpcErrorRetryTimes(int rpcErrorRetryTimes) {
        this.rpcErrorRetryTimes = rpcErrorRetryTimes;
    }

}
