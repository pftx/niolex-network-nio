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

import static org.junit.Assert.*;

import org.apache.niolex.network.demo.proto.PersonProtos.Person;
import org.apache.niolex.network.demo.proto.PersonProtos.PhoneNumber;
import org.apache.niolex.network.demo.proto.PersonProtos.PhoneType;
import org.apache.niolex.network.demo.proto.PersonProtos.Work;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-6-5
 */
public class DemoProtoRpcServerTest {

	/**
	 * Test method for {@link org.apache.niolex.network.demo.proto.DemoProtoRpcServer#main(java.lang.String[])}.
	 * @throws Throwable
	 */
	@Test
	public void testMain() throws Throwable {
		DemoProtoRpcServer.main(null);
		DemoProtoRpcClient.main(null);
		DemoProtoRpcServer.stop();
	}

	/**
	 * Test method for {@link org.apache.niolex.network.demo.proto.DemoProtoRpcServer#stop()}.
	 */
	@Test
	public void testStop() {
		DemoProtoRpcServer pr = new DemoProtoRpcServer();
		pr.toString();
		DemoProtoRpcClient qr = new DemoProtoRpcClient();
		qr.toString();
		PersonServiceImpl impl = new PersonServiceImpl();
		int i = 982341;
		PhoneNumber n = PhoneNumber.newBuilder().setNumber("123122311" + i).setType(PhoneType.HOME).build();
		PhoneNumber m = PhoneNumber.newBuilder().setNumber("109302109" + i).setType(PhoneType.HOME).build();
		Work w = Work.newBuilder().setReportTo(3).setPosition("SSE").setSalary(16000).build();
		Person p = Person.newBuilder().setEmail("kjdfjkdf" + i + "@xxx.com").setId(45 + i)
				.setName("Niolex [" + i + "]")
				.addPhone(n).setWork(w)
				.build();
		Person q = Person.newBuilder().setEmail("oe;lda" + i + "@xxx.com").setId(45 + i)
		        .setName("Niolex Inc. CEO")
		        .addPhone(n).setWork(w)
		        .build();
		impl.updatePerson(q);
		impl.addPerson(p, n);
		impl.updatePerson(q);
		impl.addPerson(q, n);
		Person r = impl.getPerson(n);
		assertEquals(r, q);
		Person s = impl.getPerson(m);
		assertNull(s);
		impl.updatePerson(p);
	}

}
