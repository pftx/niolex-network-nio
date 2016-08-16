/**
 * RpcStubPool.java
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
import org.apache.niolex.network.rpc.cli.RpcStub;

/**
 * The pool stores all the rpc clients by their Internet socket address. We will not close connection actively,
 * but leave it open with no retry times. No matter the connection is still OK or not, we will not use it if it's
 * address is removed from zookeeper.
 * 
 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since Jul 22, 2016
 */
public class RpcStubPool {
    
    /**
     * The global singleton instance.
     */
    private static final RpcStubPool INSTANCE = new RpcStubPool();
    
    /**
     * Get the global singleton instance.
     * 
     * @return the global singleton instance
     */
    public static final RpcStubPool getPool() {
        return INSTANCE;
    }
    
    /**
     * The internal concurrent hash map.
     */
    private final ConcurrentHashMap<InetSocketAddress, Set<RpcStub>> rpcStubPool = new ConcurrentHashMap<InetSocketAddress, Set<RpcStub>>();

    /**
     * Get all the clients stored in one set by the specified Internet socket address.
     * 
     * @param key the specified Internet socket address
     * @return the clients
     */
    public Set<RpcStub> getClients(InetSocketAddress key) {
        Set<RpcStub> set = rpcStubPool.get(key);
        if (set == null) {
            set = ConcurrentUtil.initMap(rpcStubPool, key, new HashSet<RpcStub>());
        }
        return set;
    }
    
    /**
     * Remove the key value pair from the internal map by the specified Internet socket address.
     * 
     * @param key the specified Internet socket address
     * @return the clients or null if not found
     */
    public Set<RpcStub> removeClients(InetSocketAddress key) {
        return rpcStubPool.remove(key);
    }

}
