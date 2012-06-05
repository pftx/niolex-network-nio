/**
 * ProtoUtilTest.java
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

import java.io.ByteArrayInputStream;
import java.sql.Date;

import org.apache.niolex.commons.test.MockUtil;
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
public class ProtoUtilTest {

	/**
	 * Test method for {@link org.apache.niolex.network.rpc.proto.ProtoUtil#parseOne(byte[], java.lang.reflect.Type)}.
	 */
	@Test(expected = RpcException.class)
	public void testParseOneByteArrayType() {
		byte[] ret = MockUtil.randByteArray(12);
		ProtoUtil.parseOne(ret, null);
	}

	/**
	 * Test method for {@link org.apache.niolex.network.rpc.proto.ProtoUtil#parseOne(byte[], java.lang.reflect.Type)}.
	 */
	@Test(expected = RpcException.class)
	public void testParseOneByteArrayType2() {
		byte[] ret = MockUtil.randByteArray(12);
		ProtoUtil.parseOne(ret, String.class);
	}

	/**
	 * Test method for {@link org.apache.niolex.network.rpc.proto.ProtoUtil#parseOne(byte[], java.lang.reflect.Type)}.
	 */
	@Test
	public void testParseOneByteArrayType3() {
		int i = 2345;
		Person p = Person.newBuilder().setEmail("kjdfjkdf" + i + "@xxx.com").setId(45 + i)
				.setName("Niolex [" + i + "]")
				.addPhone(PhoneNumber.newBuilder().setNumber("123122311" + i).setType(PhoneType.MOBILE).build())
				.build();
		byte[] ret = p.toByteArray();
		Person q = (Person) ProtoUtil.parseOne(ret, Person.class);
		assertEquals(p, q);
	}

	/**
	 * Test method for
	 * {@link org.apache.niolex.network.rpc.proto.ProtoUtil#parseOne(java.io.InputStream, java.lang.reflect.Type)}.
	 */
	@Test(expected = RpcException.class)
	public void testParseOneInputStreamType() {
		byte[] ret = MockUtil.randByteArray(12);
		ByteArrayInputStream in = new ByteArrayInputStream(ret);
		ProtoUtil.parseOne(in, null);
	}

	/**
	 * Test method for
	 * {@link org.apache.niolex.network.rpc.proto.ProtoUtil#parseOne(java.io.InputStream, java.lang.reflect.Type)}.
	 */
	@Test(expected = RpcException.class)
	public void testParseOneInputStreamType2() {
		byte[] ret = MockUtil.randByteArray(12);
		ByteArrayInputStream in = new ByteArrayInputStream(ret);
		ProtoUtil.parseOne(in, Date.class);
	}

	/**
	 * Test method for
	 * {@link org.apache.niolex.network.rpc.proto.ProtoUtil#parseOne(java.io.InputStream, java.lang.reflect.Type)}.
	 */
	@Test
	public void testParseOneInputStreamType3() {
		int i = 563432;
		Person p = Person.newBuilder().setEmail("kjdfjkdf" + i + "@xxx.com").setId(45 + i)
				.setName("Niolex [" + i + "]")
				.addPhone(PhoneNumber.newBuilder().setNumber("123122311" + i).setType(PhoneType.MOBILE).build())
				.build();
		byte[] ret = p.toByteArray();
		ByteArrayInputStream in = new ByteArrayInputStream(ret);
		ProtoUtil.parseOne(in, Person.class);
	}

}
