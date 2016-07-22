/**
 * RpcClientPool.java
 *
 * Copyright 2016 the original author or authors.
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

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.niolex.commons.concurrent.ConcurrentUtil;
import org.apache.niolex.network.rpc.RpcClient;

/**
 * The pool stores all the rpc clients by their Internet socket address.
 * 
 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
 * @version 2.1.2
 * @since Jul 22, 2016
 */
public class RpcClientPool {
    
    /**
     * The global singleton instance.
     */
    private static final RpcClientPool INSTANCE = new RpcClientPool();
    
    /**
     * Get the global singleton instance.
     * 
     * @return the global singleton instance
     */
    public static final RpcClientPool getPool() {
        return INSTANCE;
    }
    
    /**
     * The internal concurrent hash map.
     */
    private final ConcurrentHashMap<InetSocketAddress, Set<RpcClient>> rpcClientPool
            = new ConcurrentHashMap<InetSocketAddress, Set<RpcClient>>();

    /**
     * Get all the clients stored in one set by the specified Internet socket address.
     * 
     * @param key the specified Internet socket address
     * @return the clients
     */
    public Set<RpcClient> getClients(InetSocketAddress key) {
        Set<RpcClient> set = rpcClientPool.get(key);
        if (set == null) {
            set = ConcurrentUtil.initMap(rpcClientPool, key, new HashSet<RpcClient>());
        }
        return set;
    }
    
    /**
     * Remove the key value pair from the internal map by the specified Internet socket address.
     * 
     * @param key the specified Internet socket address
     * @return the clients
     */
    public Set<RpcClient> removeClients(InetSocketAddress key) {
        return rpcClientPool.remove(key);
    }
    
}
