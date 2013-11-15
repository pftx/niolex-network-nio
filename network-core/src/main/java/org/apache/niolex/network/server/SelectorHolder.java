/**
 * SelectorHolder.java
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

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class hold the selector and selector thread.
 * It will decide how to attach write operation to selector.
 * <br>
 * As the JDK indicates, it's only safe to change the interest operations
 * in the selectors thread. That's why we need this class.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-8-17
 */
public class SelectorHolder {

    /**
     * The interest operations.
     */
    private static final int INTEREST_OPS = SelectionKey.OP_READ | SelectionKey.OP_WRITE;

	/**
	 * This set store all the interested selection Keys.
	 */
	private final Set<SelectionKey> selectionKeySet = new HashSet<SelectionKey>();

	/**
	 * This flag indicates the status of selector, so we will not wake up duplicated.
	 */
	private final AtomicBoolean isAwake = new AtomicBoolean(false);

	/**
	 * This thread is the thread running the selector.
	 */
	private Thread selectorThread;

	/**
	 * This is the selector being held.
	 */
	private Selector selector;


	/**
	 * The Constructor, must set selector thread and selector itself.
	 *
	 * @param selectorThread
	 * @param selector
	 */
	public SelectorHolder(Thread selectorThread, Selector selector) {
		super();
		this.selectorThread = selectorThread;
		this.selector = selector;
	}

	/**
	 * {@link FastCore} use this method to register the wish of changing interest operations.
	 * We will change the interest operations into both read and write.
	 *
	 * If the change is in the selector thread, we change it directly.
	 * Otherwise, we save it into the set and wakeup the selector to register the change.
	 *
	 * @param selectionKey
	 */
	public void changeInterestOps(SelectionKey selectionKey) {
		if (selectorThread == Thread.currentThread()) {
			selectionKey.interestOps(INTEREST_OPS);
		} else {
		    addSelectionKey(selectionKey);
			wakeup();
		}
	}

	/**
	 * Add the selection key into the key set in a synchronized method.
	 *
	 * @param selectionKey
	 */
	private synchronized void addSelectionKey(SelectionKey selectionKey) {
	    selectionKeySet.add(selectionKey);
	}

	/**
	 * Use this method to wake up the selector managed by this holder.
	 * Holder will eliminate unnecessary multiple wakeups.
	 */
	protected void wakeup() {
		if (isAwake.compareAndSet(false, true)) {
			selector.wakeup();
		}
	}

	/**
	 * Server use this method to change all the interest operations on hold.
	 * <br><b>
	 * This method can only be invoked in the selector's thread.</b>
	 */
	protected synchronized void changeAllInterestOps() {
		isAwake.set(false);
		if (selectionKeySet.isEmpty()) {
			return;
		}
		for (SelectionKey selectionKey : selectionKeySet) {
			selectionKey.interestOps(INTEREST_OPS);
		}
		selectionKeySet.clear();
	}


	/**
	 * @return  The selector this holder is managing.
	 */
	public Selector getSelector() {
		return selector;
	}
}
