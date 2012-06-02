/**
 * RpcClient.java
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
package org.apache.niolex.network.demo.rpc;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.niolex.network.PacketClient;
import org.apache.niolex.network.rpc.RpcConfig;
import org.apache.niolex.network.rpc.json.JsonRpcClient;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-2
 */
public class RpcClient {

    /**
     * The Client Demo
     *
     * @param args
     */
    public static void main(String[] arg2s) throws Exception {
        PacketClient c = new PacketClient(new InetSocketAddress("localhost", 8808));
        JsonRpcClient client = new JsonRpcClient(c);
        RpcConfig[] confs = new RpcConfig[1];
        RpcConfig c2 = new RpcConfig();
        c2.setInterfs(RpcService.class);
        c2.setTarget(new RpcServiceImpl());
        confs[0] = c2;
        client.setRpcConfigs(confs);
        client.connect();

        RpcService ser = client.getService(RpcService.class);
        int k = ser.add(3, 4, 5, 6, 7, 8, 9);
        System.out.println("Out => " + k);

        List<String> args = new ArrayList<String>();
        args.add("3");
        k = ser.size(args);
        System.out.println("Out => " + k);
        c.stop();
    }

}
