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
package org.apache.niolex.rpc.core;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class hold the selector and selector thread.
 * It will decide how to attach write operation to selector.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-8-17
 */
public class SelectorHolder {

	/**
	 * This set store all the interested selection Keys.
	 */
	private final Set<SelectionKey> selectionKeySet = new HashSet<SelectionKey>();

	private final AtomicBoolean isAwake = new AtomicBoolean(false);

	/**
	 * This thread is the thread running the selector.
	 */
	private Thread thread;

	/**
	 * This is the selector being held.
	 */
	private Selector selector;


	/**
	 * The Constructor, must set selector thread and selector itself.
	 * @param selectorThread
	 * @param selector
	 */
	public SelectorHolder(Thread selectorThread, Selector selector) {
		super();
		this.thread = selectorThread;
		this.selector = selector;
	}

	/**
	 * #RpcCore & #NioConnCore use this method to register the wish to change interest operations.
	 * This method will decide make the change now or wait for the next wakeup.
	 *
	 * @param selectionKey
	 */
	public void changeInterestOps(SelectionKey selectionKey, int ops) {
		if (thread == Thread.currentThread()) {
			selectionKey.interestOps(ops);
		} else {
			synchronized (selectionKeySet) {
				selectionKeySet.add(selectionKey);
			}
			wakeup();
		}
	}

	/**
	 * Use this method to wake up the selector managed by this holder.
	 * Holder will eliminate unnecessary multiple wakeups.
	 */
	public void wakeup() {
		if (isAwake.compareAndSet(false, true)) {
			selector.wakeup();
		}
	}

	/**
	 * Server use this method of change all the interest operations.
	 */
	public void changeAllInterestOps() {
		isAwake.set(false);
		if (selectionKeySet.isEmpty()) {
			return;
		}
		synchronized (selectionKeySet) {
			for (SelectionKey selectionKey : selectionKeySet) {
				selectionKey.interestOps(SelectionKey.OP_WRITE);
			}
			selectionKeySet.clear();
		}
	}


	/**
	 * @return  The selector this holder is managing.
	 */
	public Selector getSelector() {
		return selector;
	}
}
