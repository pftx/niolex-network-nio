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

import org.apache.niolex.address.rpc.svr.RpcExpose;
import org.apache.niolex.commons.net.NetUtil;

/**
 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2014-1-27
 */
public class AddressUtil {

    public static final String generateAddress(RpcExpose ee, int port) throws UnknownHostException {
        StringBuilder sb = new StringBuilder();
        sb.append(ee.getServiceType().replace('/', '^')).append(":").append(NetUtil.getLocalIP());
        sb.append(":").append(port).append(":").append(ee.getWeight()).append(":");
        return sb.toString();
    }

}
