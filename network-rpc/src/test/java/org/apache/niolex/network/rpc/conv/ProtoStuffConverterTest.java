/**
 * ProtoStuffConverterTest.java
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

import org.apache.niolex.commons.test.Benchmark;
import org.apache.niolex.network.rpc.conv.ProtoStuffConverter;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-11-8
 */
public class ProtoStuffConverterTest {

	ProtoStuffConverter con = new ProtoStuffConverter();

	/**
	 * Test method for {@link org.apache.niolex.network.rpc.conv.ProtoStuffConverter#prepareParams(byte[], java.lang.reflect.Type[])}.
	 * @throws Exception
	 */
	@Test
	public void testPrepareParams() throws Exception {
		Benchmark ben = Benchmark.makeBenchmark();
		JsonConverterTest.Bean ban = new JsonConverterTest.Bean(89127, 'r');
		byte[] b = con.serializeParams(new Object[] {ben, ban});
		Object[] res = con.prepareParams(b, new Type[] {Benchmark.class, JsonConverterTest.Bean.class});
		assertEquals(ben, res[0]);
		assertEquals(ban, res[1]);
	}

	/**
	 * Test method for {@link org.apache.niolex.network.rpc.conv.ProtoStuffConverter#prepareReturn(byte[], java.lang.reflect.Type)}.
	 * @throws Exception
	 */
	@Test
	public void testPrepareReturn() throws Exception {
		byte[] b = con.serializeReturn("NIce to have protostuff.");
		Object r = con.prepareReturn(b, String.class);
		assertEquals(r, "NIce to have protostuff.");
	}

	@Test
    public void testGeneral() throws Exception {
        GeneralTestConverter g = new GeneralTestConverter(con);
        g.testComplicate();
        g.testReturn();
        g.testSimple();
    }

}
