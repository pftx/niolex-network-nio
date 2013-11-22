/**
 * BufferManagerTest.java
 *
 * Copyright 2013 the original author or authors.
 *
 * We licenses this file to you under the Apache License, version 2.0
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
package org.apache.niolex.network.server;


import static org.junit.Assert.*;

import java.nio.ByteBuffer;

import org.apache.niolex.network.Config;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2013-11-22
 */
public class BufferManagerTest {

    private BufferManager bufMgr;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        bufMgr = new BufferManager();
    }

    @Test
    public void testGetBuffer() throws Exception {
        ByteBuffer buf = bufMgr.getBuffer();
        assertNotNull(buf);
        assertEquals(Config.SERVER_DIRECT_BUFFER_SIZE, buf.capacity());
    }

    @Test
    public void testGiveBack() throws Exception {
        bufMgr.setMaxQueueSize(1);
        ByteBuffer buf;
        buf = ByteBuffer.allocate(8);
        assertTrue(bufMgr.giveBack(buf));
        assertFalse(bufMgr.giveBack(buf));
    }

    @Test
    public void testGetMaxQueueSize() throws Exception {
        assertEquals(Config.BUFFER_MGR_MAX_QUEUE_SIZE, bufMgr.getMaxQueueSize());
        ByteBuffer buf;
        buf = ByteBuffer.allocate(8);
        assertTrue(bufMgr.giveBack(buf));
        assertEquals(buf, bufMgr.getBuffer());
    }

    @Test
    public void testSetMaxQueueSize() throws Exception {
        bufMgr.setMaxQueueSize(-1);
        assertEquals(-1, bufMgr.getMaxQueueSize());
    }

}
