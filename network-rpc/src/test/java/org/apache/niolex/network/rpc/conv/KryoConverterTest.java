/**
 * KryoConverterTest.java
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

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Type;

import org.apache.niolex.commons.test.Benchmark;
import org.apache.niolex.commons.bean.Pair;
import org.apache.niolex.network.rpc.conv.JsonConverterTest.Bean;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0, $Date: 2012-12-2$
 */
public class KryoConverterTest {

    KryoConverter con = new KryoConverter();
    GeneralTestConverter g = new GeneralTestConverter(con);

    /**
     * Test method for {@link org.apache.niolex.network.rpc.conv.KryoConverter#prepareParams(byte[], java.lang.reflect.Type[])}.
     * @throws Exception
     */
    @Test
    public final void testPrepareParams() throws Exception {
        g.testComplicate();
    }

    /**
     * Test method for {@link org.apache.niolex.network.rpc.conv.KryoConverter#serializeParams(java.lang.Object[])}.
     * @throws Exception
     */
    @Test
    public final void testSerializeParams() throws Exception {
        g.testSimple();
    }

    /**
     * Test method for {@link org.apache.niolex.network.rpc.conv.KryoConverter#prepareReturn(byte[], java.lang.reflect.Type)}.
     * @throws Exception
     */
    @Test
    public final void testPrepareReturn() throws Exception {
        g.testReturn();
    }

    /**
     * Test method for {@link org.apache.niolex.network.rpc.conv.KryoConverter#serializeReturn(java.lang.Object)}.
     * @throws Exception
     */
    @Test
    public final void testSerializeReturn() throws Exception {
        Object[] params = new Object[5];
        params[0] = Benchmark.makeBenchmark();
        params[1] = new Pair<String, Integer>("SST", 119);
        params[2] = "x.j.y";
        params[3] = Benchmark.Group.makeGroup();
        params[4] = new Bean(5758, 'a');
        byte[] abc = con.serializeParams(params);
        System.out.println("Com Ser Size " + abc.length);
        Object[] r = con.prepareParams(abc, new Type[] {Benchmark.class, Pair.class, String.class, Benchmark.Group.class,
                Bean.class});
        assertEquals(r[0], params[0]);
        assertEquals(r[2], params[2]);
        assertEquals(r[3], params[3]);
        assertEquals(r[4], params[4]);    }

}
