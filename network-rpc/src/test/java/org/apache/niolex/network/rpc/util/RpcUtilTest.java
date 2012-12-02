/**
 * RpcUtilTest.java
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
package org.apache.niolex.network.rpc.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;
import java.util.Date;

import org.apache.niolex.commons.compress.JacksonUtil;
import org.apache.niolex.commons.test.Benchmark;
import org.apache.niolex.commons.test.Benchmark.Bean;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.rpc.RpcException;
import org.apache.niolex.network.rpc.util.RpcUtil;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-7-24
 */
public class RpcUtilTest {

	@Test
	public void test() {
		new RpcUtil(){};
	}

	@Test
	public void testSerializeRpcException() throws Exception {
		byte[] bb = RpcUtil.serializeRpcException(new RpcException("This is good",
				RpcException.Type.CONNECTION_CLOSED, null));
		assertNotNull(bb);
		RpcException ex = RpcUtil.parseRpcException(bb);
		assertEquals("This is good", ex.getMessage());
		assertEquals("NullCause", ex.getCause().getMessage());
		assertEquals(RpcException.Type.CONNECTION_CLOSED, ex.getType());
	}

	/**
	 * Test method for {@link org.apache.niolex.network.rpc.util.RpcUtil#parseJson(byte[], java.lang.reflect.Type[])}.
	 */
	@Test
	public void testParseJson() throws Throwable {
		Benchmark bench = Benchmark.makeBenchmark();
		Bean q = new Bean(5, "Another", 523212, new Date(1338008328334L));
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		JacksonUtil.writeObj(bos, bench);
		JacksonUtil.writeObj(bos, q);
		byte[] bs = bos.toByteArray();
		Object[] re = RpcUtil.parseJson(bs, new Type[] { Benchmark.class, Bean.class });
		if (re[0] instanceof Benchmark) {
			Benchmark copy = (Benchmark) re[0];
			assertTrue(bench.equals(copy));
		} else {
			fail("Benchmark Not yet implemented");
		}
		if (re[1] instanceof Bean) {
			Bean t = (Bean) re[1];
			assertTrue(t.getId() != 0);
			assertTrue(t.getBirth().getTime() == 1338008328334L);
		} else {
			fail("Bean Not yet implemented");
		}
	}

	@Test
	public void testParseJsonEmpty() throws Throwable {
		byte[] bs = new byte[6];
		Object[] re = RpcUtil.parseJson(bs, new Type[0]);
		assertEquals(0, re.length);
	}

	/**
	 * Test method for
	 * {@link org.apache.niolex.network.rpc.util.RpcUtil#generateKey(org.apache.niolex.network.PacketData)}.
	 */
	@Test
	public void testGenerateKeyPacketData() {
		PacketData abc = new PacketData(65535);
		abc.setVersion((byte) 16);
		abc.setReserved((byte) 33);
		int k = RpcUtil.generateKey(abc);
		String s = Integer.toHexString(k);
		System.out.println(s);
		assertEquals(s, "ffff1021");
	}

	@Test
	public void testGenerateKeyMaxReser() {
		PacketData abc = new PacketData(65535);
		abc.setVersion((byte) 16);
		abc.setReserved((byte) -128);
		int k = RpcUtil.generateKey(abc);
		String s = Integer.toHexString(k);
		System.out.println(s);
		assertEquals(s, "ffff107f");
	}

	@Test
	public void testGenerateKeyMaxReser2() {
		PacketData abc = new PacketData(65535);
		abc.setVersion((byte) 16);
		abc.setReserved((byte) 128);
		int k = RpcUtil.generateKey(abc);
		String s = Integer.toHexString(k);
		System.out.println(s);
		assertEquals(s, "ffff107f");
	}

	@Test
	public void testGenerateKeyMaxReser3() {
		PacketData abc = new PacketData(65533);
		abc.setVersion((byte) 16);
		abc.setReserved((byte) 0);
		int k = RpcUtil.generateKey(abc);
		String s = Integer.toHexString(k);
		System.out.println(s);
		assertEquals(s, "fffd10ff");
	}

	@Test
	public void testGenerateKeyMaxReser4() {
		PacketData abc = new PacketData(128);
		abc.setVersion((byte) -1);
		abc.setReserved((byte) -2);
		int k = RpcUtil.generateKey(abc);
		String s = Integer.toHexString(k);
		System.out.println(s);
		assertEquals(s, "80fffd");
	}

	@Test
	public void main() throws Exception {
		short a = (short) 0xcffc;
		int q = a << 16;
		System.out.println(Integer.toHexString(q));
		byte b = -4;
		q += (b & 0xFF) << 8;
		System.out.println(Integer.toHexString(q));
		byte c = -11;
		q += (c & 0xFF);
		System.out.println(Integer.toHexString(q));
		int l = RpcUtil.generateKey(a, b, c);
		System.out.println(Integer.toHexString(l));

		byte d = -4;
		System.out.println(d % 2 == 0);
		System.out.println(d & 0xFF);
		if (d % 2 == 0) {
			--d;
		}
		System.out.println(d & 0xFF);
		System.out.println("--");
		d = 8;
		byte x = -1, y = 126;
		while (d-- > 0) {
			x = y++;
			int z = y - x;
			int s = 1;
			byte m = (byte) (y + s);
			System.out.println(y + " " + (z) + " " + (x - y) + " " + (m));
		}
	}

	/**
	 * Test method for
	 * {@link org.apache.niolex.network.rpc.util.RpcUtil#authHeader(java.lang.String, java.lang.String)}.
	 */
	@Test
	public final void testAuthHeader() {
		String s = RpcUtil.authHeader("webadmin", "IJDieio3980");
		System.out.println(s);
		assertEquals("Basic d2ViYWRtaW46SUpEaWVpbzM5ODA=", s);
	}

	/**
	 * Test method for {@link org.apache.niolex.network.rpc.util.RpcUtil#genSessionId(int)}.
	 */
	@Test
	public final void testGenSessionId() {
		String s = RpcUtil.genSessionId(45);
		String q = RpcUtil.genSessionId(45);
		assertEquals(45, s.length());
		assertEquals(45, q.length());
		assertNotEquals(s, q);
	}

	/**
	 * Test method for {@link org.apache.niolex.network.rpc.util.RpcUtil#checkServerStatus(java.lang.String, int, int)}
	 * .
	 */
	@Test
	public final void testCheckServerStatus() {
		boolean b = RpcUtil.checkServerStatus("http://www.baidu.com", 4000, 4000);
		System.out.println(b);
		assertTrue(b);
		boolean c = RpcUtil.checkServerStatus("http://www.apache.org/tomcat.php", 4000, 4000);
		System.out.println(c);
		assertFalse(c);
		boolean d = RpcUtil.checkServerStatus("http://cycqc.baidu.com/find.php", 4000, 4000);
		System.out.println(d);
		assertFalse(d);
		boolean e = RpcUtil.checkServerStatus("http://www.cs.zju.edu.cn/org/codes/404.html", 4000, 4000);
		System.out.println(e);
		assertFalse(e);
		boolean f = RpcUtil.checkServerStatus("http://www.facebook.com", 4000, 4000);
		System.out.println(f);
		assertFalse(f);
	}

}
