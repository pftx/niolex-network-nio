/**
 * RpcClientFactory.java
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
import java.util.List;

import org.apache.niolex.address.client.Consumer;
import org.apache.niolex.address.rpc.RpcInterface;
import org.apache.niolex.address.rpc.cli.pool.SimplePool;
import org.apache.niolex.address.rpc.svr.RpcExpose;
import org.apache.niolex.address.util.VersionUtil;
import org.apache.niolex.commons.bean.MutableOne;
import org.apache.niolex.commons.codec.StringUtil;
import org.apache.niolex.commons.test.Check;
import org.apache.niolex.commons.util.SystemUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class Wrap the ZK client detail and build Rpc client stub for Application user.
 * <br>
 * There are two implementations here.
 * 1. The pool implementation: We use the pool handler as the internal structure.
 * 2. The stub implementation: We use the retry handler as the internal structure.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5, $Date: 2012-11-30$
 */
public class RpcClientFactory {
    private static final Logger LOG = LoggerFactory.getLogger(RpcClientFactory.class);

    private Consumer zkConsumer;

    // ------------------------------------------------
    // ZK parameters
    private String zkClusterAddress;
    private String zkEnvironment;
    private int zkSessionTimeout;
    private String zkUserName;
    private String zkPassword;
    // ------------------------------------------------

    /**
     * Construct a RpcClientFactory with all the ZK parameters from system properties
     */
    public RpcClientFactory() {
        super();
        // Init ZK parameters
        zkClusterAddress = System.getProperty("zk.cluster.address");
        zkEnvironment = SystemUtil.getSystemPropertyWithDefault("zk.root", "dev");
        zkSessionTimeout = SystemUtil.getSystemPropertyAsInt("zk.session.timeout", 6000);
        zkUserName = System.getProperty("zk.cli.username");
        zkPassword = System.getProperty("zk.cli.password");
    }

    /**
     * Construct a RpcClientFactory with the specified user name and password, all the
     * other ZK parameters are from system properties
     *
     * @param zkUserName the ZK user name
     * @param zkPassword the ZK password
     */
    public RpcClientFactory(String zkUserName, String zkPassword) {
        this();
        this.zkUserName = zkUserName;
        this.zkPassword = zkPassword;
    }

    /**
     * Connect to ZK, so we can get service addresses from it.
     *
     * @return true if connected, false otherwise.
     */
    public boolean connectToZK() {
        try {
            zkConsumer = new Consumer(zkClusterAddress, zkSessionTimeout);
            zkConsumer.setRoot(zkEnvironment);
            zkConsumer.addAuthInfo(zkUserName, zkPassword);
            return true;
        } catch (IOException e) {
            LOG.error("Error occured when try to connect to ZK.", e);
        }
        return false;
    }

    /**
     * Disconnect from ZK, so we can stop this client stub factory.
     */
    public void disconnectFromZK() {
        if (zkConsumer != null) {
            zkConsumer.close();
            zkConsumer = null;
        }
    }

    /**
     * Get the client pool for this service.
     *
     * @param interfaze the service interface.
     * @param serviceName the service name.
     * @param version the service version.
     * @param state the state of the service you want to have.
     * @param poolSize the client pool size.
     * @return the client pool.
     */
    public <T> BaseStub<T> getPool(Class<T> interfaze, String serviceName,
            String version, String state, int poolSize) {
        MutableOne<List<String>> mutableOne = zkConsumer.getAddressList(serviceName, version, state);
        BaseStub<T> pool = new SimplePool<T>(poolSize, interfaze, mutableOne);
        // The pool is ready for use.
        return pool;
    }

    /**
     * Get the client pool for this service.
     *
     * @param interfaze the service interface.
     * @param version the service version.
     * @param state the state of the service you want to have.
     * @param poolSize the client pool size.
     * @return the client pool.
     */
    public <T> BaseStub<T> getPool(Class<T> interfaze, String version, String state, int poolSize) {
        RpcInterface inter = interfaze.getAnnotation(RpcInterface.class);
        Check.notNull(inter, "There is no annotation [RpcInterface] on " + interfaze.getName());
        String serviceName = inter.serviceName();
        if (StringUtil.isBlank(serviceName)) {
            serviceName = interfaze.getCanonicalName();
        }
        return getPool(interfaze, serviceName, version, state, poolSize);
    }

    /**
     * Get the client pool for this service.
     *
     * @param interfaze the service interface.
     * @param state the state of the service you want to have.
     * @param poolSize the client pool size.
     * @return the client pool.
     */
    public <T> BaseStub<T> getPool(Class<T> interfaze, String state, int poolSize) {
        RpcInterface inter = interfaze.getAnnotation(RpcInterface.class);
        Check.notNull(inter, "There is no annotation [RpcInterface] on " + interfaze.getName());
        return getPool(interfaze, "" + VersionUtil.encodeVersion(inter.version()), state, poolSize);
    }

    /**
     * Get the client pool for this service.
     *
     * @param interfaze the service interface.
     * @param poolSize the client pool size.
     * @return the client pool.
     */
    public <T> BaseStub<T> getPool(Class<T> interfaze, int poolSize) {
        RpcInterface inter = interfaze.getAnnotation(RpcInterface.class);
        Check.notNull(inter, "There is no annotation [RpcInterface] on " + interfaze.getName());
        return getPool(interfaze, "" + VersionUtil.encodeVersion(inter.version()), RpcExpose.DFT_STATE, poolSize);
    }

    /**
     * Get the client pool for this service.
     *
     * @param interfaze the service interface.
     * @return the client pool.
     */
    public <T> BaseStub<T> getPool(Class<T> interfaze) {
        return getPool(interfaze, 0);
    }

    //-------------------------------------------------------------------------
    // GETTERS & SETTERS
    //-------------------------------------------------------------------------

    /**
     * @return the zkClusterAddress
     */
    public String getZkClusterAddress() {
        return zkClusterAddress;
    }

    /**
     * @param zkClusterAddress the zkClusterAddress to set
     */
    public void setZkClusterAddress(String zkClusterAddress) {
        this.zkClusterAddress = zkClusterAddress;
    }

    /**
     * @return the zkEnvironment
     */
    public String getZkEnvironment() {
        return zkEnvironment;
    }

    /**
     * @param zkEnvironment the zkEnvironment to set
     */
    public void setZkEnvironment(String zkEnvironment) {
        this.zkEnvironment = zkEnvironment;
    }

    /**
     * @return the zkSessionTimeout
     */
    public int getZkSessionTimeout() {
        return zkSessionTimeout;
    }

    /**
     * @param zkSessionTimeout the zkSessionTimeout to set
     */
    public void setZkSessionTimeout(int zkSessionTimeout) {
        this.zkSessionTimeout = zkSessionTimeout;
    }

    /**
     * @return the zkUserName
     */
    public String getZkUserName() {
        return zkUserName;
    }

    /**
     * @param zkUserName the zkUserName to set
     */
    public void setZkUserName(String zkUserName) {
        this.zkUserName = zkUserName;
    }

    /**
     * @return the zkPassword
     */
    public String getZkPassword() {
        return zkPassword;
    }

    /**
     * @param zkPassword the zkPassword to set
     */
    public void setZkPassword(String zkPassword) {
        this.zkPassword = zkPassword;
    }

}
