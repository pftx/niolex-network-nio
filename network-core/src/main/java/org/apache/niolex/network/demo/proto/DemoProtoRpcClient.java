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
		ProtoRpcClient client = new ProtoRpcClient(c);
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
		System.out.println("Done.....");
		c.stop();
	}

}
