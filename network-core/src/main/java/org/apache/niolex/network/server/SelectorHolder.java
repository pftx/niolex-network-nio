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

/**
 * This class hold the selector and selector thread.
 * It will decide how to attach write operation to selector.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-8-17
 */
public abstract class SelectorHolder {

	/**
	 * This set store all the interested selection Keys.
	 */
	private Set<SelectionKey> selectionKeySet = new HashSet<SelectionKey>();

	/**
	 * This thread is the thread running the selector.
	 */
	private Thread selectorThread;

	/**
	 * Return the selector this holder is managing.
	 * @return
	 */
	public abstract Selector getSelector();

	/**
	 * #FastCore use this method to register the wish to change interest operations.
	 *
	 * @param selectionKey
	 */
	public void changeInterestOps(SelectionKey selectionKey) {
		if (selectorThread == Thread.currentThread()) {
			selectionKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
		} else {
			synchronized (selectionKeySet) {
				selectionKeySet.add(selectionKey);
			}
			getSelector().wakeup();
		}
	}

	/**
	 * Server use this method of change all the interest operations.
	 */
	public void changeAllInterestOps() {
		if (selectionKeySet.isEmpty()) {
			return;
		}
		synchronized (selectionKeySet) {
			for (SelectionKey selectionKey : selectionKeySet) {
				selectionKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
			}
			selectionKeySet.clear();
		}
	}

	/**
	 * Set the thread the current selector is running with.
	 * @param selectorThread
	 */
	public void setSelectorThread(Thread selectorThread) {
		this.selectorThread = selectorThread;
	}

}
