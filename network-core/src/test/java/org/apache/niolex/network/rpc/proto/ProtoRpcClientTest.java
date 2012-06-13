/**
 * ProtoRpcClientTest.java
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
package org.apache.niolex.network.rpc.proto;

import static org.junit.Assert.*;

import org.apache.niolex.network.PacketClient;
import org.apache.niolex.network.demo.proto.PersonProtos.Person;
import org.apache.niolex.network.demo.proto.PersonProtos.Person.PhoneNumber;
import org.apache.niolex.network.demo.proto.PersonProtos.Person.PhoneType;
import org.apache.niolex.network.rpc.PacketInvoker;
import org.apache.niolex.network.rpc.RpcException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-5
 */
public class ProtoRpcClientTest {

	@Mock
	private PacketClient client;
	private ProtoRpcClient protoRpcClient;

	@Before
	public void createProtoRpcClient() throws Exception {
		protoRpcClient = new ProtoRpcClient(client, new PacketInvoker());
	}

	/**
	 * Test method for {@link org.apache.niolex.network.rpc.proto.ProtoRpcClient#serializeParams(java.lang.Object[])}.
	 * @throws Exception
	 */
	@Test
	public void testSerializeParams() throws Exception {
		int i = 1231;
		Person p = Person.newBuilder().setEmail("kjdfjkdf" + i + "@xxx.com").setId(45 + i)
				.setName("Niolex [" + i + "]")
				.addPhone(PhoneNumber.newBuilder().setNumber("123122311" + i).setType(PhoneType.WORK).build())
				.build();
		byte[] bs = protoRpcClient.serializeParams(new Object[] { p });
		Person q = Person.parseFrom(bs);
		assertEquals(p, q);
	}

	@Test(expected=RpcException.class)
	public void testSerializeInvalidParams() throws Exception {
		int i = 1231;
		Person p = Person.newBuilder().setEmail("kjdfjkdf" + i + "@xxx.com").setId(45 + i)
				.setName("Niolex [" + i + "]")
				.addPhone(PhoneNumber.newBuilder().setNumber("123122311" + i).setType(PhoneType.MOBILE).build())
				.build();
		byte[] bs = protoRpcClient.serializeParams(new Object[] { p, "Nice" });
		Person q = Person.parseFrom(bs);
		assertEquals(p, q);
	}

	/**
	 * Test method for {@link org.apache.niolex.network.rpc.proto.ProtoRpcClient#prepareReturn(byte[], java.lang.reflect.Type)}.
	 * @throws Exception
	 */
	@Test
	public void testPrepareReturnByteArrayType() throws Exception {
		int i = 9834;
		Person p = Person.newBuilder().setEmail("kjdfjkdf" + i + "@xxx.com").setId(45 + i)
				.setName("Niolex [" + i + "]")
				.addPhone(PhoneNumber.newBuilder().setNumber("123122311" + i).setType(PhoneType.MOBILE).build())
				.build();
		byte[] bs = p.toByteArray();
		Object q = protoRpcClient.prepareReturn(bs, Person.class);
		assertEquals(p, q);
	}

}
