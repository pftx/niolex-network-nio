/**
 * StuffRpcServer.java
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

import org.apache.niolex.rpc.RpcConfig;
import org.apache.niolex.rpc.protocol.StuffProtocol;
import org.apache.niolex.rpc.server.MultiNioServer;
import org.apache.niolex.rpc.server.RpcInvoker;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5, $Date: 2012-12-3$
 */
public class StuffRpcServer {

    private static MultiNioServer s = new MultiNioServer();

    /**
     * The Server Demo
     * @param args
     */
    public static void main(String[] args) throws IOException {
        s.setPort(8808);
        RpcInvoker handler = new RpcInvoker(new StuffProtocol());
        s.setInvoker(handler);
        if (args != null && args.length != 0) {
        	s.setSelectorsNumber(Integer.parseInt(args[0]));
        	s.setInvokersNumber(Integer.parseInt(args[1]));
        }

        RpcConfig[] confs = new RpcConfig[1];
        RpcConfig c = new RpcConfig();
        c.setInterface(RpcService.class);
        c.setTarget(new RpcServiceImpl());
        confs[0] = c;
		handler.setRpcConfigs(confs);

        s.start();
    }

    public static void stop() {
        s.stop();
    }

}
