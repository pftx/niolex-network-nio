/**
 * BaseStub.java
 *
 * Copyright 2014 the original author or authors.
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
package org.apache.niolex.address.rpc.cli;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.niolex.address.rpc.AddressUtil;
import org.apache.niolex.address.rpc.ConverterCenter;
import org.apache.niolex.address.rpc.cli.pool.MultiplexPoolHandler;
import org.apache.niolex.commons.bean.MutableOne;
import org.apache.niolex.commons.test.Check;
import org.apache.niolex.network.cli.Constants;
import org.apache.niolex.network.rpc.IConverter;
import org.apache.niolex.network.rpc.cli.RpcStub;
import org.apache.niolex.network.rpc.cli.SocketInvoker;
import org.apache.niolex.network.rpc.util.RpcUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The base client side stub, handle the events sent from ZK server, deal with RPC server
 * add/remove events and generate client stub.
 * <br>
 * We manage all the server addresses here. The {@link MutableOne} will invoke listeners in
 * synchronized block, so we do not need to consider thread-safety.
 * <br>
 * We share connections by {@link RpcStubPool}.
 *
 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2014-1-22
 */
public abstract class BaseStub<T> implements MutableOne.DataChangeListener<List<String>> {
    protected static final Logger LOG = LoggerFactory.getLogger(BaseStub.class);
    protected static final RpcStubPool POOL = RpcStubPool.getPool();

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
     * The set save all the ready server node information.
     */
    protected final Set<NodeInfo> readySet = new HashSet<NodeInfo>();

    protected final Class<T> interfaze;
    protected T stub;

    /**
     * The current stub status, true if it's ready to work.
     */
    protected boolean isWorking;

    /**
     * How many connections to create for each weight, default to 2.
     */
    protected double weightShare = 2;

    /**
     * Create a Base stub with this pool size and interface.
     *
     * @param interfaze the service interface.
     * @param mutableOne the server address list of this service.
     */
    public BaseStub(Class<T> interfaze, MutableOne<List<String>> mutableOne) {
        super();
        this.interfaze = interfaze;
        this.isWorking = false;
        // Fire the first change.
        this.onDataChange(null, mutableOne.data());
        mutableOne.addListener(this);
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
    public void onDataChange(List<String> oldList, List<String> newList) {
        if (newList == null) {
            return;
        }

        HashSet<NodeInfo> newSet = new HashSet<NodeInfo>();
        HashSet<NodeInfo> delSet = new HashSet<NodeInfo>();
        HashSet<NodeInfo> addSet = new HashSet<NodeInfo>();

        // Parse config information.
        for (String node : newList) {
            NodeInfo info = AddressUtil.parseAddress(node);
            if (info != null)
                newSet.add(info);
        }
        // Check deleted items.
        for (NodeInfo info : readySet) {
            if (!newSet.contains(info)) {
                delSet.add(info);
            }
        }
        // Check added items.
        for (NodeInfo info : newSet) {
            if (!readySet.contains(info)) {
                addSet.add(info);
            }
        }

        // Try to make the change by now.
        if (isWorking) {
            fireChanges(delSet, addSet);
        } else {
            readySet.removeAll(delSet);
            readySet.addAll(addSet);
        }
    }

    /**
     * Build all the clients for this server address.
     *
     * @param info the node info which contains server address
     * @return the built clients
     */
    protected Set<RpcStub> buildClients(NodeInfo info) {
        // omitting decimal fractions smaller than 0.5 and counting all others, including 0.5, as 1
        final int curMax = (int) (info.getWeight() * weightShare + 0.5);
        // Set converter
        final IConverter converter = ConverterCenter.getConverter(info.getProtocol());
        if (converter == null) {
            LOG.error("The converter for service address [{}] type [{}] not found.", info.getAddress(), info.getProtocol());
            return null;
        }
        Set<RpcStub> clientSet = POOL.getClients(info.getAddress());
        int i = 0;
        // Make all the old connections ready.
        for (RpcStub rpcc : clientSet) {
            RpcUtil.setConnectRetryTimes(rpcc, connectRetryTimes);
            rpcc.addInferface(interfaze);
            if (!rpcc.isReady()) {
                try {
                    rpcc.connect();
                } catch (IOException e) {
                    LOG.error("Error occured when try to connect to {}.", info, e);
                }
            }
            ++i;
        }

        // Add more connections if necessary.
        for (; i < curMax; ++i) {
            try {
                // Create the invoker.
                SocketInvoker bi = new SocketInvoker(info.getAddress());
                bi.setConnectTimeout(connectTimeout);
                bi.setConnectRetryTimes(connectRetryTimes);
                bi.setSleepBetweenRetryTime(connectSleepBetweenRetry);
                bi.setRpcHandleTimeout(rpcTimeout);
                // Let's create the connection here.
                bi.connect();
                // Create Rpc stub.
                RpcStub cli = new RpcStub(bi, converter);
                cli.addInferface(interfaze);
                // Ready to add.
                clientSet.add(cli);
            } catch (IOException e) {
                LOG.error("Error occured when create client for {}.", info, e);
            }
        }

        return clientSet;
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

    // -------------------------------------------------------------------------
    // ABSTRACT METHODS
    // -------------------------------------------------------------------------

    /**
     * If the stub is working, then we will call this method to notify subclass the
     * changes of server lists.
     * 
     * @param delSet the node info list waiting to be deleted, subclass need to disconnect from these servers.
     * @param addSet the node info list waiting to be added, subclass need to connect to these new servers.
     */
    protected abstract void fireChanges(Set<NodeInfo> delSet, Set<NodeInfo> addSet);

    /**
     * Build this client stub for use. This method can only be called once.
     * 
     * @return this
     */
    public abstract BaseStub<T> build();

    /**
     * Destroy this pool.
     */
    public abstract void destroy();

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
        Check.lt(0, connectRetryTimes, "The connectRetryTimes must greater than 0.");
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
        Check.lt(1000, rpcTimeout, "The rpcTimeout must greater than 1000.");
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
        Check.lt(0, rpcErrorRetryTimes, "The rpcErrorRetryTimes must greater than 0.");
        this.rpcErrorRetryTimes = rpcErrorRetryTimes;
    }

}
