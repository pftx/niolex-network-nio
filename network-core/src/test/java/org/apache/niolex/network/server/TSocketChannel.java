/**
 * TSocketChannel.java
 *
 * Copyright 2013 the original author or authors.
 *
 * We licenses this file to you under the Apache License, version 2.0
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
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;

import org.apache.niolex.network.CoreRunner;

/**
 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2013-11-18
 */
public class TSocketChannel extends SocketChannel {

    byte[] dst = new byte[7];

    /**
     * Constructor
     */
    protected TSocketChannel() {
        super(SelectorProvider.provider());
    }

    /**
     * This is the override of super method.
     * @see java.nio.channels.SocketChannel#connect(java.net.SocketAddress)
     */
    @Override
    public boolean connect(SocketAddress remote) throws IOException {
        return false;
    }

    /**
     * This is the override of super method.
     * @see java.nio.channels.SocketChannel#finishConnect()
     */
    @Override
    public boolean finishConnect() throws IOException {
        return false;
    }

    /**
     * This is the override of super method.
     * @see java.nio.channels.SocketChannel#isConnected()
     */
    @Override
    public boolean isConnected() {
        return false;
    }

    /**
     * This is the override of super method.
     * @see java.nio.channels.SocketChannel#isConnectionPending()
     */
    @Override
    public boolean isConnectionPending() {
        return false;
    }

    /**
     * This is the override of super method.
     * @see java.nio.channels.SocketChannel#read(java.nio.ByteBuffer)
     */
    @Override
    public int read(ByteBuffer dst) throws IOException {
        return 0;
    }

    /**
     * This is the override of super method.
     * @see java.nio.channels.SocketChannel#read(java.nio.ByteBuffer[], int, int)
     */
    @Override
    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException {
        return 0;
    }

    /**
     * This is the override of super method.
     * @see java.nio.channels.SocketChannel#socket()
     */
    @Override
    public Socket socket() {
        Socket so = null;
        try {
            so = new Socket("localhost", CoreRunner.PORT);
        } catch (IOException e) {
        }
        return so;
    }

    /**
     * This is the override of super method.
     * @see java.nio.channels.SocketChannel#write(java.nio.ByteBuffer)
     */
    @Override
    public int write(ByteBuffer src) throws IOException {
        int l = src.remaining();
        src.get(dst, 0, l > dst.length ? dst.length : l);
        return 0;
    }

    /**
     * This is the override of super method.
     * @see java.nio.channels.SocketChannel#write(java.nio.ByteBuffer[], int, int)
     */
    @Override
    public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
        return 0;
    }

    /**
     * This is the override of super method.
     * @see java.nio.channels.spi.AbstractSelectableChannel#implCloseSelectableChannel()
     */
    @Override
    protected void implCloseSelectableChannel() throws IOException {
        throw new IOException("Test ex in Txx");
    }

    /**
     * This is the override of super method.
     * @see java.nio.channels.spi.AbstractSelectableChannel#implConfigureBlocking(boolean)
     */
    @Override
    protected void implConfigureBlocking(boolean block) throws IOException {
    }

}
