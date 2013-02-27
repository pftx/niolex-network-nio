/**
 * RpcServer.java
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
package org.apache.niolex.rpc.demo;

import java.io.IOException;

import org.apache.niolex.rpc.core.CoreTest;
import org.apache.niolex.rpc.protocol.JsonProtocol;
import org.apache.niolex.rpc.server.MultiNioServer;
import org.apache.niolex.rpc.server.RpcInvoker;


/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-6-2
 */
public class JsonRpcServer {

    private static MultiNioServer multiSvr = new MultiNioServer();
    public static boolean isServerStarted;

    /**
     * The Server Demo
     * @param args
     */
    public static void main(String[] args) throws IOException {
        if (args != null && args.length != 0) {
            multiSvr.setSelectorsNumber(Integer.parseInt(args[0]));
            multiSvr.setInvokersNumber(Integer.parseInt(args[1]));
        }
        multiSvr.setPort(CoreTest.PORT);
        RpcInvoker invoker = new RpcInvoker(new JsonProtocol());
        multiSvr.setInvoker(invoker);
        invoker.exportObject(new RpcServiceImpl());

        multiSvr.start();
        isServerStarted = true;
    }

    public static void stop() {
        multiSvr.stop();
        isServerStarted = false;
    }
}
