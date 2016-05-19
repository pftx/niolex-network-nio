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

import static org.apache.niolex.network.demo.DemoUtil.*;

import java.io.IOException;

import org.apache.niolex.network.example.EchoPacketHandler;
import org.apache.niolex.network.handler.DispatchPacketHandler;
import org.apache.niolex.network.handler.SessionPacketHandler;
import org.apache.niolex.network.handler.SummaryPacketHandler;
import org.apache.niolex.network.server.MultiNioServer;


/**
 * DemoServer, use MultiNioServer to start a simple server.
 * We do some different things for different codes:
 * 2 -> echo packet back.
 * 3 -> print the packet summary to log and send the summary back.
 * 4 -> create a session for each client and do last talk reply.
 *
 * @author Xie, Jiyun
 *
 */
public class DemoServer {

    private static MultiNioServer s = new MultiNioServer();

    /**
     * The Server Demo, use it in command line.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        DemoUtil.parseArgs(args);
        s.setPort(PORT);
        if (POOL_SIZE != 0)
            s.setThreadsNumber(POOL_SIZE);
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
