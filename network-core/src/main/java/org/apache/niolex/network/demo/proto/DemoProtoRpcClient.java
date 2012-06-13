/**
 * ProtoRpcClient.java
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

import java.net.InetSocketAddress;

import org.apache.niolex.network.PacketClient;
import org.apache.niolex.network.demo.proto.PersonProtos.Person;
import org.apache.niolex.network.demo.proto.PersonProtos.Person.PhoneNumber;
import org.apache.niolex.network.demo.proto.PersonProtos.Person.PhoneType;
import org.apache.niolex.network.rpc.PacketInvoker;
import org.apache.niolex.network.rpc.proto.ProtoRpcClient;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-5
 */
public class DemoProtoRpcClient {


	public static void main(String[] arg2s) throws Exception {
		// PacketClient c = new PacketClient(new InetSocketAddress("10.22.241.233", 8808));
		PacketClient c = new PacketClient(new InetSocketAddress("localhost", 8808));
		ProtoRpcClient client = new ProtoRpcClient(c, new PacketInvoker());
		client.connect();

		final PersonService ser = client.getService(PersonService.class);

		for (int i = 0; i < 5; ++i) {
			Person p = Person.newBuilder().setEmail("kjdfjkdf" + i + "@xxx.com").setId(45 + i)
					.setName("Niolex [" + i + "]")
					.addPhone(PhoneNumber.newBuilder().setNumber("123122311" + i).setType(PhoneType.MOBILE).build())
					.build();
			ser.addPerson(p);
		}
		for (int i = 0; i < 5; ++i) {
			Person p = Person.newBuilder().setId(45 + i).setName("a").build();
			p = ser.getPerson(p);
			System.out.println("Join ... " + p);
		}
		Runnable r = new Runnable() {

        	final int SIZE = 2212;

			@Override
			public void run() {
				int i = SIZE;
				long in = System.currentTimeMillis();
				long maxin = 0;
				while (i-- > 0) {
					long xin = System.currentTimeMillis();
					Person p = Person.newBuilder().setId(45).setName("a").build();
					p = ser.getPerson(p);
					if (!p.getName().startsWith("Niolex [0]")) {
						System.out.println("Out => " + p);
					}
					p = Person.newBuilder().setId(46).setName("a").build();
					p = ser.getPerson(p);
					if (!p.getName().startsWith("Niolex [1]")) {
						System.out.println("Out => " + p);
					}
					p = Person.newBuilder().setId(47).setName("a").build();
					p = ser.getPerson(p);
					if (!p.getName().startsWith("Niolex [2]")) {
						System.out.println("Out => " + p);
					}

					long xou = System.currentTimeMillis() - xin;
					if (xou > maxin) {
						maxin = xou;
					}
				}
				long t = System.currentTimeMillis() - in;
				System.out.println("rps => " + (SIZE * 3000 / t) + ", Max " + maxin + ", Avg " + (t / (SIZE * 3)));
			}};
		final int THREAD_NUM = 5;
		Thread[] ts = new Thread[THREAD_NUM];
		for (int i = 0; i < THREAD_NUM; ++i) {
			Thread t = new Thread(r);
			t.start();
			ts[i] = t;
		}
		for (int i = 0; i < THREAD_NUM; ++i) {
			ts[i].join();
			System.out.println("Join ...");
		}

		System.out.println("Done.....");
		client.stop();
	}

}
