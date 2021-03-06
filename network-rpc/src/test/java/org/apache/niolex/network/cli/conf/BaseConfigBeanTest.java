/**
 * BaseConfigBeanTest.java
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

import org.apache.niolex.network.cli.conf.BaseConfigBean;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-6-3
 */
public class BaseConfigBeanTest {

	@Test
    public final void testSetConfigWithHeader() {
	    BaseConfigBean c = new BaseConfigBean("FFG");
	    assertEquals("FFG", c.getGroupName());
	    assertFalse(c.hasHeader());
	    c.setConfig("header.auth", "abc:de");
	    c.setConfig("hasHeader", "true");
	    c.setConfig("header.ref", "lex");
	    c.setConfig("hrl", "rpc://g");
	    assertNull(c.getHeader("auth"));
	    assertEquals("abc:de", c.getProp("header.auth"));
	    assertTrue(c.hasHeader());
	    assertEquals("lex", c.getHeader("ref"));
	    assertEquals("rpc://g", c.getProp("hrl"));
	    c.setConfig("hasHeader", "nice");
	    assertFalse(c.hasHeader());
	}

	/**
	 * Test method for {@link org.apache.niolex.network.cli.conf.BaseConfigBean#getProp(java.lang.String)}.
	 */
	@Test
	public final void testGetPropString() {
		BaseConfigBean c = new BaseConfigBean("FFF");
		c.setConfig("yet", "true");
		c.setConfig("method", "network");
		assertEquals("true", c.getProp("yet"));
		assertEquals("network", c.getProp("method"));
		assertEquals("FFF", c.getGroupName());
	}

	/**
	 * Test method for {@link org.apache.niolex.network.cli.conf.BaseConfigBean#getHeader(java.lang.String)}.
	 */
	@Test
	public final void testGetHeaderString() {
		BaseConfigBean c = new BaseConfigBean("FFF");
		c.setConfig("hasHeader", "true");
		c.setConfig("header.cool", "Nice");
		assertEquals("Nice", c.getHeader("cool"));
		assertTrue(c.hasHeader());
	}

	@Test
	public final void testGetHeaderStringn() {
		BaseConfigBean c = new BaseConfigBean("FFF");
		c.setConfig("header.cool", "Nice");
		assertEquals(null, c.getHeader("cool"));
	}

	/**
	 * Test method for {@link org.apache.niolex.network.cli.conf.BaseConfigBean#getHeader()}.
	 */
	@Test
	public final void testGetHeader() {
		BaseConfigBean c = new BaseConfigBean("FFF");
		c.setConfig("hasHeader", "true");
		c.setConfig("header.cool", "Nice");
		c.setConfig("header.link", "BaseConfigBean");
		assertEquals(2, c.getHeader().size());
	}

	/**
	 * Test method for {@link org.apache.niolex.network.cli.conf.BaseConfigBean#getGroupName()}.
	 */
	@Test
	public final void testGetGroupName() {
		BaseConfigBean c = new BaseConfigBean("FFF");
		c.setConfig("yet", "true");
		c.setConfig("method", "network");
		assertEquals(2, c.getProp().size());
	}

	@Test
	public void testSetField() throws Exception {
		Head h = new Head("ghgh");
		h.setConfig("kill", "912039298102988");
		h.setConfig("time", "32767");
		assertEquals(912039298102988l, h.getKill());
		assertEquals(32767, h.getTime());
	}

}

class Head extends BaseConfigBean {
    private long kill;
    private short time;
    private String serviceUrl;

    public Head(String groupName) {
        super(groupName);
    }

    public long getKill() {
        return kill;
    }

    public short getTime() {
        return time;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

}
