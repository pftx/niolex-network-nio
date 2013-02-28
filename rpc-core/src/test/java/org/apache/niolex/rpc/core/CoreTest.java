/**
 * CoreTest.java
 *
 * Copyright 2012 Niolex, Inc.
 *
 * Niolex licenses this file to you under the Apache License, version 2.0
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
package org.apache.niolex.rpc.core;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.niolex.rpc.client.SocketClient;
import org.apache.niolex.rpc.demo.JsonRpcServer;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5, $Date: 2012-12-4$
 */
public class CoreTest {

    public static final int PORT = 9909;
    public static boolean isOk;
    public static final InetSocketAddress SERVER_ADDRESS = new InetSocketAddress("localhost", PORT);
    public static final String SERVER_ADDRESS_STR = "localhost:" + PORT;

    static {
        try {
            if (!tryConnect()) {
                JsonRpcServer.main(null);
            }
            isOk = true;
        } catch (IOException e) {
            isOk = false;
            e.printStackTrace();
        }
    }

    public static final void startSvc() {
        // Do nothing here.
    }

    /**
     * @return
     */
    private static boolean tryConnect() {
        SocketClient c = new SocketClient(CoreTest.SERVER_ADDRESS);
        try {
            c.connect();
            c.stop();
            return true;
        } catch (Exception e) { }
        return false;
    }

    public static final void stop() {
        JsonRpcServer.stop();
    }

    @Test
    public void test() {
        System.out.println("The global server status - " + isOk);
    }

}
