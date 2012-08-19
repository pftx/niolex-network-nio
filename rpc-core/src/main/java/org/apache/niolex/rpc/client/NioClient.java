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

import org.apache.niolex.network.IClient;
import org.apache.niolex.network.Packet;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-8-19
 */
public class NioClient implements IClient {

	/**
	 * This is the override of super method.
	 * @see org.apache.niolex.network.IClient#connect()
	 */
	@Override
	public void connect() throws IOException {
		// TODO Auto-generated method stub

	}

	/**
	 * This is the override of super method.
	 * @see org.apache.niolex.network.IClient#sendAndReceive(org.apache.niolex.network.Packet)
	 */
	@Override
	public Packet sendAndReceive(Packet sc) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * This is the override of super method.
	 * @see org.apache.niolex.network.IClient#stop()
	 */
	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

	/**
	 * This is the override of super method.
	 * @see org.apache.niolex.network.IClient#isWorking()
	 */
	@Override
	public boolean isWorking() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * This is the override of super method.
	 * @see org.apache.niolex.network.IClient#setConnectTimeout(int)
	 */
	@Override
	public void setConnectTimeout(int connectTimeout) {
		// TODO Auto-generated method stub

	}

}
