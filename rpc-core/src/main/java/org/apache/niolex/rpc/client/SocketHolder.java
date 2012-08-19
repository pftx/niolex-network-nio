/**
 * SocketHolder.java
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
package org.apache.niolex.rpc.client;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Hold all the ready connections.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-8-19
 */
public class SocketHolder {

	private final ConcurrentLinkedQueue<ClientCore> readyQueue = new ConcurrentLinkedQueue<ClientCore>();
	private NioClient nioClient;

	/**
	 * Create a SocketHolder with this nioClient.
	 * The only Constructor
	 * @param nioClient
	 */
	public SocketHolder(NioClient nioClient) {
		super();
		this.nioClient = nioClient;
	}

	/**
	 * Insert this client into the tail of the ready queue.
	 * @param clientCore
	 */
	public void ready(ClientCore clientCore) {
		readyQueue.add(clientCore);
	}

	/**
	 * Retrieves and removes the head of the ready queue, or returns null if the queue is empty.
	 * @return
	 */
	public ClientCore take() {
		ClientCore core;
		while ((core = readyQueue.poll()) != null) {
			if (core.isValid())
				return core;
		}
		return null;
	}

	/**
	 * @param clientCore
	 */
	public void close(ClientCore clientCore) {
		nioClient.closeChannel(clientCore);
	}

}
