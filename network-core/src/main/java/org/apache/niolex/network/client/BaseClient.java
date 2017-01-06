/**
 * BaseClient.java
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
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

import org.apache.niolex.commons.stream.StreamUtil;
import org.apache.niolex.commons.util.SystemUtil;
import org.apache.niolex.network.Config;
import org.apache.niolex.network.IClient;
import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.event.WriteEventListener;

/**
 * The base implementation of IClient, please extend this class for convenience. We are using blocking IO to operate the
 * socket. Because we can do multi-in/multi-out on one socket, there's no need to create many sockets for one client
 * Application. 2 ~ 4 connections per client was the recommended set.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0, Date: 2012-6-14
 */
public abstract class BaseClient implements IClient {

    /**
     * The byte array used to read packet header.
     */
    private final byte[] readHeader = new byte[Config.PACKET_HEADER_SIZE];

    /**
     * The byte buffer used to write packet header.
     */
    private final ByteBuffer writeHeader = ByteBuffer.allocate(Config.PACKET_HEADER_SIZE);

	/**
     * The socket address this client is going to connect.
     */
    protected InetSocketAddress serverAddress;

    /**
     * The packet handler when packet received.
     */
    protected IPacketHandler packetHandler;

    /**
     * The client socket under control.
     */
    protected Socket socket;

    /**
     * The socket input stream.
     */
    protected InputStream in;

    /**
     * The socket output stream.
     */
    protected OutputStream out;

    /**
     * The status of this client.
     */
    protected volatile boolean isWorking;

    /**
     * Socket connect timeout.
     */
    protected int connectTimeout = Config.SO_CONNECT_TIMEOUT;

    /**
     * Socket send &amp; receive buffer size.
     */
    protected int socketBufferSize = Config.SO_BUFFER_SIZE;

    /**
     * Prepare socket and connect to the specified server address.
     *
     * @return the prepared socket
     * @throws IOException if any I/O error occurs during the operation
     */
    protected Socket prepareSocket() throws IOException {
        // First, we must ensure the old socket is closed, or there will be resource leak.
        safeClose();

        // Then, we are ready to go.
        socket = new Socket();
        socket.setSendBufferSize(socketBufferSize);
        socket.setReceiveBufferSize(socketBufferSize);
        // Specify the specified linger time in seconds.
        socket.setSoLinger(true, 3);
        socket.setSoTimeout(connectTimeout);
        socket.setTcpNoDelay(true);
        socket.connect(serverAddress);
        in = new BufferedInputStream(socket.getInputStream(), socketBufferSize);
        out = new BufferedOutputStream(socket.getOutputStream(), socketBufferSize);
        return socket;
    }

    /**
     * Parse packet from the input stream. We will throw IOException if end of stream reached
     * before we finished one packet.
     *
     * @return the read packet
     * @throws IOException if any I/O error occurs
     * @throws IllegalStateException If packet is too large
     */
    protected PacketData readPacket() throws IOException {
        // Read header.
        int size = StreamUtil.readData(in, readHeader);
        if (size != 8) {
            throw new EOFException("End of stream found, but packet was not finished.");
        }
        PacketData readPacket = new PacketData();
        readPacket.parseHeader(ByteBuffer.wrap(readHeader));

        // Read body.
        size = StreamUtil.readData(in, readPacket.getData());
        if (size != readPacket.getLength()) {
            throw new EOFException("End of stream found, but packet was not finished.");
        }
        return readPacket;
    }

    /**
     * Write the packet into the output stream and flush the stream.
     *
     * @param pd the packet to be written
     * @throws IOException if any I/O error occurs
     */
    protected void writePacket(PacketData pd) throws IOException {
        writeHeader.clear();
        pd.putHeader(writeHeader);
        out.write(writeHeader.array());
        out.write(pd.getData());
        out.flush();
    }

	/**
	 * {@inheritDoc}
	 *
	 * Override super method
	 * @see org.apache.niolex.network.IPacketWriter#getRemoteName()
	 */
    @Override
    public String getRemoteName() {
    	if (socket == null) {
    		return serverAddress.toString() + "-0000";
    	} else {
    		return serverAddress.toString() + "-" + socket.getLocalPort();
    	}
    }

    /**
     * Safely close the socket.
     *
     * @return null if success, exception if error occurred.
     */
    protected Exception safeClose() {
        if (socket != null) {
            StreamUtil.closeStream(in);
            in = null;
            StreamUtil.closeStream(out);
            out = null;
            Exception e = SystemUtil.close(socket);
            socket = null;
            return e;
        }
        return null;
    }

	/**
	 * Override super method
	 * @see org.apache.niolex.network.IPacketWriter#addEventListener(WriteEventListener)
	 */
	@Override
	public void addEventListener(WriteEventListener listener) {
		throw new UnsupportedOperationException("This method is not supported.");
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.IPacketWriter#attachData(String, Object)
	 */
	@Override
	public Object attachData(String key, Object value) {
		throw new UnsupportedOperationException("This method is not supported.");
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.IPacketWriter#getAttached(String)
	 */
	@Override
	public <T> T getAttached(String key) {
		throw new UnsupportedOperationException("This method is not supported.");
	}

    /**
     * {@inheritDoc}
     *
	 * This is the override of super method.
	 * @see org.apache.niolex.network.IClient#setPacketHandler(IPacketHandler)
	 */
    @Override
	public void setPacketHandler(IPacketHandler packetHandler) {
        this.packetHandler = packetHandler;
    }

	/**
	 * {@inheritDoc}
	 *
	 * This is the override of super method.
	 * @see org.apache.niolex.network.IClient#getServerAddress()
	 */
	@Override
	public InetSocketAddress getServerAddress() {
		return serverAddress;
	}

	/**
	 * {@inheritDoc}
	 *
	 * This is the override of super method.
	 * @see org.apache.niolex.network.IClient#setServerAddress(java.net.InetSocketAddress)
	 */
	@Override
	public void setServerAddress(InetSocketAddress serverAddress) {
		this.serverAddress = serverAddress;
	}

	/**
	 * {@inheritDoc}
	 *
	 * This is the override of super method.
	 * @see org.apache.niolex.network.IClient#setServerAddress(java.lang.String)
	 */
	@Override
	public void setServerAddress(String addr) {
	    String[] aa = addr.split(":");
	    this.serverAddress = new InetSocketAddress(aa[0], Integer.parseInt(aa[1]));
	}

	/**
	 * @return the socket connect timeout.
	 */
	public int getConnectTimeout() {
		return connectTimeout;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Override super method
	 * @see org.apache.niolex.network.IClient#setConnectTimeout(int)
	 */
	@Override
	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	/**
     * @return the socket buffer size
     */
    public int getSocketBufferSize() {
        return socketBufferSize;
    }

    /**
     * @param socketBufferSize the socket buffer size to set
     */
    public void setSocketBufferSize(int socketBufferSize) {
        this.socketBufferSize = socketBufferSize;
    }

    /**
	 * {@inheritDoc}
	 *
	 * This is the override of super method.
	 * @see org.apache.niolex.network.IClient#isWorking()
	 */
	@Override
	public boolean isWorking() {
		return isWorking;
	}

}
