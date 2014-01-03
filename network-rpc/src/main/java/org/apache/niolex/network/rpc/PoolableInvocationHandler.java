/**
 * PoolableInvocationHandler.java
 *
 * Copyright 2012 The original author or authors.
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
package org.apache.niolex.network.rpc;

import java.lang.reflect.InvocationHandler;

import org.apache.niolex.network.ConnStatus;

/**
 * This interface is for {@link org.apache.niolex.network.cli.PoolHandler}. When user
 * want to use PoolHandler, He will need to implement this interface.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5
 * @since 2012-12-19
 */
public interface PoolableInvocationHandler extends InvocationHandler {

    /**
     * Get the Rpc Service Client Stub powered by this handler.
     *
     * @param c The interface you want to have stub
     * @return the stub
     */
    public <T> T getService(Class<T> c);

    /**
     * Add the interface this handler need to handle.
     *
     * @param interfs
     */
    public void addInferface(Class<?> interfs);

    /**
     * Get Connection Status of this handler.
     *
     * @return current status
     */
    public ConnStatus getConnStatus();

    /**
     * @return The string representation of the remote peer. i.e. The IP address.
     */
    public String getRemoteName();

    /**
     * Get backed connection status of this handler.
     *
     * @return true if this handler is valid and ready to work.
     */
    public boolean isValid();

}
