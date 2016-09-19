/**
 * AddressPublisherDemo.java
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
package org.apache.niolex.network.name.demo;

import java.io.InputStream;
import java.util.Scanner;

import org.apache.niolex.network.name.client.AddressPublisher;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-6-27
 */
public class AddressPublisherDemo {

    private static InputStream in = System.in;

    /**
     * The Client Demo
     *
     * @param args
     */
    public static void main(String[] args) throws Exception {
    	AddressPublisher c = new AddressPublisher("localhost:8181");
        c.pushlishService("network/name", "localhost:8181");
        @SuppressWarnings("resource")
        Scanner sc = new Scanner(in);
        while (true) {
            System.out.print("Please enter the next code(-1 for exit): ");
            short code = sc.nextShort();
            if (code == -1) {
                break;
            }
            sc.nextLine();
            System.out.print("Please enter the Service Key: ");
            String msg = sc.nextLine();
            System.out.print("Please enter the Service Value: ");
            String val = sc.nextLine();
            c.pushlishService(msg, val);
        }
        Thread.sleep(1000);
        c.stop();
    }

    /**
     * @param in the in to set
     */
    public static void setIn(InputStream in) {
    	AddressPublisherDemo.in = in;
    }
}
