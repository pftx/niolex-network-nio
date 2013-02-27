/**
 * MultiAddressClient.java
 *
 * Copyright 2013 The original author or authors.
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
package org.apache.niolex.rpc.client;

import java.io.IOException;

import org.apache.niolex.network.Packet;
import org.apache.niolex.network.client.BaseClient;

/**
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5
 * @since 2013-2-27
 */
public class MultiAddressClient extends BaseClient {

    /**
     * Override super method
     * @see org.apache.niolex.network.IClient#connect()
     */
    @Override
    public void connect() throws IOException {
        // TODO Auto-generated method stub

    }

    /**
     * Override super method
     * @see org.apache.niolex.network.IClient#sendAndReceive(org.apache.niolex.network.Packet)
     */
    @Override
    public Packet sendAndReceive(Packet sc) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Override super method
     * @see org.apache.niolex.network.IClient#stop()
     */
    @Override
    public void stop() {
        // TODO Auto-generated method stub

    }

}
