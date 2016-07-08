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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.niolex.network.cli.PoolHandler;
import org.apache.niolex.network.cli.RpcClientAdapter;
import org.apache.niolex.network.rpc.RpcClient;

/**
 * The basic PoolHandler handles SocketClient, which can only be used in a single
 * thread. We want to extend the capability of that.
 *
 * In order to manage clients that can be used in multiple threads, we add clients
 * multiple times into the ready queue.
 *
 * We also remove those clients marked as deleted from the ready queue when appropriate.
 *
 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2014-1-21
 */
public class MultiplexPoolHandler extends PoolHandler<RpcClientAdapter> {

    /**
     * Translate the list of rpc clients into list of rpc client adapters.
     *
     * @param col the list of rpc clients
     * @param url the corresponding URL
     * @return the translated rpc client adapters list
     */
    public static final List<RpcClientAdapter> translate(Collection<RpcClient> col, String url) {
        List<RpcClientAdapter> list = new ArrayList<RpcClientAdapter>();
        for (RpcClient cli : col) {
            list.add(new RpcClientAdapter(url, cli));
        }
        return list;
    }

    /**
     * Save all the handlers here for multiplex.
     */
    private final List<RpcClientAdapter> backupHandlers;

    /**
     * The max threads we can have to visit one client concurrently
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
    public MultiplexPoolHandler(Collection<RpcClientAdapter> col, int retryTimes, int maxMultiplex) {
        super(col, retryTimes);
        this.backupHandlers = new ArrayList<RpcClientAdapter>(col);
        this.maxMultiplex = maxMultiplex;
    }

    /**
     * This is the override of super method.
     * @see org.apache.niolex.network.cli.PoolHandler#take()
     */
    @Override
    public RpcClientAdapter take() {
        RpcClientAdapter core = super.take();
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
            RpcClientAdapter h = backupHandlers.get(i);
            if (isClosed(h.getHandler())) {
                backupHandlers.remove(i);
                --i;
                continue;
            }
            super.offer(new RpcClientAdapter(h.getServiceUrl(), h.getHandler()));
        }
    }

    /**
     * Override super method
     * @see org.apache.niolex.network.cli.PoolHandler#repair(org.apache.niolex.network.cli.IServiceHandler)
     */
    @Override
    protected void repair(RpcClientAdapter core) {
        if (!isClosed(core.getHandler()))
            super.repair(core);
    }

    /**
     * Override super method
     * @see org.apache.niolex.network.cli.PoolHandler#offer(org.apache.niolex.network.cli.IServiceHandler)
     */
    @Override
    public void offer(RpcClientAdapter core) {
        if (!isClosed(core.getHandler()))
            super.offer(core);
    }

    /**
     * Stop those clients been marked by ConnectRetryTimes = 0
     *
     * @param core the client to check
     * @return true if closed, false if it's valid to reuse.
     */
    protected boolean isClosed(RpcClient core) {
        if (core.getConnectRetryTimes() != 0)
            return false;
        else {
            // Stop bad connection.
            core.stop();
            return true;
        }
    }

    /**
     * Add these new rpc client handlers into the ready queue.
     *
     * @param cliList the new rpc client adapters list
     */
    public synchronized void addNew(List<RpcClientAdapter> cliList) {
        // First, add all of them into the backup list.
        backupHandlers.addAll(cliList);
        // Then, add them into the working queue.
        for (RpcClientAdapter h : cliList) {
            super.offer(h);
            for (int i = 1; i < currentMultiplex; ++i) {
                super.offer(new RpcClientAdapter(h.getServiceUrl(), h.getHandler()));
            }
        }
    }

    /**
     * Destroy this pool, disconnect all the clients.
     */
    public synchronized void destroy() {
        // Disable #addMultiplex
        currentMultiplex = maxMultiplex;
        for (RpcClientAdapter h : backupHandlers) {
            RpcClient client = h.getHandler();
            client.setConnectRetryTimes(0);
            client.stop();
        }
    }

}
