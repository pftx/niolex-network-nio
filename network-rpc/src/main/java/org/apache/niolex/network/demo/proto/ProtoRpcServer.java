/**
 * ProtoRpcServer.java
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
package org.apache.niolex.network.demo.proto;

import java.io.IOException;

import org.apache.niolex.network.rpc.ConfigItem;
import org.apache.niolex.network.rpc.RpcPacketHandler;
import org.apache.niolex.network.rpc.conv.ProtobufConverter;
import org.apache.niolex.network.server.MultiNioServer;
import org.apache.niolex.network.server.NioServer;

/**
 * Demo server.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0, Date: 2012-6-5
 */
public class ProtoRpcServer {

    private static NioServer s = new MultiNioServer();

    /**
     * The Server Demo
     * @param args
     */
    public static void main(String[] args) throws IOException {
        s.setPort(8808);
        RpcPacketHandler handler = new RpcPacketHandler();
        s.setPacketHandler(handler);
        handler.setConverter(new ProtobufConverter());
        ConfigItem[] confs = new ConfigItem[1];
        ConfigItem c = new ConfigItem();
        c.setInterface(PersonService.class);
        c.setTarget(new PersonServiceImpl());
        confs[0] = c;
		handler.setRpcConfigs(confs);
        s.start();
    }

    public static void stop() {
        s.stop();
    }
}
