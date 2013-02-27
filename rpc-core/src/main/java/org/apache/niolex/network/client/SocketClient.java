/**
 * SocketClient.java
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.niolex.commons.stream.StreamUtil;
import org.apache.niolex.commons.util.SystemUtil;
import org.apache.niolex.network.Config;
import org.apache.niolex.network.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The blocking implementation of IClient. This client can only be used in one
 * thread. If you want to reuse client in multithreading, use
 * {@link NioClient}
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-6-13
 */
public class SocketClient extends BaseClient {
	private static final Logger LOG = LoggerFactory.getLogger(SocketClient.class);

	/**
	 * The socket address this client it going to connect.
	 */
    protected InetSocketAddress serverAddress;

    /**
     * The client socket under control.
     */
    protected Socket socket;

    /**
     * The serial number of this socket client.
     */
    short serialNumber = 1;

    /**
     * The time to sleep between retry.
     */
    private int sleepBetweenRetryTime = Config.RPC_SLEEP_BT_RETRY;

    /**
     * Times to retry get connected.
     */
    private int connectRetryTimes = Config.RPC_CONNECT_RETRY_TIMES;

    /**
     * Socket streams.
     */
    private DataInputStream inS;
    private DataOutputStream outS;

    /**
     * Crate a SocketClient without any server address
     * Call setter to set serverAddress before connect
     */
	public SocketClient() {
		super();
	}

	/**
	 * Create a SocketClient with this Server Address
	 * @param serverAddress
	 */
	public SocketClient(InetSocketAddress serverAddress) {
		super();
		this.serverAddress = serverAddress;
	}

	/**
	 * Create a SocketClient with this Server Address
	 * @param host the server host name
	 * @param port the server port
	 */
	public SocketClient(String host, int port) {
	    super();
	    this.serverAddress = new InetSocketAddress(host, port);
	}

	/**
	 * Generate serial number
	 * @param rc
	 */
	private void serialPacket(Packet rc) {
	    short seri = ++serialNumber;
	    if (seri == Short.MAX_VALUE) {
	        serialNumber = 0;
	    }
	    rc.setSerial(seri);
	}

	/**
	 * Safely close the socket, ignore any exception in this process.
	 */
	private void safeCloseSocket() {
	    if (socket != null) {
	        try {
	            socket.close();
	        } catch(Exception e) {}
	        socket = null;
	    }
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.IClient#connect()
	 */
	@Override
	public void connect() throws IOException {
	    safeCloseSocket();
        socket = new Socket();
        socket.setSoTimeout(connectTimeout);
        socket.setTcpNoDelay(true);
        socket.connect(serverAddress);
        inS = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        outS = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        this.isWorking = true;
        this.connStatus = Status.CONNECTED;
        LOG.info("Client connected to address: {}", serverAddress);
	}

	/**
	 * This is the override of super method.
	 * @see org.apache.niolex.network.IClient#sendAndReceive(org.apache.niolex.network.Packet)
	 */
	@Override
	public synchronized Packet sendAndReceive(Packet sc) throws IOException {
	    try {
    		// 1. Generate serial number
    		serialPacket(sc);
    		// 2. Write packet
    		sc.writeObject(outS);
    		LOG.debug("Packet sent. desc {}, length {}.", sc.descriptor(), sc.getLength());
    		// 3. Read result
    		return handleRead();
	    } catch(IOException e) {
	        doRetry();
	        throw e;
	    }
	}

	/**
	 * Read a packet from server.
	 * @throws IOException
	 */
	public Packet handleRead() throws IOException {
		Packet readPacket = new Packet();
		while (true) {
			readPacket.readObject(inS);
			LOG.debug("Packet received. desc {}, length {}.", readPacket.descriptor(), readPacket.getLength());
			if (readPacket.getCode() == Config.CODE_HEART_BEAT) {
            	// Let's ignore the heart beat packet here.
            	continue;
            }
			return readPacket;
		}
	}


    /**
     * Handle connection lose, Try to reconnect.
     */
    public void doRetry() {
        if (this.connStatus == Status.CLOSED) {
            return;
        }
        // We will retry to connect in this method.
        this.connStatus = Status.RETRYING;
        if (!retryConnect()) {
            LOG.error("Exception occured when try to re-connect to server. Client will stop.");
            // Shutdown this Client.
            this.stop();
        }
    }

    /**
     * Try to re-connect to server, iterate connectRetryTimes
     * @return true if connected to server.
     */
    private boolean retryConnect() {
        for (int i = 0; i < connectRetryTimes; ++i) {
            SystemUtil.sleep(sleepBetweenRetryTime);
            LOG.info("RPC Client try to reconnect to server round {} ...", i);
            try {
                this.connect();
                this.connStatus = Status.CONNECTED;
                return true;
            } catch (IOException e) {
                // Not connected.
                LOG.info("Try to re-connect to server failed. {}", e.toString());
            }
        }
        return false;
    }

    /**
	 * This is the override of super method.
	 * @see org.apache.niolex.network.IClient#stop()
	 */
    @Override
	public void stop() {
        this.isWorking = false;
        this.connStatus = Status.CLOSED;
        try {
    		StreamUtil.closeStream(inS);
    		StreamUtil.closeStream(outS);
    		safeCloseSocket();
    		LOG.info("Socket client stoped.");
    	} catch(Exception e) {
    		LOG.error("Error occured when stop the socket client.", e);
    	}
    }

	/**
	 * Set the server Internet address this client want to connect
	 * This method must be called before connect(), or the client
	 * will still connect to the old address.
	 *
	 * @param serverAddress
	 */
	public void setServerAddress(InetSocketAddress serverAddress) {
		this.serverAddress = serverAddress;
	}

	/**
	 * Get the current Internet address
	 * @return the current Internet address
	 */
	public InetSocketAddress getServerAddress() {
		return serverAddress;
	}

    /**
     * Set the sleep time between retry, default to 1 second.
     * @param sleepBetweenRetryTime
     */
    public void setSleepBetweenRetryTime(int sleepBetweenRetryTime) {
        this.sleepBetweenRetryTime = sleepBetweenRetryTime;
    }

    /**
     * Set the number of times to retry we connection lost from server.
     * @param connectRetryTimes
     */
    public void setConnectRetryTimes(int connectRetryTimes) {
        this.connectRetryTimes = connectRetryTimes;
    }

}
