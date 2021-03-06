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
package org.apache.niolex.rpc.client.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.niolex.commons.concurrent.Blocker;
import org.apache.niolex.commons.concurrent.BlockerException;
import org.apache.niolex.commons.concurrent.WaitOn;
import org.apache.niolex.commons.util.SystemUtil;
import org.apache.niolex.network.Config;
import org.apache.niolex.network.Packet;
import org.apache.niolex.rpc.RpcException;
import org.apache.niolex.rpc.client.BaseClient;
import org.apache.niolex.rpc.core.SelectorHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The non blocking client.
 * This client maintains a number of connections by the ConnectionHolder class.
 * So it's concurrent enabled.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-8-19
 */
public class NioClient extends BaseClient implements Runnable {
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
     * Socket connect timeout. There is no connect timeout in Non-Blocking,
     * so we use this timeout for take connection from {@link ConnectionHolder}
     */
    protected int connectTimeout = Config.SO_CONNECT_TIMEOUT;

    /**
     * The status of this client.
     */
    protected boolean isWorking;

	/**
	 * The client selector.
	 */
    private final Selector selector;

	/**
	 * The selector thread.
	 */
    private final Thread selectorThread;

	/**
	 * The selector holder.
	 */
    private final SelectorHolder selectorHolder;

	/**
	 * The NIO socket channel container hold all the socket channels.
	 */
	private final ConnectionHolder connManager;

	/**
	 * The configured server address array.
	 */
	private SocketAddress[] serverAddresses;

	/**
	 * Current address index.
	 */
	private int addressIdx = 0;

	/**
     * The only Constructor, create an empty instance.
     * <p>
     * Please call {@link #setServerAddress(String)} and then
     * call {@link #connect()} to make this instance ready.
     * <p>
     * 
     * @throws IOException if I / O related error occurred
     */
	public NioClient() throws IOException {
		super();
		this.selector = Selector.open();
		this.selectorThread = new Thread(this);
        this.selectorHolder = new SelectorHolder(selectorThread, selector);
        this.connManager = new ConnectionHolder(this);
	}

    /**
     * This is the override of super method.
     * 
     * @see org.apache.niolex.network.IClient#connect()
     */
    @Override
    public void connect() throws IOException {
        this.isWorking = true;
        connManager.needReady(connectionNumber / 2 + 1);
        addClientChannels();
        selectorThread.start();
        try {
            connManager.waitReady();
        } catch (InterruptedException e) {
            LOG.warn("The nio client been interrupted when wait for connections to be ready.");
        }
        this.connStatus = Status.CONNECTED;
    }

    /**
     * Add more client channels if the manager is not full.
     */
    public void addClientChannels() {
        while (validCnt.get() < connectionNumber) {
            int k = validCnt.incrementAndGet();
            if (k <= connectionNumber) {
                addNewClientChannel();
            } else {
                validCnt.decrementAndGet();
            }
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
            // Connection attached to channel, channel registered to selector.
            new ConnectionCore(selectorHolder, ch, connManager);
        } catch (IOException e) {
            validCnt.decrementAndGet();
            LOG.error("Failed to create channel to address: {}", remote, e);
        }
    }

	/**
	 * Run the selector, the main method.
	 * This is the override of super method.
	 *
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			while (isWorking) {
				selector.select();
				// Make all the interest changes happen now.
				selectorHolder.changeAllInterestOps();
				Set<SelectionKey> selectedKeys = selector.selectedKeys();
	            for (SelectionKey selectionKey : selectedKeys) {
	                handleKey(selectionKey);
	            }
	            selectedKeys.clear();
	            // Add more connections to ensure there are always many connections available.
	            addClientChannels();
	        }
		} catch (Exception e) {
            LOG.error("Error occured while nio client is running. The client will now shutdown.", e);
            stop();
		}
	}

	/**
     * Process all the IO requests.
     * Handle connect, read, write. Please do not override this method.
     * 
     * @param selectionKey the selection key
     * @throws IOException if I / O related error occurred
     */
    protected void handleKey(SelectionKey selectionKey) throws IOException {
        try {
        	ConnectionCore cli = (ConnectionCore) selectionKey.attachment();
            if (selectionKey.isConnectable()) {
            	cli.handleConnect();
            } else if (selectionKey.isReadable()) {
            	if (cli.handleRead()) {
            		// Read packet finished, we need to invoke packet handler
            		cli.readFinished(blocker);
            	}
            } else if (selectionKey.isWritable()) {
            	cli.handleWrite();
            }
        } catch (Exception e) {
        	if (e instanceof CancelledKeyException || e instanceof ClosedChannelException) {
        		return;
        	}
            LOG.info("Failed to handle socket event: {}", e.toString());
        }
    }

	/**
     * A channel is invalid and closed here.
     * 
     * @param clientCore the specified client connection
     */
	public void closeChannel(ConnectionCore clientCore) {
		validCnt.decrementAndGet();
		BlockerException ex = new BlockerException("Connection closed.");
		blocker.release(clientCore, ex);
	}

	/**
     * Asynchronously invoke of remote call.
     *
     * @param sc the packet to be sent to server
     * @return the wait on object
     */
	@Override
	public WaitOn<Packet> asyncInvoke(Packet sc) {
		sc.setSerial((short) 1);
		ConnectionCore cli = connManager.take(connectTimeout);
		if (cli != null) {
			WaitOn<Packet> on = blocker.init(cli);
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
		} catch (RpcException e) {
		    throw e;
		} catch (InterruptedException e) {
		    throw new RpcException("Rpc timeout.", RpcException.Type.TIMEOUT, e);
		} catch (Exception e) {
            throw new RpcException("Error occurred when get result.", RpcException.Type.ERROR_INVOKE, e);
		}
	}

	/**
	 * This is the override of super method.
	 * @see org.apache.niolex.network.IClient#stop()
	 */
	@Override
	public void stop() {
		this.isWorking = false;
		this.connStatus = Status.CLOSED;
		for (SelectionKey skey : new HashSet<SelectionKey>(selector.keys())) {
            SystemUtil.close(skey.channel());
        }
		selector.wakeup();
		try {
			selectorThread.join();
			selector.close();
    		LOG.info("NioClient stoped.");
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
		this.connectTimeout = connectTimeout;
	}

	/**
     * Connection number is the total number of connections this client will create and manage.
     * 
     * @param connectionNumber the total number of connections
     */
	public void setConnectionNumber(int connectionNumber) {
		this.connectionNumber = connectionNumber;
	}

	/**
     * The rpc invoke timeout in millisecond.
     * 
     * @param rpcHandleTimeout the rpc invoke timeout
     */
	public void setRpcHandleTimeout(int rpcHandleTimeout) {
		this.rpcHandleTimeout = rpcHandleTimeout;
	}

	/**
     * Set the remote Rpc server address, with the format of ip:port,ip:port
     * You can specify multiple IP addresses.
     * <br>
     * And Parse address from string into SocketAddress format.
     * 
     * @param str the RPC server addresses
     */
	public void setServerAddress(String str) {
		String[] ss = str.split(" *[,;] *");
		SocketAddress[] ass = new SocketAddress[ss.length];
		for (int i = 0; i < ss.length; ++i) {
			String a = ss[i];
			String[] aa = a.split(":");
			SocketAddress s = new InetSocketAddress(aa[0], Integer.parseInt(aa[1]));
			ass[i] = s;
		}
		serverAddresses = ass;
		if (connectionNumber == 0) {
            connectionNumber = ass.length * 2;
		}
	}

}
