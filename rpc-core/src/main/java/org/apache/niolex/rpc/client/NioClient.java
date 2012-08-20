/**
 * NioClient.java
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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.niolex.commons.concurrent.Blocker;
import org.apache.niolex.commons.concurrent.WaitOn;
import org.apache.niolex.network.Config;
import org.apache.niolex.network.IClient;
import org.apache.niolex.network.Packet;
import org.apache.niolex.rpc.RpcException;
import org.apache.niolex.rpc.core.SelectorHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The non blocking client.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-8-19
 */
public class NioClient implements IClient, Runnable {
	private static final Logger LOG = LoggerFactory.getLogger(NioClient.class);

	/**
	 * The current valid connections count.
	 */
	private final AtomicInteger validCnt = new AtomicInteger(0);

	/**
	 * The blocker to wait for result.
	 */
	private final Blocker<Packet> blocker = new Blocker<Packet>();

	/**
	 * The number of socket connections to maintain.
	 */
	private int connectionNumber;

	/**
	 * The rpc handle timeout in milliseconds.
	 */
	private int rpcHandleTimeout = Config.RPC_HANDLE_TIMEOUT;

    /**
     * The status of this client.
     */
    protected boolean isWorking;

	/**
	 * The client selector.
	 */
	private Selector selector;

	/**
	 * The selector thread.
	 */
	private Thread thread;

	/**
	 * The selector holder.
	 */
	private SelectorHolder selectorHolder;

	/**
	 * The socket container hold all the sockets.
	 */
	private final SocketHolder socketHolder;

	/**
	 * The configured server address array.
	 */
	private SocketAddress[] serverAddresses;

	/**
	 * Current address index.
	 */
	private int addressIdx = 0;

	/**
	 * Constructor
	 * @throws IOException
	 */
	public NioClient() throws IOException {
		super();
		this.selector = Selector.open();
		this.thread = new Thread(this);
		selectorHolder = new SelectorHolder(thread, selector);
		socketHolder = new SocketHolder(this);
	}

	/**
	 * Run the selector.
	 * This is the override of super method.
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			while (isWorking) {
				selector.select();
				selectorHolder.changeAllInterestOps();
				Set<SelectionKey> selectedKeys = selector.selectedKeys();
	            for (SelectionKey selectionKey : selectedKeys) {
	                handleKey(selectionKey);
	            }
	            selectedKeys.clear();
	            addClientChannels();
	        }
		} catch (Exception e) {
            LOG.error("Error occured while nio client is listening. The client will now shutdown.", e);
            stop();
		}
	}

	/**
     * Process all the IO requests.
     * Handle connect, read, write. Please do not override this method.
     */
    protected void handleKey(SelectionKey selectionKey) throws IOException {
        try {
        	ClientCore cli = (ClientCore) selectionKey.attachment();
            if (selectionKey.isConnectable()) {
            	cli.handleConnect();
            } else if (selectionKey.isReadable()) {
            	if (cli.handleRead()) {
            		// Read packet finished, we need to invoke packet handler
            		blocker.release(cli, cli.readFinished());
            	}
            } else if (selectionKey.isWritable()) {
            	cli.handleWrite();
            }
        } catch (Exception e) {
        	if (e instanceof CancelledKeyException || e instanceof ClosedChannelException) {
        		return;
        	}
            LOG.info("Failed to handle socket: {}", e.toString());
        }
    }

	/**
	 * This is the override of super method.
	 * @see org.apache.niolex.network.IClient#connect()
	 */
	@Override
	public void connect() throws IOException {
		this.isWorking = true;
		addClientChannels();
		socketHolder.needReady(connectionNumber / 2);
		thread.start();
		try {
			socketHolder.waitReady();
		} catch (InterruptedException e) {}
	}

	/**
	 * Add more client channels if not full.
	 */
	public void addClientChannels() {
		while (validCnt.get() < connectionNumber) {
			int k = validCnt.incrementAndGet();
			if (k <= connectionNumber)
				addNewClientChannel();
		}
	}

