/**
 * Constants.java
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
package org.apache.niolex.network.rpc;

import org.apache.niolex.commons.config.PropUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 *
 * @version 1.0.0, $Date: 2011-7-11$
 *
 */
public abstract class Constants {
    private static final Logger LOG = LoggerFactory.getLogger(Constants.class);

    static {
        try {
            PropUtil.loadConfig("/network-rpc.properties", Constants.class);
        } catch (Throwable t) {
            LOG.info("network-rpc.properties not found, use default configurations instead.");
        }
    }

    public static final int CLIENT_ERROR_BLOCK_TIME = PropUtil.getInteger("rpcClientErrorBlockTime", 60000);
    public static final int CLIENT_CONNECT_TIMEOUT = PropUtil.getInteger("rpcClientConnectTimeout", 3000);
    public static final int CLIENT_READ_TIMEOUT = PropUtil.getInteger("rpcClientReadTimeout", 3000);
    public static final int CLIENT_RETRY_TIMES = PropUtil.getInteger("rpcClientRetryTimes", 2);
    public static final int CLIENT_INTERVAL_BT_RETRY = PropUtil.getInteger("rpcClientIntervalBetweenRetry", 50);

    public static final String CLIENT_ENCODING = PropUtil.getProperty("rpcClientEncoding", "utf-8");
}
