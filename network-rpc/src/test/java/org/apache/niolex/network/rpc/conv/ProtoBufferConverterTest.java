/**
 * ProtoBufferConverterTest.java
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
package org.apache.niolex.network.rpc.conv;

import static org.junit.Assert.*;

import java.lang.reflect.Type;

import org.apache.niolex.commons.seri.ProtoUtil;
import org.apache.niolex.commons.seri.SeriException;
import org.apache.niolex.network.demo.proto.PersonProtos.Person;
import org.apache.niolex.network.demo.proto.PersonProtos.Person.PhoneNumber;
import org.apache.niolex.network.demo.proto.PersonProtos.Person.PhoneType;
import org.apache.niolex.network.rpc.RpcException;
import org.apache.niolex.network.rpc.conv.ProtoBufferConverter;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-11-8
 */
public class ProtoBufferConverterTest {

	ProtoBufferConverter con = new ProtoBufferConverter();

	/**
	 * Test method for {@link org.apache.niolex.network.rpc.conv.ProtoBufferConverter#prepareParams(byte[], java.lang.reflect.Type[])}.
	 * @throws Exception
	 */
	@Test
	public void testPrepareParams() throws Exception {
		int i = 2334;
		PhoneNumber n = PhoneNumber.newBuilder().setNumber("123122311" + i).setType(PhoneType.HOME).build();
		Person p = Person.newBuilder().setEmail("kjdfjkdf" + i + "@xxx.com").setId(45 + i)
				.setName("Niolex [" + i + "]")
				.addPhone(n)
				.build();
		byte arr2[] = ProtoUtil.seriMulti(new Object[] {n, p});
		Object[] arr = con.prepareParams(arr2, new Type[] {PhoneNumber.class, Person.class});
		assertEquals(arr[0], n);
		assertEquals(arr[1], p);
	}

	@Test
	public void testPrepareParams2() throws Exception {
		int i = 2334;
		Person p = Person.newBuilder().setEmail("kjdfjkdf" + i + "@xxx.com").setId(45 + i)
				.setName("Niolex [" + i + "]")
				.addPhone(PhoneNumber.newBuilder().setNumber("123122311" + i).setType(PhoneType.HOME).build())
				.build();
		byte arr2[] = ProtoUtil.seriMulti(new Object[] {p});
		assertNotNull(con.prepareParams(arr2, new Type[] {Person.class}));
	}

	/**
	 * Test method for {@link org.apache.niolex.network.rpc.conv.ProtoBufferConverter#serializeParams(java.lang.Object[])}.
	 * @throws Exception
	 */
	@Test
	public void testSerializeParams() throws Exception {
		int i = 1231;
		Person p = Person.newBuilder().setEmail("kjdfjkdf" + i + "@xxx.com").setId(45 + i)
				.setName("Niolex [" + i + "]")
				.addPhone(PhoneNumber.newBuilder().setNumber("123122311" + i).setType(PhoneType.WORK).build())
				.build();
		byte[] bs = con.serializeParams(new Object[] { p });
		Person q = (Person) ProtoUtil.parseMulti(bs, new Type[] { Person.class })[0];
		assertEquals(p, q);
	}

	@Test(expected=SeriException.class)
	public void testSerializeInvalidParams() throws Exception {
		int i = 1231;
		Person p = Person.newBuilder().setEmail("kjdfjkdf" + i + "@xxx.com").setId(45 + i)
				.setName("Niolex [" + i + "]")
				.addPhone(PhoneNumber.newBuilder().setNumber("123122311" + i).setType(PhoneType.MOBILE).build())
				.build();
		byte[] bs = con.serializeParams(new Object[] { p, "Nice" });
		Person q = Person.parseFrom(bs);
		assertEquals(p, q);
	}

	/**
	 * Test method for {@link org.apache.niolex.network.rpc.conv.ProtoBufferConverter#prepareReturn(byte[], java.lang.reflect.Type)}.
	 * @throws Exception
	 */
	@Test
	public void testPrepareReturn() throws Exception {
		int i = 9834;
		Person p = Person.newBuilder().setEmail("kjdfjkdf" + i + "@xxx.com").setId(45 + i)
				.setName("Niolex [" + i + "]")
				.addPhone(PhoneNumber.newBuilder().setNumber("123122311" + i).setType(PhoneType.MOBILE).build())
				.build();
		byte[] bs = p.toByteArray();
		Object q = con.prepareReturn(bs, Person.class);
		assertEquals(p, q);
	}

	/**
	 * Test method for {@link org.apache.niolex.network.rpc.conv.ProtoBufferConverter#serializeReturn(java.lang.Object)}.
	 * @throws Exception
	 */
	@Test
	public void testSerializeReturn() throws Exception {
		int i = 66534;
		Person p = Person.newBuilder().setEmail("kjdfjkdf" + i + "@xxx.com").setId(45 + i)
				.setName("Niolex [" + i + "]")
				.addPhone(PhoneNumber.newBuilder().setNumber("123122311" + i).setType(PhoneType.MOBILE).build())
				.build();
		byte[] bb = con.serializeReturn(p);
		assertNotNull(bb);
	}

	@Test(expected=RpcException.class)
	public void testHandleError() throws Exception {
		byte[] bb = con.serializeReturn("This is not Protobuf");
		assertNotNull(bb);
	}
}
