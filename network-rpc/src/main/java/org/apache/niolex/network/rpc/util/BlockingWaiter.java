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
package org.apache.niolex.network.rpc.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.niolex.commons.util.Pair;

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
	private final ConcurrentHashMap<Object, WaitOn> waitMap = new ConcurrentHashMap<Object, WaitOn>();

	/**
	 * Initialize an internal wait structure, and return it.
	 * When use this method, the application need to make sure only one thread waiting for one key at any time.
	 * Or the old wait on object will be replaced and that thread can not get result at all.
	 *
	 * @see #init(Object)
	 *
	 * @param key
	 * @return The newly created WaitOn
	 */
	public WaitOn initWait(Object key) {
		lock.lock();
		try {
			Condition waitOn = lock.newCondition();
			WaitOn value = new WaitOn(key, waitOn);
			waitMap.put(key, value);
			return value;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Initialize an internal wait structure, and return it.
	 * If there is already another one waiting on the same key, that old structure will be returned.
	 * Use this method if for anyone want to wait on the same key concurrently.
	 *
	 * @param key
	 * @return Pair.a true if the wait on object is newly created. Pair.b the wait on object.
	 */
	public Pair<Boolean, WaitOn> init(Object key) {
		lock.lock();
		try {
			Condition waitOn = lock.newCondition();
			WaitOn value = new WaitOn(key, waitOn);
			Pair<Boolean, WaitOn> p = new Pair<Boolean, WaitOn>();
			p.b = waitMap.putIfAbsent(key, value);
			if (p.b == null) {
				p.a = Boolean.TRUE;
				p.b = value;
			} else {
				p.a = Boolean.FALSE;
			}
			return p;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * A short method for initWait(key).waitForResult(time).
	 * Just for some one do not care about initialization.
	 *
	 * @param key
	 * @param time
	 * @return The result
	 * @throws InterruptedException
	 */
	public E waitForResult(Object key, long time) throws InterruptedException {
		return initWait(key).waitForResult(time);
	}

	/**
	 * Release the thread waiting on the key with this result.
	 *
	 * @param key
	 * @param value
	 * @return true if success to release, false if no thread waiting on it.
	 */
	public boolean release(Object key, E value) {
		WaitOn it = waitMap.get(key);
		if (it != null) {
			it.release(value);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Release the thread waiting on the key with this exception.
	 *
	 * @param key
	 * @param value
	 * @return true if success to release, false if no thread waiting on it.
	 */
	public boolean release(Object key, RuntimeException value) {
		WaitOn it = waitMap.get(key);
		if (it != null) {
			it.release(value);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Use this class to wait for result.
	 * The internal wait structure.
	 *
	 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
	 * @version 1.0.0
	 * @Date: 2012-6-29
	 */
	public class WaitOn {

		/**
		 * The internal managed wait item.
		 */
		private final Object key;
		private final Condition waitOn;
		private E result;
		private RuntimeException e;

		/**
		 * The only constructor.
		 * @param waitOn
		 */
		public WaitOn(Object key, Condition waitOn) {
			super();
			this.key = key;
			this.waitOn = waitOn;
		}

		/**
		 * Wait for result from server.
		 * If result is not ready after the given time, will return null.
		 * If there is any exception thrown from the release side, that exception will be thrown.
		 *
		 * @param time
		 * @return
		 * @throws InterruptedException
		 */
		public E waitForResult(long time) throws InterruptedException {
			lock.lock();
			try {
				if (result != null)
					return result;
				if (e != null) {
					// Release with exception.
					throw e;
				}
				// Not ready yet, let's wait.
				waitOn.await(time, TimeUnit.MILLISECONDS);
				if (e != null) {
					// Release with exception.
					throw e;
				}
				// Just return, if not ready, will return null.
				return result;
			} finally {
				// Anyway, we will remove the key from map, to prevent
				// memory leak.
				waitMap.remove(key);
				lock.unlock();
			}
		}

		public void release(E result) {
			this.result = result;
			lock.lock();
			try {
				waitOn.signalAll();
			} finally {
				lock.unlock();
			}
		}

		public void release(RuntimeException result) {
			this.e = result;
			lock.lock();
			try {
				waitOn.signalAll();
			} finally {
				lock.unlock();
			}
		}

	}

}
