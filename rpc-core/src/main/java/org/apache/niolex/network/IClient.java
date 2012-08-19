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


/**
 * The client interface.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-2
 */
public interface IClient {

	/**
	 * Do real connect action, connect to server.
	 * This method will return immediately.
	 *
	 * @throws IOException
	 */
	public void connect() throws IOException;

    /**
     * Handle the Packet, and return result.
     *
     * @param sc The Packet need to be send
     * @return the Packet returned from server
     *
     * @throws IOException
     */
    public Packet sendAndReceive(Packet sc) throws IOException;

	/**
	 * Stop this client.
	 */
	public void stop();

	/**
	 * Test whether this client is working now
	 * @return
	 */
	public boolean isWorking();

	/**
	 * Set the underline socket connect timeout
	 * This method must be called before connect()
	 *
	 * @param connectTimeout
	 */
	public void setConnectTimeout(int connectTimeout);

}