/**
 * MultiNioServer.java
 *
 * Copyright 2011 Niolex, Inc.
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

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Set;

import org.apache.niolex.network.NioServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The MultiNioServer reads and writes Packet in multiple threads.
 * This is specially for multiple CPU(CORE) server.
 * @author Xie, Jiyun
 */
public class MultiNioServer extends NioServer {
	private static final Logger LOG = LoggerFactory.getLogger(MultiNioServer.class);

	// The Thread pool size
    private int threadsNumber = 8;
    private int currentIdx = 0;
    private ThreadGroup tPool;
    private Selector[] selectors;



    public MultiNioServer() {
		super();
		this.threadsNumber = Runtime.getRuntime().availableProcessors();
		if (threadsNumber > 8) {
			// Default to 8, which is the majority CPU number on servers.
			// Setting too many selectors is not good.
			threadsNumber = 8;
		}
	}

	/**
     * Start the server, then the worker pool internally.
     */
	@Override
	public boolean start() {
		boolean started = super.start();
		if (!started) {
			return false;
		}
		tPool = new ThreadGroup("Selectors");
		selectors = new Selector[threadsNumber];
		try {
			for (int i = 0; i < threadsNumber; ++i) {
				Selector s = Selector.open();
				Thread th = new Thread(tPool, new RunnableSelector(s));
				selectors[i] = s;
				th.start();
			}
		} catch (IOException e) {
            LOG.error("Failed to start MultiNioServer.", e);
            return false;
        }
		LOG.info("MultiNioServer started to work with {} threads.", threadsNumber);
		return true;
	}

	/**
	 * Return multiple selector to super class.
	 *
	 * Override super method
	 * @see org.apache.niolex.network.NioServer#getReadSelector()
	 */
	@Override
	protected void registerClient(SocketChannel client) throws IOException {
		int k = currentIdx++;
		if (k >= selectors.length) {
			k = 0;
			currentIdx = 1;
		}
		Selector s = selectors[k];
    	client.register(s, SelectionKey.OP_READ,
    			new ClientHandler(packetHandler, s, client));
	}

	/**
	 * Invoke super stop internally.
	 * Then stop the internal worker pool.
	 */
	@Override
	public void stop() {
		if (!isListening) {
    		return;
    	}
		super.stop();
		tPool.interrupt();
		try {
			for (int i = 0; i < selectors.length; ++i) {
				selectors[i].close();
			}
		} catch (IOException e) {
            LOG.error("Failed to stop MultiNioServer.", e);
        }
	}

	/**
	 * Get the current threads number.
	 * @return
	 */
	public int getThreadsNumber() {
		return threadsNumber;
	}

	/**
	 * Set the internal work pool threads number.
	 * You need to set this before call the start method,
	 * or it will be useless.
	 * @param threadsNumber
	 */
	public void setThreadsNumber(int threadsNumber) {
		this.threadsNumber = threadsNumber;
	}

	/**
	 * Run the wrapped selector endlessly.
	 *
	 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
	 * @version 1.0.0
	 * @Date: 2012-6-11
	 */
	private class RunnableSelector implements Runnable {
		private Selector selector;

		public RunnableSelector(Selector selector) {
			super();
			this.selector = selector;
		}

		/**
		 * Override super method
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			try {
				while (isListening) {
		            // Setting the timeout for accept method. Avoid that this server can not be shut
		            // down when this thread is waiting to accept.
					selector.select(acceptTimeOut);
		            Set<SelectionKey> selectionKeys = selector.selectedKeys();
		            for (SelectionKey selectionKey: selectionKeys) {
		                handleKey(selectionKey);
		            }
		            selectionKeys.clear();
		            Thread.yield();
		        }
			} catch (Exception e) {
	            LOG.error("Error occured while server is listening. The server will now shutdown.", e);
	            stop();
			}
		}

	}
}