	/**
	 * Add a new client channel.
	 */
	public void addNewClientChannel() {
		SocketAddress remote = serverAddresses[addressIdx];
		addressIdx = (addressIdx + 1) % serverAddresses.length;
		try {
			SocketChannel ch = SocketChannel.open();
			ch.configureBlocking(false);
			ch.socket().setTcpNoDelay(true);
			ch.socket().setSoLinger(false, 0);
			ch.connect(remote);
			new ClientCore(selectorHolder, ch, socketHolder);
		} catch (IOException e) {
			validCnt.decrementAndGet();
			LOG.error("Failed to create channel to address: {}", remote, e);
		}
	}

	/**
	 * A channel is invalid and closed here.
	 * @param clientCore
	 */
	public void closeChannel(ClientCore clientCore) {
		validCnt.decrementAndGet();
		Exception ex = new RpcException("Client closed.", RpcException.Type.CONNECTION_LOST, null);
		blocker.release(clientCore, ex);
	}

	/**
	 * Async invoke of remote call.
	 *
	 * @param sc
	 * @return
	 */
	public WaitOn<Packet> asyncInvoke(Packet sc) {
		sc.setSerial((short) 1);
		ClientCore cli = socketHolder.take();
		if (cli != null) {
			WaitOn<Packet> on = blocker.initWait(cli);
			cli.prepareWrite(sc);
			return on;
		} else {
			throw new RpcException("No connection is ready.", RpcException.Type.CLIENT_BUSY, null);
		}
	}

	/**
	 * This is the override of super method.
	 * @see org.apache.niolex.network.IClient#sendAndReceive(org.apache.niolex.network.Packet)
	 */
	@Override
	public Packet sendAndReceive(Packet sc) {
		WaitOn<Packet> on = asyncInvoke(sc);
		try {
			return on.waitForResult(rpcHandleTimeout);
		} catch (Exception e) {
			if (e instanceof InterruptedException) {
				throw new RpcException("Rpc timeout.", RpcException.Type.TIMEOUT, e);
			}
			throw new RpcException("Error get result.", RpcException.Type.ERROR_INVOKE, e);
		}
	}

	/**
	 * This is the override of super method.
	 * @see org.apache.niolex.network.IClient#stop()
	 */
	@Override
	public void stop() {
		this.isWorking = false;
		for (SelectionKey skey : selector.keys()) {
        	try {
        		skey.channel().close();
        	} catch (Exception e) {}
        }
		selector.wakeup();
		try {
			thread.join();
			selector.close();
    		LOG.info("Client stoped.");
    	} catch(Exception e) {
    		LOG.error("Error occured when stop the nio client.", e);
    	}
	}

	/**
	 * This is the override of super method.
	 * @see org.apache.niolex.network.IClient#isWorking()
	 */
	@Override
	public boolean isWorking() {
		return isWorking;
	}

	/**
	 * This is the override of super method.
	 * @see org.apache.niolex.network.IClient#setConnectTimeout(int)
	 */
	@Override
	public void setConnectTimeout(int connectTimeout) {
		// Connection timeout is use less for nonblocking.
	}

	/**
	 * Connection number is the total number of connections this client will create and manage.
	 * @param connectionNumber
	 */
	public void setConnectionNumber(int connectionNumber) {
		this.connectionNumber = connectionNumber;
	}

	/**
	 * The rpc invoke timeout in millisecond.
	 * @param rpcHandleTimeout
	 */
	public void setRpcHandleTimeout(int rpcHandleTimeout) {
		this.rpcHandleTimeout = rpcHandleTimeout;
	}

	/**
	 * Set the remote Rpc server address, with the format of ip:port,ip:port
	 * You can specify multiple ip addresses.
	 *
	 * And Parse address from string into SocketAddress format.
	 * @param serverAddress
	 */
	public void setServerAddress(String str) {
		String[] ss = str.split(" *, *");
		SocketAddress[] ass = new SocketAddress[ss.length];
		for (int i = 0; i < ss.length; ++i) {
			String a = ss[i];
			String[] aa = a.split(":");
			SocketAddress s = new InetSocketAddress(aa[0], Integer.parseInt(aa[1]));
			ass[i] = s;
		}
		serverAddresses = ass;
	}

}
