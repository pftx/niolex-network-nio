/**
 * RpcConfigBean.java
 *
 * Copyright 2011 Niolex, Inc.
 *
 * Niolex licenses this file to you under the Apache License, version 2.0
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
package org.apache.niolex.network.cli.conf;

import java.util.Arrays;

import org.apache.niolex.network.cli.Constants;



/**
 * The rpc config bean, config common rpc properties.
 * e.g. connectTimeout, sleepBetweenRetry, retryTimes
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-5-27
 */
public class RpcConfigBean extends BaseConfigBean {

	/**
	 * The network connection parameters.
	 */
    public int connectTimeout = Constants.CLIENT_CONNECT_TIMEOUT;
    public int connectRetryTimes = Constants.CLIENT_CONNECT_RETRY_TIMES;
    public int connectSleepBetweenRetry = Constants.CLIENT_CONNECT_SLEEP_TIME;

    /**
     * The Rpc parameters.
     */
    public int rpcTimeout = Constants.CLIENT_RPC_TIMEOUT;
    public int rpcErrorBlockTime = Constants.CLIENT_RPC_ERROR_BLOCK_TIME;
    public int rpcErrorRetryTimes = Constants.CLIENT_RPC_RETRY_TIMES;
    public int rpcSleepBetweenRetry = Constants.CLIENT_RPC_INTERVAL_BT_RETRY;

    /**
     * Server address parameters.
     */
    public String[] serverList;
    public String serviceUrl = "";
    public String serviceType = "network/json";

    public RpcConfigBean(String groupName) {
        super(groupName);
        hasHeader = true;
    }

    @Override
    public void setConfig(String key, String value) {
        if ("serverList".equals(key)) {
            serverList = value.split(" *[,;] *");
        } else {
            super.setConfig(key, value);
        }
    }

    @Override
    public void setSuper(final BaseConfigBean superConf) {
        if (superConf instanceof RpcConfigBean) {
            final RpcConfigBean co = (RpcConfigBean) superConf;
            copyFrom(co);
        }
        super.setSuper(superConf);
    }

    public void copyFrom(final RpcConfigBean co) {
    	// connect
    	this.connectTimeout = co.connectTimeout;
    	this.connectRetryTimes = co.connectRetryTimes;
    	this.connectSleepBetweenRetry = co.connectSleepBetweenRetry;
    	// rpc
        this.rpcTimeout = co.rpcTimeout;
        this.rpcErrorBlockTime = co.rpcErrorBlockTime;
        this.rpcErrorRetryTimes = co.rpcErrorRetryTimes;
        this.rpcSleepBetweenRetry = co.rpcSleepBetweenRetry;
        // address
        this.serverList = co.serverList;
        this.serviceUrl = co.serviceUrl;
        this.serviceType = co.serviceType;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("(").append(groupName).append(") [connect: {").append(connectTimeout)
        .append(", ").append(connectRetryTimes).append(", ").append(connectSleepBetweenRetry)
        .append("}, rpc: {").append(rpcTimeout).append(", ").append(rpcErrorBlockTime)
        .append(", ").append(rpcErrorRetryTimes).append(", ").append(rpcSleepBetweenRetry)
        .append("}, address: {").append(Arrays.toString(serverList)).append(", ")
        .append(serviceUrl).append(", ").append(serviceType).append("}, header: ")
        .append(header).append(", prop: ").append(prop).append("]");
        return builder.toString();
    }

}
