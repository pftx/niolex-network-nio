/**
 * DemoServer.java
 *
 * Copyright 2011 Niolex, Inc.
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
package org.apache.niolex.network.demo;

import java.io.IOException;

import org.apache.niolex.network.example.EchoPacketHandler;
import org.apache.niolex.network.handler.DispatchPacketHandler;
import org.apache.niolex.network.handler.SessionPacketHandler;
import org.apache.niolex.network.handler.SummaryPacketHandler;
import org.apache.niolex.network.server.NioServer;


/**
 * DemoServer
 * @author Xie, Jiyun
 *
 */
public class DemoServer {

    private static NioServer s = new NioServer();

    /**
     * The Server Demo
     * @param args
     */
    public static void main(String[] args) throws IOException {
        s.setPort(8808);
        DispatchPacketHandler handler = new DispatchPacketHandler();
        handler.addHandler((short)2, new EchoPacketHandler());
        handler.addHandler((short)3, new SummaryPacketHandler());
        handler.addHandler((short)4, new SessionPacketHandler(new LastTalkFactory()));
        s.setPacketHandler(handler);
        s.start();
    }

    public static void stop() {
        s.stop();
    }
}
