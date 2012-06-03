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
package org.apache.niolex.network.rpc.conf;

import java.util.Arrays;

import org.apache.niolex.network.rpc.Constants;


/**
 * The rpc config bean, config common rpc properties.
 * e.g. connectTimeout, readTimeout, retryTimes
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-5-27
 */
public class RpcConfigBean extends BaseConfigBean {

    public int errorBlockTime = Constants.CLIENT_ERROR_BLOCK_TIME;
    public int connectTimeout = Constants.CLIENT_CONNECT_TIMEOUT;
    public int readTimeout = Constants.CLIENT_READ_TIMEOUT;
    public int retryTimes = Constants.CLIENT_RETRY_TIMES;
    public int intervalBetweenRetry = Constants.CLIENT_INTERVAL_BT_RETRY;
    public String[] serverList;
    public String serviceUrl = "";

    public RpcConfigBean(String groupName) {
        super(groupName);
        hasHeader = true;
    }

    @Override
    public void setConfig(String key, String value) {
        if ("serverList".equals(key)) {
            serverList = value.split(" *, *");
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
        this.errorBlockTime = co.errorBlockTime;
        this.connectTimeout = co.connectTimeout;
        this.readTimeout = co.readTimeout;
        this.retryTimes = co.retryTimes;
        this.intervalBetweenRetry = co.intervalBetweenRetry;
        this.serverList = co.serverList;
        this.serviceUrl = co.serviceUrl;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("(").append(groupName).append(") [connectTimeout=").append(connectTimeout).append(
                ", errorBlockTime=").append(errorBlockTime).append(", intervalBetweenRetry=").append(
                intervalBetweenRetry).append(", readTimeout=").append(readTimeout).append(", retryTimes=").append(
                retryTimes).append(", serverList=").append(Arrays.toString(serverList)).append(", serviceUrl=").append(
                serviceUrl).append(", header=").append(header).append(", prop=").append(prop).append("]");
        return builder.toString();
    }

}
