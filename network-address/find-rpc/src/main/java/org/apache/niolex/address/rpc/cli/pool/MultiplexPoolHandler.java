/**
 * MultiplexPoolHandler.java
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
package org.apache.niolex.address.rpc.cli.pool;

import static org.apache.niolex.network.rpc.util.RpcUtil.isInUse;
import static org.apache.niolex.network.rpc.util.RpcUtil.markAbandon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.niolex.network.cli.PoolHandler;
import org.apache.niolex.network.rpc.cli.RpcStub;

/**
 * The basic PoolHandler handles SocketClient, which can only be used in a single
 * thread. We want to extend the capability of that.
 *
 * In order to manage clients that can be used in multiple threads, we add clients
 * multiple times into the ready queue.
 *
 * We also remove those clients marked as abandoned from the ready queue when appropriate.
 *
 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2014-1-21
 */
public class MultiplexPoolHandler extends PoolHandler<RpcStub> {

    /**
     * Save all the handlers here for multiplex.
     */
    private final List<RpcStub> backupHandlers;

    /**
     * The max threads we can have to visit one client concurrently.
     */
    private final int maxMultiplex;
    private int currentMultiplex = 1;

    /**
     * Construct the pool handler, save all the handlers in the backup list, add them into
     * the ready queue when necessary.
     *
     * @param col the collection of handlers
     * @param retryTimes the max retry times
     * @param maxMultiplex the max threads we can have to visit one client concurrently
     */
    public MultiplexPoolHandler(Collection<RpcStub> col, int retryTimes, int maxMultiplex) {
        super(col, retryTimes);
        this.backupHandlers = new ArrayList<RpcStub>(col);
        this.maxMultiplex = maxMultiplex;
    }

    /**
     * This is the override of super method.
     * @see org.apache.niolex.network.cli.PoolHandler#take()
     */
    @Override
    public RpcStub take() {
        RpcStub core = super.take();
        if (core == null && currentMultiplex < maxMultiplex) {
            // We need more handlers.
            addMultiplex();
            core = super.take();
        }
        return core;
    }

    /**
     * Add another multiplex for all the handlers.
     */
    protected synchronized void addMultiplex() {
        if (currentMultiplex >= maxMultiplex) {
            return;
        }
        ++currentMultiplex;
        for (int i = 0; i < backupHandlers.size(); ++i) {
            RpcStub h = backupHandlers.get(i);
            if (!isInUse(h)) {
                backupHandlers.remove(i);
                --i;
                continue;
            }
            super.offer(h);
        }
    }

    /**
     * Override super method
     * @see org.apache.niolex.network.cli.PoolHandler#repair(org.apache.niolex.network.cli.IServiceHandler)
     */
    @Override
    protected void repair(RpcStub core) {
        if (isInUse(core))
            super.repair(core);
    }

    /**
     * Override super method
     * @see org.apache.niolex.network.cli.PoolHandler#offer(org.apache.niolex.network.cli.IServiceHandler)
     */
    @Override
    public void offer(RpcStub core) {
        if (isInUse(core))
            super.offer(core);
    }

    /**
     * Add these new rpc client handlers into the ready queue.
     *
     * @param cliList the new rpc client adapters list
     */
    public synchronized void addNew(List<RpcStub> cliList) {
        // First, add all of them into the backup list.
        backupHandlers.addAll(cliList);
        // Then, add them into the working queue.
        for (int i = 0; i < currentMultiplex; ++i) {
            for (RpcStub h : cliList) {
                super.offer(h);
            }
        }
    }

    /**
     * Destroy this pool, disconnect all the clients.
     */
    public synchronized void destroy() {
        // Disable #addMultiplex
        currentMultiplex = maxMultiplex;
        for (RpcStub h : backupHandlers) {
            markAbandon(h);
            h.stop();
        }
    }

}
