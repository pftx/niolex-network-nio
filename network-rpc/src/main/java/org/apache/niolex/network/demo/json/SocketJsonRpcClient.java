/**
 * SocketJsonRpcClient.java
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
package org.apache.niolex.network.demo.json;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.niolex.commons.test.Check;
import org.apache.niolex.network.rpc.cli.BaseInvoker;
import org.apache.niolex.network.rpc.cli.RpcStub;
import org.apache.niolex.network.rpc.cli.SingleInvoker;
import org.apache.niolex.network.rpc.conv.JsonConverter;

/**
 * Socket client test.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0, Date: 2012-6-13
 */
public class SocketJsonRpcClient {

    private static int err = 0;

    public static void main(String[] arg2s) throws IOException, Throwable {
        BaseInvoker invoker = new SingleInvoker(new InetSocketAddress("localhost", 8808));
        invoker.connect();
        RpcStub client = new RpcStub(invoker, new JsonConverter());

        final RpcService ser = client.getService(RpcService.class);

        int k = ser.add(3, 4, 5, 6, 7, 8, 9);
        System.out.println("Out 42 => " + k);
        assertt(42, k, "ser.add");
        List<String> args = new ArrayList<String>();
        args.add("3");
        args.add("3");
        args.add("3");
        k = ser.size(args);
        System.out.println("Out 3 => " + k);
        assertt(3, k, "ser.size");
        k = ser.size(null);
        System.out.println("Out -1 => " + k);
        assertt(-1, k, "ser.size");
        k = ser.add(3, 4, 5);
        System.out.println("Out 12 => " + k);
        assertt(12, k, "ser.add");

        final int SIZE = 2212;

        int i = SIZE;
        long in = System.currentTimeMillis();
        long maxin = 0;
        while (i-- > 0) {
            long xin = System.currentTimeMillis();
            k = ser.add(3, 4, 5, 6, 7, 8, 9, i);
            assertt(k, 42 + i, "Out 1 => " + k);

            args = new ArrayList<String>();
            args.add("3");
            args.add("3");
            k = ser.size(args);
            assertt(k, 2, "Out 2 => " + k);
            k = ser.size(null);
            assertt(k, -1, "Out 3 => " + k);
            long xou = System.currentTimeMillis() - xin;
            if (xou > maxin) {
                maxin = xou;
            }
        }
        long t = System.currentTimeMillis() - in;
        System.out.println("rps => " + (SIZE * 3000 / t) + ", Max " + maxin + ", Avg " + (t / (SIZE * 3)));
        System.out.println("Done..... " + err);

        Check.eq(0, err, "Error count not zero.");
        invoker.stop();
    }

    public static void assertt(int a, int b, String c) {
        if (a != b) {
            ++err;
            System.out.println(c);
        }
    }

}
