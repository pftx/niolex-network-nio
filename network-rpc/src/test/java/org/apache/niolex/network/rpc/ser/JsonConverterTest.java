/**
 * JsonConverterTest.java
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
package org.apache.niolex.network.rpc.ser;

import static org.junit.Assert.*;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Date;

import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-11-8
 */
public class JsonConverterTest {

	JsonConverter con = new JsonConverter();

	public static class Bean {
		int a; char b;

		public Bean(int a, char b) {
			super();
			this.a = a;
			this.b = b;
		}

		public Bean() {
			super();
		}

		public int getA() {
			return a;
		}

		public void setA(int a) {
			this.a = a;
		}

		public char getB() {
			return b;
		}

		public void setB(char b) {
			this.b = b;
		}

		@Override
		public String toString() {
			return "Bean [a=" + a + ", b=" + b + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + a;
			result = prime * result + b;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Bean other = (Bean) obj;
			if (a != other.a)
				return false;
			if (b != other.b)
				return false;
			return true;
		}

	}

	/**
	 * Test method for {@link org.apache.niolex.network.rpc.ser.JsonConverter#prepareParams(byte[], java.lang.reflect.Type[])}.
	 * @throws Exception
	 */
	@Test
	public void testPrepareParams() throws Exception {
		byte[] abc = "\"ab\"1352344810125\"qqx\"443{\"a\":123,\"b\":\"c\"}".getBytes();
		Object[] r = con.prepareParams(abc, new Type[] {String.class, Date.class, String.class,
				Integer.class, Bean.class});
		System.out.println(Arrays.toString(r));
		assertEquals(r[2], "qqx");
	}

	/**
	 * Test method for {@link org.apache.niolex.network.rpc.ser.JsonConverter#serializeParams(java.lang.Object[])}.
	 * @throws Exception
	 */
	@Test
	public void testSerializeParams() throws Exception {
		byte[] abc = con.serializeParams(new Object[] {"ab", new Date(), "gcc", new Integer(443),
				new Bean(123, 'c')});
		System.out.println(new String(abc));
	}

	/**
	 * Test method for {@link org.apache.niolex.network.rpc.ser.JsonConverter#prepareReturn(byte[], java.lang.reflect.Type)}.
	 * @throws Exception
	 */
	@Test
	public void testPrepareReturn() throws Exception {
		Bean b = (Bean) con.prepareReturn("{\"a\":3424,\"b\":\"c\"}".getBytes(), Bean.class);
		assertEquals(b.a, 3424);
	}

	/**
	 * Test method for {@link org.apache.niolex.network.rpc.ser.JsonConverter#serializeReturn(java.lang.Object)}.
	 * @throws Exception
	 */
	@Test
	public void testSerializeReturn() throws Exception {
		con.serializeReturn(new Bean(123, 'c'));
	}

}
