/**
 * PoolHandlerPoolTest.java
 *
 * Copyright 2014 the original author or authors.
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
package org.apache.niolex.network.cli;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.apache.niolex.network.cli.handler.IServiceHandler;
import org.junit.Test;

import com.google.common.collect.Lists;

/**
 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2014-1-3
 */
public class PoolHandlerPoolTest {

    PoolHandler<IServiceHandler> pool;

    public void set(int k) {
        List<IServiceHandler> list = Lists.newArrayList();
        for (int i = 0; i < k; ++i) {
            IServiceHandler m = mock(IServiceHandler.class);
            when(m.isReady()).thenReturn(i % 2 == 0);
            when(m.getServiceUrl()).thenReturn(Integer.toString(i));
            list.add(m);
        }
        pool = new PoolHandler<IServiceHandler>(list, 2);
    }

    /**
     * Test method for {@link org.apache.niolex.network.cli.PoolHandler#take()}.
     */
    @Test
    public void testTake() {
        set(3);
        pool.setWaitTimeout(3);
        IServiceHandler take = pool.take();
        assertEquals("0", take.getServiceUrl());
        // ----
        take = pool.take();
        assertEquals("2", take.getServiceUrl());
        assertNull(pool.take());
        // ---- again
        assertNull(pool.take());
    }

    /**
     * Test method for {@link org.apache.niolex.network.cli.PoolHandler#takeOne(int)}.
     */
    @Test
    public void testTakeOne() {
        set(1);
        pool.setWaitTimeout(1);
        assertNotNull(pool.take());
        // ---- timeout
        assertNull(pool.take());
        IServiceHandler m = mock(IServiceHandler.class);
        pool.offer(m);
        assertEquals(m, pool.takeOne(5));
    }

}
