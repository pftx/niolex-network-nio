/**
 * AddressUtil.java
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
package org.apache.niolex.address.rpc;

import java.net.UnknownHostException;

import org.apache.niolex.address.rpc.cli.NodeInfo;
import org.apache.niolex.address.rpc.svr.RpcExpose;
import org.apache.niolex.commons.codec.StringUtil;
import org.apache.niolex.commons.net.NetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage server address and string representation translation.
 *
 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2014-1-27
 */
public class AddressUtil {
    private static final Logger LOG = LoggerFactory.getLogger(AddressUtil.class);

    /**
     * Generate the string representation of server address.
     *
     * @param ee the RPC expose bean
     * @param port the server port
     * @return the string representation of server address
     * @throws UnknownHostException if local Internet address not found
     */
    public static final String generateAddress(RpcExpose ee, int port) throws UnknownHostException {
        StringBuilder sb = new StringBuilder();
        sb.append(ee.getServiceType().replace('/', '^')).append("#").append(NetUtil.getLocalIP());
        sb.append(":").append(port).append("#").append(ee.getWeight()).append("#");
        return sb.toString();
    }

    /**
     * Make a new node info bean from the string representation.
     *
     * @param node the node string
     * @return the node info bean
     */
    public static final NodeInfo parseAddress(String node) {
        try {
            NodeInfo info = new NodeInfo();
            // Address format:
            //          Protocol#IP:Port#Weight
            String[] pr = StringUtil.split(node, "#", true);
            if (pr.length < 3) {
                LOG.warn("Invalid server address format: {}.", node);
                return null;
            }
            info.setProtocol(pr[0].replace('^', '/'));
            info.setAddress(NetUtil.ipPort2InetSocketAddress(pr[1]));
            info.setWeight(Integer.parseInt(pr[2]));
            return info;
        } catch (Exception e) {
            LOG.warn("Invalid server address format: {}, msg: {}", node, e.toString());
            return null;
        }
    }

}
