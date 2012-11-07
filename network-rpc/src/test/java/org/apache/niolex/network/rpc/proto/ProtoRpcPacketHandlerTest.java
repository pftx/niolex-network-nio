/**
 * ProtoRpcPacketHandlerTest.java
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Type;

import org.apache.niolex.commons.seri.ProtoUtil;
import org.apache.niolex.network.demo.proto.PersonProtos.Person;
import org.apache.niolex.network.demo.proto.PersonProtos.Person.PhoneNumber;
import org.apache.niolex.network.demo.proto.PersonProtos.Person.PhoneType;
import org.apache.niolex.network.rpc.RpcException;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-5
 */
public class ProtoRpcPacketHandlerTest {

	/**
	 * Test method for {@link org.apache.niolex.network.rpc.proto.ProtoRpcPacketHandler#prepareParams(byte[], java.lang.reflect.Type[])}.
	 * @throws Exception
	 */
	@Test
	public void testPrepareParams() throws Exception {
		ProtoRpcPacketHandler pph = new ProtoRpcPacketHandler();
		int i = 2334;
		PhoneNumber n = PhoneNumber.newBuilder().setNumber("123122311" + i).setType(PhoneType.HOME).build();
		Person p = Person.newBuilder().setEmail("kjdfjkdf" + i + "@xxx.com").setId(45 + i)
				.setName("Niolex [" + i + "]")
				.addPhone(n)
				.build();
		byte arr2[] = ProtoUtil.seriMulti(new Object[] {n, p});
		Object[] arr = pph.prepareParams(arr2, new Type[] {PhoneNumber.class, Person.class});
		assertEquals(arr[0], n);
		assertEquals(arr[1], p);
	}

	/**
	 * Test method for {@link org.apache.niolex.network.rpc.RpcPacketHandler#handleRead(org.apache.niolex.network.PacketData, org.apache.niolex.network.IPacketWriter)}.
	 */
	@Test
	public void testPrepareParams2() throws Exception {
		ProtoRpcPacketHandler pph = new ProtoRpcPacketHandler(5);
		int i = 2334;
		Person p = Person.newBuilder().setEmail("kjdfjkdf" + i + "@xxx.com").setId(45 + i)
				.setName("Niolex [" + i + "]")
				.addPhone(PhoneNumber.newBuilder().setNumber("123122311" + i).setType(PhoneType.HOME).build())
				.build();
		byte arr2[] = ProtoUtil.seriMulti(new Object[] {p});
		assertNotNull(pph.prepareParams(arr2, new Type[] {Person.class}));
	}

	/**
	 * Test method for {@link org.apache.niolex.network.rpc.proto.ProtoRpcPacketHandler#serializeReturn(java.lang.Object)}.
	 * @throws Exception
	 */
	@Test
	public void testSerializeReturn() throws Exception {
		ProtoRpcPacketHandler pph = new ProtoRpcPacketHandler();
		int i = 66534;
		Person p = Person.newBuilder().setEmail("kjdfjkdf" + i + "@xxx.com").setId(45 + i)
				.setName("Niolex [" + i + "]")
				.addPhone(PhoneNumber.newBuilder().setNumber("123122311" + i).setType(PhoneType.MOBILE).build())
				.build();
		byte[] bb = pph.serializeReturn(p);
		assertNotNull(bb);
	}


	/**
	 * Test method for {@link org.apache.niolex.network.rpc.RpcPacketHandler#handleClose(org.apache.niolex.network.IPacketWriter)}.
	 * @throws Exception
	 */
	@Test
	public void testHandleError() throws Exception {
		ProtoRpcPacketHandler pph = new ProtoRpcPacketHandler();
		byte[] bb = pph.serializeReturn(new Exception("This is good"));
		assertNotNull(bb);
	}

	@Test(expected=RpcException.class)
	public void testHandleError2() throws Exception {
		ProtoRpcPacketHandler pph = new ProtoRpcPacketHandler();
		byte[] bb = pph.serializeReturn("This is not Protobuf");
		assertNotNull(bb);
	}

}