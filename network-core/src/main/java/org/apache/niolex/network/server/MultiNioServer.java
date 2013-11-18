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
import java.util.LinkedList;
import java.util.Set;

import org.apache.niolex.commons.util.SystemUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The MultiNioServer reads and writes Packet in multiple threads.
 * This is specially Fast for multiple CPU(CORE) server.
 *
 * @author Xie, Jiyun
 */
public class MultiNioServer extends NioServer {
	private static final Logger LOG = LoggerFactory.getLogger(MultiNioServer.class);
	/* For auto numbering the threads in this pool. */
    private static int threadInitNumber = 0;

	// The Thread pool size
    private int threadsNumber = 8;
    private int currentIdx = 0;
    private ThreadGroup tPool;
    private RunnableSelector[] selectors;


    /**
     * Create a MultiNioServer with default threads number.
     * User can change the threads number by setter.
     */
    public MultiNioServer() {
		this(Runtime.getRuntime().availableProcessors());
	}

    /**
     * Create a MultiNioServer with your specified threads number.
     *
     * @param threadsNumber the threads number
     */
	public MultiNioServer(int threadsNumber) {
		super();
		if (threadsNumber < 8) {
		    // Default to 8, which is the majority CPU number on servers.
		    // Setting too many selectors is not good.
		    threadsNumber = 8;
		}
		this.threadsNumber = threadsNumber;
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
		selectors = new RunnableSelector[threadsNumber];
		try {
			for (int i = 0; i < threadsNumber; ++i) {
				selectors[i] = new RunnableSelector(tPool, "selector-" + threadInitNumber++);
			}
		} catch (IOException e) {
            LOG.error("Failed to start MultiNioServer.", e);
            return false;
        }
		LOG.info("MultiNioServer started to work with {} threads.", threadsNumber);
		return true;
	}

	/**
	 * Using multiple selectors to handle client sockets.
	 *
	 * Override super method
	 * @see org.apache.niolex.network.server.NioServer#registerClient(SocketChannel)
	 */
	@Override
	protected void registerClient(SocketChannel client) throws IOException {
		currentIdx = (currentIdx + 1) % selectors.length;
		RunnableSelector runSelec = selectors[currentIdx];
    	runSelec.registerClient(client);
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
		try {
			for (int i = 0; i < selectors.length; ++i) {
				selectors[i].close();
			}
		} catch (Exception e) {
            LOG.error("Failed to stop MultiNioServer.", e);
        }
	}

	/**
	 * Get the current threads number.
	 *
	 * @return the current threads number.
	 */
	public int getThreadsNumber() {
		return threadsNumber;
	}

	/**
	 * Set the internal work pool threads number.
	 * You need to set this before call the start method,
	 * or it will throw an exception.
	 *
	 * @param threadsNumber the new threads number
	 */
	public void setThreadsNumber(int threadsNumber) {
	    if (isListening)
	        throw new IllegalStateException("threadsNumber can not be changed if server is running.");
		this.threadsNumber = threadsNumber;
	}

	/**
	 * Run the wrapped selector endlessly in separate threads.
	 *
	 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
	 * @version 1.0.0
	 * @since 2012-6-11
	 */
	private class RunnableSelector implements Runnable {
		private final LinkedList<SocketChannel> clientQueue = new LinkedList<SocketChannel>();
		private final SelectorHolder selectorHolder;
		private final Selector selector;
		private final Thread thread;

		public RunnableSelector(ThreadGroup tPool, String name) throws IOException {
			super();
			this.selector = Selector.open();
			this.thread = new Thread(tPool, this, name);
			this.selectorHolder = new SelectorHolder(thread, selector);
			thread.start();
		}

		/**
		 * Register the client to this selector.
		 *
		 * @param client
		 */
		public synchronized void registerClient(SocketChannel client) {
			clientQueue.add(client);
			selectorHolder.wakeup();
		}

		/**
		 * Close the internal selector and wait for the thread to shutdown.
		 *
		 * @throws IOException
		 * @throws InterruptedException
		 */
		public void close() throws IOException, InterruptedException {
			for (SelectionKey skey : selector.keys()) {
			    SystemUtil.close(skey.channel());
            }
			selector.wakeup();
			thread.join();
			selector.close();
		}

		/**
		 * Override super method
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			try {
				while (isListening) {
					selector.select(acceptTimeOut);
					selectorHolder.changeAllInterestOps();
					Set<SelectionKey> selectedKeys = selector.selectedKeys();
		            for (SelectionKey selectionKey : selectedKeys) {
		                handleKey(selectionKey);
		            }
		            selectedKeys.clear();

		            addClients();
		        }
			} catch (Exception e) {
	            LOG.error("Error occured while server is listening. The server will now shutdown.", e);
	            stop();
			}
		}

		/**
		 * Add all the clients into this selector now.
		 *
		 * @throws IOException
		 */
		public synchronized void addClients() throws IOException {
			// Check the status, if there is any clients need to attach.
		    SocketChannel client = null;
		    while ((client = clientQueue.poll()) != null) {
		        new FastCore(packetHandler, selectorHolder, client);
		    }
		}

	}
}
