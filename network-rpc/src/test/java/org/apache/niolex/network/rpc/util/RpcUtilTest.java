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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;

import org.apache.niolex.commons.codec.StringUtil;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.rpc.RpcException;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-7-24
 */
public class RpcUtilTest extends RpcUtil {

	@Test
	public void testSerializeRpcWithRoot() {
		Exception e = new ArrayStoreException("Can not alloc memory.#49$");
		RpcException ex = new RpcException("This is not Good.", RpcException.Type.ERROR_INVOKE, e);
		byte[] bb = RpcUtil.serializeRpcException(ex);
		assertNotNull(bb);
		System.out.println(StringUtil.utf8ByteToStr(bb));
		RpcException ox = RpcUtil.parseRpcException(bb);
		assertEquals("This is not Good.", ox.getMessage());
        assertEquals(RpcException.Type.ERROR_INVOKE, ox.getType());
        System.out.println(ox.getCause());
	}

    @Test
    public void testSerializeRpcWithInvocationTargetException() throws Exception {
        Exception e2 = new ArrayStoreException("Can not alloc memory.#49$");
        Exception e = new InvocationTargetException(e2);
        RpcException ex = new RpcException("This is not Good.", RpcException.Type.ERROR_INVOKE, e);
        byte[] bb = RpcUtil.serializeRpcException(ex);
        assertNotNull(bb);
        System.out.println(StringUtil.utf8ByteToStr(bb));
        RpcException ox = RpcUtil.parseRpcException(bb);
        assertEquals("This is not Good.", ox.getMessage());
        assertEquals(RpcException.Type.ERROR_INVOKE, ox.getType());
        System.out.println(ox.getCause());
    }

	@Test
	public void testSerializeRpcExceptionNoRoot() throws Exception {
		byte[] bb = RpcUtil.serializeRpcException(new RpcException("This is good",
				RpcException.Type.CONNECTION_CLOSED, null));
		assertNotNull(bb);
		RpcException ex = RpcUtil.parseRpcException(bb);
		assertEquals("This is good", ex.getMessage());
		assertEquals(RpcException.Type.CONNECTION_CLOSED, ex.getType());
		assertNull(ex.getCause());
	}

    @Test
    public void testParseRpcException() throws Exception {
        String s = "This is not Good.#~@&ERROR_INVOKE#~@&java.lang2.ArrayStoreException/^*(V)*^/Can not alloc memory.#49$/^*(V)*^/RpcUtilTest";
        byte[] bb = StringUtil.strToAsciiByte(s);
        RpcException ox = RpcUtil.parseRpcException(bb);
        assertEquals("This is not Good.", ox.getMessage());
        assertEquals(RpcException.Type.ERROR_INVOKE, ox.getType());
        System.out.println(ox.getCause());
        assertTrue(ox.getCause() instanceof ClassNotFoundException);
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
    public void testGenerateKeyShortByteByte() throws Exception {
        PacketData abc = new PacketData(65535);
        byte vr = 66;
        byte rr = 125;
        for (int i = 0; i < 1000; ++i) {
            abc.setVersion(vr++);
            abc.setReserved(rr++);
            int k = generateKey(abc);
            abc.setReserved(rr++);
            int s = generateKey(abc);
            assertEquals(s, k);
        }
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
	public void testGenerateKey10000() {
	    PacketData abc = new PacketData(128);
	    for (int i = 1; i < 200000; i += 2) {
	        abc.setVersion((byte) (i >> 8));
	        abc.setReserved((byte) i);
	        int k = RpcUtil.generateKey(abc);
	        abc.setReserved((byte) (abc.getReserved() + 1));
	        int q = RpcUtil.generateKey(abc);
	        assertEquals(k, q);
	    }
	}

	@Test
	public void testGenerateKey20000() {
	    PacketData abc = new PacketData(128);
	    for (int i = 2147483641, j = 0; j < 10000; i += 2, ++j) {
	        abc.setVersion((byte) (i >> 8));
	        abc.setReserved((byte) i);
	        int k = RpcUtil.generateKey(abc);
	        abc.setReserved((byte) (abc.getReserved() + 1));
	        int q = RpcUtil.generateKey(abc);
	        assertEquals(k, q);
	    }
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
		String s = RpcUtil.genSessionId(145);
		String q = RpcUtil.genSessionId(145);
		assertEquals(145, s.length());
		assertEquals(145, q.length());
		assertNotEquals(s, q);
	}

	/**
	 * Test method for {@link org.apache.niolex.network.rpc.util.RpcUtil#checkServerStatus(java.lang.String, int, int)}
	 * .
	 */
	@Test
	public final void testCheckServerStatusLenMinus1() {
		boolean b = RpcUtil.checkServerStatus("http://www.baidu.com", 4000, 4000);
		assertTrue(b);
	}

	@Test
	public final void testCheckServerStatusLessThan2() {
	    String url = RpcUtilTest.class.getResource("onebyte.txt").toExternalForm();
	    boolean d = RpcUtil.checkServerStatus(url, 4000, 4000);
	    assertFalse(d);
	}

	@Test
	public final void testCheckServerStatusEx404() {
	    boolean e = RpcUtil.checkServerStatus("http://www.cs.zju.edu.cn/org/codes/404.html", 4000, 4000);
	    assertFalse(e);
	}

	@Test
    public final void testCheckServerStatusEx404Again() {
		boolean c = RpcUtil.checkServerStatus("http://www.apache.org/tomcat.php", 4000, 4000);
		assertFalse(c);
    }

    @Test
	public final void testCheckServerStatusIOEx() {
		boolean f = RpcUtil.checkServerStatus("http://www.facebook.com", 1000, 1000);
		assertFalse(f);
	}

    @Test(expected=IllegalArgumentException.class)
    public final void testCheckServerStatusNull() {
        boolean f = RpcUtil.checkServerStatus("http://www.facebook.com", -1000, -1000);
        assertFalse(f);
    }

}
