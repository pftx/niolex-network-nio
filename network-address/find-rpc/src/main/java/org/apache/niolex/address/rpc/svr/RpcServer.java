/**
 * RpcServer.java
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
package org.apache.niolex.address.rpc.svr;

import java.util.List;

import org.apache.niolex.address.rpc.AddressUtil;
import org.apache.niolex.address.rpc.ConverterCenter;
import org.apache.niolex.address.server.Producer;
import org.apache.niolex.commons.util.SystemUtil;
import org.apache.niolex.network.rpc.ConfigItem;
import org.apache.niolex.network.rpc.IConverter;
import org.apache.niolex.network.rpc.RpcPacketHandler;
import org.apache.niolex.network.server.MultiNioServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The RpcServer will proxy a MultiNioServer inside, and deal with server side
 * service object publish job. User can publish multiple services at the same time, but
 * they must use the same service type and the method code must be unique.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5, $Date: 2012-11-30$
 */
public class RpcServer {
    protected static final Logger LOG = LoggerFactory.getLogger(RpcServer.class);

    private MultiNioServer svr = new MultiNioServer();
    private RpcPacketHandler handler;
    private Producer zkProducer;
    private int handlerThreadsNumber;
    private List<RpcExpose> exposeList;

    // ------------------------------------------------
    // ZK parameters
    private String zkClusterAddress;
    private String zkEnvironment;
    private int zkSessionTimeout;
    private String zkUserName;
    private String zkPassword;
    // ------------------------------------------------

    /**
     * Init ZK parameters from system properties.
     */
    public RpcServer() {
        super();
        // Init ZK parameters
        zkClusterAddress = System.getProperty("zk.cluster.address");
        zkEnvironment = SystemUtil.getSystemPropertyWithDefault("zk.root", "dev");
        zkSessionTimeout = SystemUtil.getSystemPropertyAsInt("zk.session.timeout", 6000);
        zkUserName = System.getProperty("zk.svr.username");
        zkPassword = System.getProperty("zk.svr.password");
    }

    /**
     * @return true if success
     * @see org.apache.niolex.network.server.MultiNioServer#start()
     */
    public boolean start() {
        if (handlerThreadsNumber != 0) {
            handler = new RpcPacketHandler(handlerThreadsNumber);
        } else {
            handler = new RpcPacketHandler();
        }
        // Prepare all the objects need to publish.
        String serviceType = null;
        ConfigItem[] list = new ConfigItem[exposeList.size()];
        int i = 0;
        for (RpcExpose ee : exposeList) {
            if (!ee.build()) {
                // LOGGED at RpcExpose#build()
                return false;
            }
            if (serviceType == null) {
                serviceType = ee.getServiceType();
            }
            if (serviceType.equals(ee.getServiceType())) {
                list[i++] = new ConfigItem(ee.interfaze, ee.target);
            } else {
                // We are using only one converter for one RpcPacketHandler, so ...
                LOG.error("One RpcServer must use one service type, but there are two: {}, {}!!!!",
                        serviceType, ee.getServiceType());
                return false;
            }
        }
        handler.setRpcConfigs(list);
        // Set converter
        IConverter converter = ConverterCenter.getConverter(serviceType);
        if (converter != null) {
            handler.setConverter(converter);
        } else {
            LOG.error("The converter for service type [{}] not found.", serviceType);
            return false;
        }
        svr.setPacketHandler(handler);
        if (svr.start()) {
            // Publish everything to ZK
            try {
                zkProducer = new Producer(zkClusterAddress, zkSessionTimeout);
                zkProducer.setRoot(zkEnvironment);
                zkProducer.addAuthInfo(zkUserName, zkPassword);
                for (RpcExpose ee : exposeList) {
                    String address = AddressUtil.generateAddress(ee, getPort());
                    zkProducer.publishService(ee.serviceName, ee.version, ee.state, address,
                            null, true, true);
                }
                return true;
            } catch (Exception e) {
                LOG.error("Error occured when communicate with Find-core.", e);
            }
        }
        return false;
    }

    /**
     * Stop this RpcServer, including the internal connection to ZK.
     *
     * @see org.apache.niolex.network.server.MultiNioServer#stop()
     */
    public void stop() {
        if (zkProducer != null) {
            zkProducer.close();
            zkProducer = null;
        }
        svr.stop();
    }

    /**
     * @return current handler queue size
     * @see org.apache.niolex.network.rpc.RpcPacketHandler#getQueueSize()
     */
    public int getQueueSize() {
        return handler == null ? 0 : handler.getQueueSize();
    }

    /**
     * @return the handlerThreadsNumber
     */
    public int getHandlerThreadsNumber() {
        return handlerThreadsNumber;
    }

    /**
     * @param handlerThreadsNumber the handlerThreadsNumber to set
     */
    public void setHandlerThreadsNumber(int handlerThreadsNumber) {
        this.handlerThreadsNumber = handlerThreadsNumber;
    }

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

    /**
     * @return the exposeList
     */
    public List<RpcExpose> getExposeList() {
        return exposeList;
    }

    /**
     * @param exposeList the exposeList to set
     */
    public void setExposeList(List<RpcExpose> exposeList) {
        this.exposeList = exposeList;
    }

    /**
     * @return the server accept timeout
     * @see org.apache.niolex.network.server.NioServer#getAcceptTimeOut()
     */
    public int getAcceptTimeOut() {
        return svr.getAcceptTimeout();
    }

    /**
     * @param acceptTimeOut the server accept timeout
     * @see org.apache.niolex.network.server.NioServer#setAcceptTimeOut(int)
     */
    public void setAcceptTimeOut(int acceptTimeOut) {
        svr.setAcceptTimeout(acceptTimeOut);
    }

    /**
     * @return the server port
     * @see org.apache.niolex.network.server.NioServer#getPort()
     */
    public int getPort() {
        return svr.getPort();
    }

    /**
     * @param port the server listening port
     * @see org.apache.niolex.network.server.NioServer#setPort(int)
     */
    public void setPort(int port) {
        svr.setPort(port);
    }

    /**
     * @return the selector threads number
     * @see org.apache.niolex.network.server.MultiNioServer#getThreadsNumber()
     */
    public int getSelectorThreadsNumber() {
        return svr.getThreadsNumber();
    }

    /**
     * @param threadsNumber the server selector threads number
     * @see org.apache.niolex.network.server.MultiNioServer#setThreadsNumber(int)
     */
    public void setSelectorThreadsNumber(int threadsNumber) {
        svr.setThreadsNumber(threadsNumber);
    }

}
