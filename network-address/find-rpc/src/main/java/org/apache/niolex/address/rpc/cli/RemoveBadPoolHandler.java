/**
 * RemoveBadPoolHandler.java
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

import java.util.Collection;

import org.apache.niolex.network.cli.PoolHandler;
import org.apache.niolex.network.rpc.RpcClient;

/**
 * Remove those clients marked as deleted from the client set.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5
 * @since 2013-3-12
 */
public class RemoveBadPoolHandler extends PoolHandler<RpcClient> {

    /**
     * Invoke super constructor.
     * 
     * @param retryTimes
     * @param col
     */
    public RemoveBadPoolHandler(int retryTimes, Collection<RpcClient> col) {
        super(retryTimes, col);
    }

    /**
     * Override super method
     * @see org.apache.niolex.network.cli.PoolHandler#repair(org.apache.niolex.network.rpc.RpcClient)
     */
    @Override
    protected void repair(RpcClient core) {
        if (!checkClose(core))
            super.repair(core);
    }

    /**
     * Override super method
     * @see org.apache.niolex.network.cli.PoolHandler#offer(org.apache.niolex.network.rpc.PoolableInvocationHandler)
     */
    @Override
    public void offer(RpcClient core) {
        if (!checkClose(core))
            super.offer(core);
    }
    
    /**
     * Stop those clients been marked by ConnectRetryTimes = 0
     * 
     * @param core the client to check
     * @return true if closed, false if it's valid to reuse.
     */
    protected boolean checkClose(RpcClient core) {
        if (core.getConnectRetryTimes() != 0)
            return false;
        else {
            // Stop bad connection.
            core.stop();
            return true;
        }
    }

}
