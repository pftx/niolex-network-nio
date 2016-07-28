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
import org.apache.niolex.address.rpc.cli.pool.RetryStub;
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
 * There are two implementations here.<pre>
 * 1. The pool implementation: We use the pool handler as the internal structure.
 * 2. The stub implementation: We use the retry handler as the internal structure.
 * </pre>
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
     * Construct a RpcClientFactory with all the ZK parameters from system properties.
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
     * other ZK parameters are from system properties.
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
    
    //-------------------------------------------------------------------------
    // STUB BUILDER
    //-------------------------------------------------------------------------
    
    /**
     * Create a new stub builder to create client side stub.
     * 
     * @param interfaze the stub interface
     * @return the newly created builder
     */
    public <T> StubBuilder<T> newBuilder(Class<T> interfaze) {
        return new StubBuilder<T>(interfaze);
    }
    
    /**
     * The stub builder class.
     * 
     * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
     * @version 2.1.2
     * @since Jul 25, 2016
     * @param <T> the stub type to be built
     */
    public class StubBuilder<T> {
        
        /**
         * The service interface, client stub will proxy this interface.
         */
        private final Class<T> interfaze;
        
        /**
         * The service name, used to get service address list.
         */
        private String serviceName;
        
        /**
         * The service version.
         */
        private String version;
        
        /**
         * The state of the service you want to have.
         */
        private String state;
        
        /**
         * The client pool size. If only useful for poll stub.
         */
        private int poolSize;
        
        /**
         * The Constructor.
         * 
         * @param interfaze the service interface
         */
        public StubBuilder(Class<T> interfaze) {
            super();
            this.interfaze = interfaze;
        }
        
        /**
         * Set the service name.
         * 
         * @param serviceName the service name
         * @return this
         */
        public StubBuilder<T> serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }
        
        /**
         * Set the service version.
         * 
         * @param version the service version
         * @return this
         */
        public StubBuilder<T> version(String version) {
            if (version.indexOf('.') > 0) {
                this.version = "" + VersionUtil.encodeVersion(version);
            } else {
                this.version = version;
            }
            return this;
        }
        
        /**
         * Set the service state.
         * 
         * @param state the service state
         * @return this
         */
        public StubBuilder<T> state(String state) {
            this.state = state;
            return this;
        }
        
        /**
         * Set the client pool size.
         * 
         * @param poolSize the client pool size
         * @return this
         */
        public StubBuilder<T> poolSize(int poolSize) {
            this.poolSize = poolSize;
            return this;
        }
        
        /**
         * Check whether we are ready to build stub.
         */
        private void checkBuild() {
            RpcInterface inter = interfaze.getAnnotation(RpcInterface.class);
            Check.notNull(inter, "There is no annotation [RpcInterface] on " + interfaze.getName());
            
            // Make sure service name is ready and set.
            if (serviceName == null) {
                serviceName = inter.serviceName();
            }
            if (StringUtil.isBlank(serviceName)) {
                serviceName = interfaze.getCanonicalName();
            }
            
            // Make sure version is set.
            if (version == null) {
                version = "" + VersionUtil.encodeVersion(inter.version());
            }
            
            // Make sure state is set.
            if (state == null) {
                state = RpcExpose.DFT_STATE;
            }
        }
        
        /**
         * Build a client stub powered by connection pool.
         * 
         * @return the client stub
         */
        public BaseStub<T> buildPool() {
            checkBuild();
            return getPool(interfaze, serviceName, version, state, poolSize);
        }
        
        /**
         * Build a client stub powered by retry handler.
         * 
         * @return the client stub
         */
        public BaseStub<T> buildStub() {
            checkBuild();
            return getStub(interfaze, serviceName, version, state);
        }
        
    }
    
    //-------------------------------------------------------------------------
    // POOL IMPLEMENTATION
    //-------------------------------------------------------------------------
    
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
    public <T> BaseStub<T> getPool(Class<T> interfaze, String serviceName, String version, String state, int poolSize) {
        MutableOne<List<String>> mutableOne = zkConsumer.getAddressList(serviceName, version, state);
        BaseStub<T> pool = new SimplePool<T>(poolSize, interfaze, mutableOne);
        // The pool is ready for use.
        return pool;
    }
    
    //-------------------------------------------------------------------------
    // STUB IMPLEMENTATION
    //-------------------------------------------------------------------------
    
    /**
     * Get the client stub for this service.
     *
     * @param interfaze the service interface.
     * @param serviceName the service name.
     * @param version the service version.
     * @param state the state of the service you want to have.
     * @return the client stub.
     */
    public <T> BaseStub<T> getStub(Class<T> interfaze, String serviceName, String version, String state) {
        MutableOne<List<String>> mutableOne = zkConsumer.getAddressList(serviceName, version, state);
        RetryStub<T> stub = new RetryStub<T>(interfaze, mutableOne);
        return stub;
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
