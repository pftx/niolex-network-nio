/**
 * IClient.java
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
package org.apache.niolex.network;

import java.io.IOException;

import org.apache.niolex.commons.concurrent.WaitOn;

/**
 * The client interface.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-6-2
 */
public interface IClient {

    /**
     * The connections status of the Client.
     *
     * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
     * @version 1.0.0
     * @since 2012-6-2
     */
    public static enum Status {
        INNITIAL, CONNECTED, RETRYING, CLOSED;
    }

	/**
     * Do real connect action, connect to server.
     * This method will return only after we get connected.
     *
     * @throws IOException if I / O related error occurred
     */
	public void connect() throws IOException;

	/**
     * Asynchronously send the request to server.
     *
     * @param sc the packet to be sent to server
     * @return the wait on object to retrieve result
     * @throws IOException if I / O related error occurred
     */
    public WaitOn<Packet> asyncInvoke(Packet sc) throws IOException;

    /**
     * Send the packet to server, and wait for result in blocking mode.
     *
     * @param sc the packet to be sent to server
     * @return the packet returned from server
     * @throws IOException if I / O related error occurred
     */
    public Packet sendAndReceive(Packet sc) throws IOException;

	/**
	 * Stop this client.
	 */
	public void stop();

	/**
     * Get connection status of this client.
     *
     * @return the status
     */
    public Status getStatus();

	/**
     * Test whether this client is working now.
     *
     * @return true if working
     */
	public boolean isWorking();

	/**
     * Set the underline socket connect timeout
     * This method must be called before connect()
     *
     * @param connectTimeout the socket connect timeout
     */
	public void setConnectTimeout(int connectTimeout);

}