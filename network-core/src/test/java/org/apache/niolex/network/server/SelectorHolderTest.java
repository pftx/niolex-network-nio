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
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-11-7
 */
public class SelectorHolderTest {

	@Mock
	private Selector selector;

	@Mock
	private Thread selectorThread;

	@InjectMocks
	private SelectorHolder selectorHolder;

	/**
	 * Test method for {@link org.apache.niolex.network.server.SelectorHolder#changeInterestOps(java.nio.channels.SelectionKey)}.
	 */
	@Test
	public void testChangeInterestOps() {
		SelectionKey selectionKey = mock(SelectionKey.class);
		assertNotEquals(selectorThread, Thread.currentThread());
		selectorHolder.changeInterestOps(selectionKey);
		verify(selectionKey, never()).interestOps(anyInt());
		verify(selector).wakeup();
		selectorHolder.changeAllInterestOps();
		verify(selectionKey).interestOps(anyInt());
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
	public void testChangeAllInterestOps() {
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
