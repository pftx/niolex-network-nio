/**
 * SimplePoolHandler.java
 * 
 * Copyright 2013 Niolex, Inc.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.niolex.network.cli.PoolHandler;
import org.apache.niolex.network.rpc.RpcClient;

/**
 * A simple implementation for pooling RpcClient. Every client can be
 * used in multiplex way.
 * 
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5
 * @since 2013-3-12
 */
public class SimplePoolHandler extends PoolHandler<RpcClient> {
    
    private final AtomicInteger idx = new AtomicInteger(-1);
    private ArrayList<RpcClient> cliList;
    private int listSize;

    /**
     * The simple constructor, disable super pool, implement pool locally.
     * 
     * @param retryTimes
     * @param col
     */
    @SuppressWarnings("unchecked")
    public SimplePoolHandler(int retryTimes, ArrayList<RpcClient> col) {
        super(retryTimes, Collections.EMPTY_LIST);
        this.cliList = col;
        this.listSize = col.size();
    }

    /**
     * Override super method
     * @see org.apache.niolex.network.cli.PoolHandler#take()
     */
    @Override
    public RpcClient take() {
        int index = idx.incrementAndGet();
        if (index > Integer.MAX_VALUE - 1024) {
            idx.set(-1);
            index -= listSize;
        }
        for (int i = 0; i < listSize; ++i) {
            RpcClient rpc = cliList.get((index + i) % listSize);
            if (rpc.isValid())
                return rpc;
        }
        return null;
    }

    /**
     * Override super method
     * @see org.apache.niolex.network.cli.PoolHandler#offer(org.apache.niolex.network.rpc.PoolableInvocationHandler)
     */
    @Override
    public void offer(RpcClient core) {
        // We do nothing here.
    }

    /**
     * Add all the items in this new list into the pool.
     * 
     * @param newList
     */
    public synchronized void addNew(ArrayList<RpcClient> newList) {
        ArrayList<RpcClient> list = new ArrayList<RpcClient>();
        list.addAll(newList);
        for(int i = 0; i < listSize; ++i) {
            RpcClient rpc = cliList.get(i);
            // Check all the connections need to be deleted, stop them.
            if (rpc.getConnectRetryTimes() != 0) {
                list.add(rpc);
            } else {
                rpc.stop();
            }
        }
        if (list.size() > listSize) {
            cliList = list;
            listSize = list.size();
        } else {
            listSize = list.size();
            cliList = list;
        }
    }

    /**
     * Destroy this pool.
     */
    public void destroy() {
        for(int i = 0; i < listSize; ++i) {
            RpcClient rpc = cliList.get(i);
            rpc.stop();
        }
        listSize = 0;
        cliList.clear();
    }
    
}
