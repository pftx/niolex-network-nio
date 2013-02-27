/**
 * ConnectionManager.java
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
package org.apache.niolex.network.client;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.niolex.rpc.client.NioClient;

/**
 * Hold all the ready connections. Every connection is represented as
 * a client core instance.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-8-19
 */
public class ConnectionManager {

	private final LinkedBlockingQueue<ConnectionCore> readyQueue = new LinkedBlockingQueue<ConnectionCore>();
	private NioClient nioClient;
	private CountDownLatch latch;

	/**
	 * Create a ConnectionManager with this nioClient.
	 * The only Constructor
	 * @param nioClient
	 */
	public ConnectionManager(NioClient nioClient) {
		super();
		this.nioClient = nioClient;
	}

	/**
	 * Set the number of connections need to be ready.
	 * @param k
	 */
	public void needReady(int k) {
		latch = new CountDownLatch(k);
	}

	/**
	 * Wait for connections to be ready.
	 * @throws InterruptedException
	 */
	public void waitReady() throws InterruptedException {
		latch.await();
	}

	/**
	 * Insert this client into the tail of the ready queue.
	 * @param clientCore
	 */
	public void ready(ConnectionCore clientCore) {
		latch.countDown();
		readyQueue.offer(clientCore);
	}

	/**
	 * Retrieves and removes the head of the ready queue, or returns null if the queue is empty.
	 *
	 * @param connectTimeout the timeout to take item from queue.
	 * @return an instance of ConnectionCore, null if client is busy.
	 */
	public ConnectionCore take(int connectTimeout) {
		ConnectionCore core;
		while ((core = takeOne(connectTimeout)) != null) {
			if (core.isValid())
				return core;
			else
			    close(core);
		}
		return null;
	}

	/**
	 * Take one ConnectionCore from the ready queue, will return null if can not take out any
	 * element at the given timeout.
	 * We will not check the status of this instance, so it maybe already broken.
	 *
	 * @param connectTimeout the timeout to take item from queue.
	 * @return an instance of ConnectionCore, null if timeout.
	 */
	protected ConnectionCore takeOne(int connectTimeout) {
		try {
			return readyQueue.poll(connectTimeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			return null;
		}
	}

	/**
	 * Notify all the threads waiting for results from this connection.
	 *
	 * @param clientCore
	 */
	public void close(ConnectionCore clientCore) {
		nioClient.closeChannel(clientCore);
	}

}
