/**
 * BaseConfigerTest.java
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
package org.apache.niolex.network.cli.conf;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-6-3
 */
public class BaseConfigerTest {

	/**
	 * Test method for {@link org.apache.niolex.network.cli.conf.BaseConfiger#getConfig(java.lang.String)}.
	 * @throws IOException
	 */
	@Test(expected=NullPointerException.class)
	public final void testBaseConfiger() throws IOException {
	    Configer configer = new Configer("null.properties");
		assertEquals(0, configer.getConfigs().size());
	}

    @Test
    public void testSplitIntoItems() throws Exception {
        Configer configer = new Configer("/org/apache/niolex/network/cli/bui/rpc.properties");
        String[] items = Configer.splitIntoItems("    \ta \t ;\tb,c    ");
        assertEquals(3, items.length);
        assertEquals("a", items[0]);
        assertEquals("b", items[1]);
        assertEquals("c", items[2]);
        assertEquals(1, configer.getConfigs().size());
    }

    @Test
    public void testIterateProps() throws Exception {
        Configer configer = new Configer("demo.properties");
        Map<String, Head> destination = new HashMap<String, Head>();
        destination.put("demo", new Head("demo"));
        destination.put("nio-mock", new Head("nio-mock"));
        configer.iterateProps(destination);

        Head demo = destination.get("demo");
        assertEquals("/cgi-bin/services/WdgetService.cgi", demo.getServiceUrl());
        assertEquals("7000", demo.getProp("rpcTimeout"));
    }

    @Test
    public void testGuessGroup() throws Exception {
        Configer configer = new Configer("guess.properties");
        assertEquals(3, configer.getConfigs().size());
        assertEquals("18000", configer.getConfig("nio-mock").getProp("rpcErrorBlockTime"));
    }

    @Test
    public void testConfigGroup() throws Exception {
        Configer configer = new Configer("single.properties");
        assertEquals("3000", configer.getConfig().getProp("connectTimeout"));
    }

}

class Configer extends BaseConfiger<Head> {

    /**
     * Constructor
     * @param fileName
     * @throws IOException
     */
    public Configer(String fileName) throws IOException {
        super(fileName);
    }

    /**
     * This is the override of super method.
     * @see org.apache.niolex.network.cli.conf.BaseConfiger#newConfigBean(java.lang.String)
     */
    @Override
    protected Head newConfigBean(String groupName) {
        return new Head(groupName);
    }

}
