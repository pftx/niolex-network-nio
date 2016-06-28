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

import org.apache.niolex.network.client.PacketClient;
import org.apache.niolex.network.demo.proto.PersonProtos.Person;
import org.apache.niolex.network.demo.proto.PersonProtos.PhoneNumber;
import org.apache.niolex.network.demo.proto.PersonProtos.PhoneType;
import org.apache.niolex.network.demo.proto.PersonProtos.Work;
import org.apache.niolex.network.rpc.PacketInvoker;
import org.apache.niolex.network.rpc.RpcClient;
import org.apache.niolex.network.rpc.conv.ProtobufConverter;

/**
 * Demo client
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0, Date: 2012-6-5
 */
public class DemoProtoRpcClient {


	public static void main(String[] arg2s) throws Exception {
		PacketClient c = new PacketClient(new InetSocketAddress("localhost", 8808));
		RpcClient client = new RpcClient(c, new PacketInvoker(), new ProtobufConverter());
		client.connect();

		final PersonService ser = client.getService(PersonService.class);

		for (int i = 0; i < 5; ++i) {
		    Work w = Work.newBuilder().setReportTo(3).setPosition("SSE").setSalary(16000).build();
			PhoneNumber n = PhoneNumber.newBuilder().setNumber("123122311" + i).setType(PhoneType.MOBILE).build();
			Person p = Person.newBuilder().setEmail("kjdfjkdf" + i + "@xxx.com").setId(45 + i).setWork(w)
					.setName("Niolex [" + i + "]").addPhone(n).build();
			ser.addPerson(p, n);
		}
		for (int i = 0; i < 5; ++i) {
			PhoneNumber n = PhoneNumber.newBuilder().setNumber("123122311" + i).setType(PhoneType.MOBILE).build();
			Person p = ser.getPerson(n);
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
					Person p = null;
					PhoneNumber n = null;
					n = PhoneNumber.newBuilder().setNumber("123122311" + 0).setType(PhoneType.MOBILE).build();
					p = ser.getPerson(n);
					assertt(p.getName().startsWith("Niolex [0]"), "Out 0 => " + p);

					n = PhoneNumber.newBuilder().setNumber("123122311" + 1).setType(PhoneType.MOBILE).build();
					p = ser.getPerson(n);
					assertt(p.getName().startsWith("Niolex [1]"), "Out 1 => " + p);

					n = PhoneNumber.newBuilder().setNumber("123122311" + 2).setType(PhoneType.MOBILE).build();
					p = ser.getPerson(n);
					assertt(p.getName().startsWith("Niolex [2]"), "Out 2 => " + p);

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

	public static void assertt(boolean b, String c) {
		if (!b) {
			System.out.println(c);
		}
	}

}
