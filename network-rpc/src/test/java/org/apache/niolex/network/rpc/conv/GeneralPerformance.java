/**
 * GeneralPerformance.java
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

import org.apache.niolex.commons.test.MockUtil;
import org.apache.niolex.commons.test.Performance;
import org.apache.niolex.commons.bean.Pair;
import org.apache.niolex.network.rpc.IConverter;
import org.codehaus.jackson.type.TypeReference;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0, $Date: 2012-12-2$
 */
public class GeneralPerformance {

    public static void main(String[] args) {
        System.out.println("JSON");
        final JsonConverter jcon = new JsonConverter();
        final GeneralTestConverter jt = new GeneralTestConverter(jcon);
        Performance jp = new Performance(100, 100) {
            @Override
            protected void run() {
                try {
                    bytePerfom(jcon);
                    jt.testReturn();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        jp.start();
    }

    public static void bytePerfom(IConverter iconv) throws Exception {
        byte[] abc = MockUtil.randByteArray(4096);

        byte[] mid = iconv.serializeReturn(new Pair<String, byte[]>("MM", abc));
        System.out.println("Byte test " + mid.length);
        @SuppressWarnings("unchecked")
        Pair<String, byte[]> awt = (Pair<String, byte[]>) iconv.prepareReturn(mid, new TypeReference<Pair<String, byte[]>>() {
        }.getType());
        byte[] aft = awt.b;
        assertEquals(abc[38], aft[38]);
        assertEquals(abc[138], aft[138]);
        assertEquals(abc[238], aft[238]);
    }

}
