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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.niolex.commons.bean.MutableOne;
import org.apache.niolex.commons.codec.StringUtil;
import org.apache.niolex.commons.net.NetUtil;
import org.apache.niolex.network.cli.Constants;
import org.apache.niolex.network.rpc.RpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The base client side stub, handle the events sent from ZK server, deal with RPC server
 * add/remove events and generate client stub.
 * <br>
 * We manage all the server addresses here.
 *
 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2014-1-22
 */
public abstract class BaseStub<T> implements MutableOne.DataChangeListener<List<String>> {
    private static final Logger LOG = LoggerFactory.getLogger(BaseStub.class);

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
     * The current stub status, true if it's ready to work
     */
    protected boolean isWorking;

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
        if (nodeList == null) {
            return;
        }
        HashSet<NodeInfo> infoSet = new HashSet<NodeInfo>();
        HashSet<NodeInfo> delSet = new HashSet<NodeInfo>();
        HashSet<NodeInfo> addSet = new HashSet<NodeInfo>();
        // Parse config information.
        for (String node : nodeList) {
            NodeInfo info = makeNodeInfo(node);
            if (info != null)
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
        // Try to make the change by now.
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
     * Make a new node info bean from the string representation.
     *
     * @param node the node string
     * @return the node info bean
     */
    public NodeInfo makeNodeInfo(String node) {
        try {
            NodeInfo info = new NodeInfo();
            // Address format:
            //          Protocol:IP:Port:Weight
            String[] pr = StringUtil.split(node, ":", true);
            if (pr.length < 4) {
                LOG.warn("Invalid server address format: {}.", node);
                return null;
            }
            info.setProtocol(pr[0].replace('^', '/'));
            info.setAddress(NetUtil.ipPort2InetSocketAddress(pr[1] + ":" + pr[2]));
            info.setWeight(Integer.parseInt(pr[3]));
            return info;
        } catch (Exception e) {
            LOG.warn("Invalid server address format: {}, msg: {}", node, e.toString());
            return null;
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

    /**
     * The bridge method help subclass to retrieve client set from node info.
     *
     * @param info the node info
     * @return the client set
     */
    protected Set<RpcClient> clientSet(NodeInfo info) {
        return info.clientSet;
    }

    //-------------------------------------------------------------------------
    // ABSTRACT METHODS
    //-------------------------------------------------------------------------

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
     * Build this client stub for use. This method can only be called once.
     */
    public abstract BaseStub<T> build();

    /**
     * Destroy this pool, disconnect all the connections.
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
