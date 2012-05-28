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

import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

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

	// The Thread pool size, default to 8, which is the majority CPU number on servers.
    private int threadsNumber = 8;
    private ExecutorService tPool;

    /**
     * Start the server, then the worker pool internally.
     */
	@Override
	public boolean start() {
		boolean started = super.start();
		if (!started) {
			return false;
		}
		tPool = Executors.newFixedThreadPool(threadsNumber);
		LOG.info("MultiNioServer started to work with {} threads.", threadsNumber);
		return true;
	}

	/**
	 * Use the ConcurrentClientHandler instead of super common ClientHandler
	 */
	@Override
	protected ClientHandler getClientHandler(SocketChannel client) {
		return new ConcurrentClientHandler(client);
	}

	@Override
	protected void handleRead(ClientHandler clientHandler) {
		tPool.execute(new Read(clientHandler));
	}


	@Override
	protected void handleWrite(ClientHandler clientHandler) {
		tPool.execute(new Write(clientHandler));
	}

	/**
	 * Stop the internal worker pool.
	 */
	@Override
	public void stop() {
		super.stop();
		tPool.shutdown();
	}

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

	protected void superRead(ClientHandler clientHandler) {
		super.handleRead(clientHandler);
	}

	protected class Read implements Runnable {
		private ClientHandler clientHandler;

		public Read(ClientHandler clientHandler) {
			super();
			this.clientHandler = clientHandler;
		}

		@Override
		public void run() {
			superRead(clientHandler);
		}

	}

	protected void superWrite(ClientHandler clientHandler) {
		super.handleWrite(clientHandler);
	}

	protected class Write implements Runnable {
		private ClientHandler clientHandler;

		public Write(ClientHandler clientHandler) {
			super();
			this.clientHandler = clientHandler;
		}

		@Override
		public void run() {
			superWrite(clientHandler);
		}

	}


	/**
	 * When in concurrent environment, Socket Channel need to be handled by
	 * one thread at a time, so we need some kind of locking and synchronization.
	 *
	 */
	public class ConcurrentClientHandler extends ClientHandler {
		AtomicBoolean isRead = new AtomicBoolean(false);
		AtomicBoolean isWrite = new AtomicBoolean(false);

		public ConcurrentClientHandler(SocketChannel sc) {
			super(sc);
		}

		@Override
		public boolean handleRead() {
			if (!isRead.getAndSet(true)) {
				boolean r = super.handleRead();
				isRead.set(false);
				return r;
			}
			return false;
		}

		@Override
		public boolean handleWrite() {
			if (!isWrite.getAndSet(true)) {
				boolean r = super.handleWrite();
				isWrite.set(false);
				return r;
			}
			return false;
		}

	}
}
