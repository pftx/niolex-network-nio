/**
 * DemoClient.java
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

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Scanner;

import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.client.BlockingClient;


/**
 * DemoClient, use BlockingClient to connect to localhost, read data from console and send it
 * to server, print results to console.
 *
 * @author Xie, Jiyun
 */
public class DemoClient {

    private static InputStream in = System.in;

    /**
     * The Client Demo
     *
     * @param args
     */
    public static void main(String[] args) throws Exception {
        DemoUtil.parseArgs(args);
        BlockingClient c = new BlockingClient(new InetSocketAddress(HOST, PORT));
        // Set timeout to 10 minutes.
        c.setConnectTimeout(TIMEOUT);
        c.setPacketHandler(new PrintPacketHandler());
        c.connect();
        for (int i = 1; i < 5; ++i) {
            PacketData sc = new PacketData();
            sc.setCode((short) i);
            sc.setVersion((byte) 1);
            byte[] data = ("This is a hello world test. Round " + i).getBytes();
            sc.setLength(data.length);
            sc.setData(data);
            c.handleWrite(sc);
            Thread.sleep(100);
        }
        Scanner sc = new Scanner(in);
        while (true) {
            System.out.print("Please enter a Packet code(-1 for exit): ");
            short code = sc.nextShort();
            if (code == -1) {
                break;
            }
            PacketData pk = new PacketData();
            pk.setCode(code);
            pk.setVersion((byte) 1);
            sc.nextLine();
            System.out.print("Please enter the Packet MSG: ");
            String msg = sc.nextLine();
            byte[] data = msg.getBytes();
            pk.setLength(data.length);
            pk.setData(data);
            c.handleWrite(pk);
        }
        Thread.sleep(100);
        c.stop();
    }

    /**
     * @param in the in to set
     */
    public static void setIn(InputStream in) {
        DemoClient.in = in;
    }

}
