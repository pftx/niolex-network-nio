/**
 * ProtoRpcServerTest.java
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

import org.apache.niolex.network.demo.proto.PersonProtos.Person;
import org.apache.niolex.network.demo.proto.PersonProtos.Person.PhoneNumber;
import org.apache.niolex.network.demo.proto.PersonProtos.Person.PhoneType;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-5
 */
public class ProtoRpcServerTest {

	/**
	 * Test method for {@link org.apache.niolex.network.demo.proto.ProtoRpcServer#main(java.lang.String[])}.
	 * @throws Throwable
	 */
	@Test
	public void testMain() throws Throwable {
		ProtoRpcServer.main(null);
		DemoProtoRpcClient.main(null);
		ProtoRpcServer.stop();
	}

	/**
	 * Test method for {@link org.apache.niolex.network.demo.proto.ProtoRpcServer#stop()}.
	 */
	@Test
	public void testStop() {
		ProtoRpcServer pr = new ProtoRpcServer();
		pr.toString();
		DemoProtoRpcClient qr = new DemoProtoRpcClient();
		qr.toString();
		PersonServiceImpl impl = new PersonServiceImpl();
		int i = 982341;
		Person p = Person.newBuilder().setEmail("kjdfjkdf" + i + "@xxx.com").setId(45 + i)
				.setName("Niolex [" + i + "]")
				.addPhone(PhoneNumber.newBuilder().setNumber("123122311" + i).setType(PhoneType.HOME).build())
				.build();
		impl.addPerson(p);
		impl.getPerson(p);
	}

}
