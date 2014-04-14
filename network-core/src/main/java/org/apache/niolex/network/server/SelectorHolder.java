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
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
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
     * A dummy comparator, compare any object by it's hash code.
     *
     * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
     * @version 1.0.0
     * @since 2014-4-14
     */
    private static class DummyComparator implements Comparator<Object> {

        /**
         * This is the override of super method.
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(Object o1, Object o2) {
            return o1.hashCode() - o2.hashCode();
        }

    }

	/**
	 * This set store all the interested selection Keys.
	 */
	private final Set<SelectionKey> selectionKeySet = new ConcurrentSkipListSet<SelectionKey>(new DummyComparator());

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
		    // Add the selection key into the key set, do not need synchronize it,
		    // because we are using current set.
		    selectionKeySet.add(selectionKey);
			wakeup();
		}
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
	protected void changeAllInterestOps() {
		isAwake.set(false);
		Iterator<SelectionKey> iterator = selectionKeySet.iterator();
		while (iterator.hasNext()) {
		    iterator.next().interestOps(INTEREST_OPS);
		    iterator.remove();
		}
	}


	/**
	 * @return  The selector this holder is managing.
	 */
	public Selector getSelector() {
		return selector;
	}
}
