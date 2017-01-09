/**
 * SelectorHolderTest.java
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
package org.apache.niolex.network.server;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-11-7
 */
@RunWith(MockitoJUnitRunner.class)
public class SelectorHolderTest {

	@Mock
	private Selector selector;

	@Mock
	private Thread selectorThread;

	@InjectMocks
	private SelectorHolder selectorHolder;

	/**
    	 * Test method for {@link org.apache.niolex.network.server.SelectorHolder#attacheWrite(java.nio.channels.SelectionKey)}.
    	 */
    	@Test
    	public void testAttacheWrite() {
    		SelectionKey selectionKey = mock(SelectionKey.class);
    		assertNotEquals(selectorThread, Thread.currentThread());
    		selectorHolder.attacheWrite(selectionKey);
    		verify(selectionKey, never()).interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    		verify(selector).wakeup();
    		selectorHolder.changeAllInterestOps();
    		verify(selectionKey).interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    	}

	/**
         * Test method for {@link org.apache.niolex.network.server.SelectorHolder#attacheWrite(java.nio.channels.SelectionKey)}.
         */
        @Test
        public void testAttacheWriteCurrentThread() {
            SelectorHolder sel = new SelectorHolder(Thread.currentThread(), selector);
            SelectionKey selectionKey = mock(SelectionKey.class);
            sel.attacheWrite(selectionKey);
            verify(selectionKey).interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            verify(selector, never()).wakeup();
        }

	/**
	 * Test method for {@link org.apache.niolex.network.server.SelectorHolder#wakeup()}.
	 */
	@Test
	public void testWakeup() {
		selectorHolder.wakeup();
		selectorHolder.wakeup();
		selectorHolder.wakeup();
		verify(selector).wakeup();
	}

	/**
	 * Test method for {@link org.apache.niolex.network.server.SelectorHolder#changeAllInterestOps()}.
	 */
	@Test
	public void testChangeAllInterestOpsEmpty() {
		selectorHolder.changeAllInterestOps();
	}

	/**
	 * Test method for {@link org.apache.niolex.network.server.SelectorHolder#getSelector()}.
	 */
	@Test
	public void testGetSelector() {
		assertEquals(selectorHolder.getSelector(), selector);
	}

}
