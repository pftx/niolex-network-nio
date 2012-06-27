/**
 * BlockingWaiter.java
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
package org.apache.niolex.network.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This is a waiting utility for clients to wait for the response and in the mean time hold the thread.
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-20
 */
public class BlockingWaiter<E> {

	/**
	 * The lock to stop threads.
	 */
	private final Lock lock = new ReentrantLock();
	/**
	 * The current waiting map.
	 */
	private final Map<Object, WaitItem> waitMap = new ConcurrentHashMap<Object, WaitItem>();

	/**
	 * Wait for result from server.
	 *
	 * @param key
	 * @param time
	 * @return
	 * @throws InterruptedException
	 */
	public E waitForResult(Object key, long time) throws InterruptedException {
		lock.lock();
		try {
			Condition waitOn = lock.newCondition();
			WaitItem value = new WaitItem(waitOn);
			waitMap.put(key, value);
			waitOn.await(time, TimeUnit.MILLISECONDS);
			return value.result;
		} finally {
			waitMap.remove(key);
			lock.unlock();
		}
	}

	/**
	 * Release the thread waiting on the key with this result.
	 *
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean release(Object key, E value) {
		WaitItem it = waitMap.get(key);
		if (it != null) {
			it.release(value);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * The internal wait item structure.
	 *
	 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
	 * @version 1.0.0
	 * @Date: 2012-6-20
	 */
	private class WaitItem {
		private Condition waitOn;
		private E result;

		/**
		 * The only constructor.
		 * @param waitOn
		 */
		public WaitItem(Condition waitOn) {
			super();
			this.waitOn = waitOn;
		}

		public void release(E result) {
			this.result = result;
			lock.lock();
			try {
				waitOn.signal();
			} finally {
				lock.unlock();
			}
		}

	}
}
