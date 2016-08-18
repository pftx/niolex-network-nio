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

import static org.apache.niolex.network.demo.DemoUtil.POOL_SIZE;
import static org.apache.niolex.network.demo.DemoUtil.PORT;

import java.io.IOException;

import org.apache.niolex.network.handler.DispatchPacketHandler;
import org.apache.niolex.network.handler.SessionPacketHandler;
import org.apache.niolex.network.handler.SummaryPacketHandler;
import org.apache.niolex.network.server.MultiNioServer;


/**
 * DemoServer, use MultiNioServer to start a simple server.
 * We do some different things for different codes:
 * <pre>
 * 2 -&gt; echo packet back.
 * 3 -&gt; print the packet summary to log and send the summary back.
 * 4 -&gt; create a session for each client and do last talk reply.
 * </pre>
 * @author Xie, Jiyun
 *
 */
public class DemoServer {

    private static MultiNioServer server = new MultiNioServer();

    /**
     * The Server Demo, use it in command line.
     *
     * @param args the command line arguments
     * @throws IOException when necessary
     */
    public static void main(String[] args) throws IOException {
        DemoUtil.parseArgs(args);
        server.setPort(PORT);
        if (POOL_SIZE != 0)
            server.setThreadsNumber(POOL_SIZE);
        DispatchPacketHandler handler = new DispatchPacketHandler();
        handler.addHandler((short)2, new EchoPacketHandler());
        handler.addHandler((short)3, new SummaryPacketHandler());
        handler.addHandler(4, 100, new SessionPacketHandler(new LastTalkFactory()));
        handler.addHandler(101, 200, new SavePacketHandler(null));
        handler.addHandler(303, 404, new EchoPacketHandler());
        server.setPacketHandler(handler);
        server.start();
    }

    public static void stop() {
        server.stop();
    }

}
