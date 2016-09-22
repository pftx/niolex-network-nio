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
package org.apache.niolex.rpc.server;

import java.io.IOException;

/**
 * DemoServer
 * @author Xie, Jiyun
 *
 */
public class DemoServer {

    private static MultiNioServer s = new MultiNioServer();

    /**
     * The Server Demo
     * @param args
     */
    public static void main(String[] args) throws IOException {
        s.setPort(8808);
        if (args != null && args.length != 0) {
        	s.setSelectorsNumber(Integer.parseInt(args[0]));
        	s.setInvokersNumber(Integer.parseInt(args[1]));
        }
        s.setInvoker(new EchoInvoker());
        s.start();
    }

    public static void stop() {
        s.stop();
    }
}
