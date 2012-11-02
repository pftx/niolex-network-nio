/**
 * IServer.java
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

import org.apache.niolex.rpc.core.Invoker;

/**
 * The interface of server.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-8
 */
public interface IServer {

	/**
	 * Start the Server, bind to the Port.
	 * This method need to return after this server is started.
	 * Server need to start threads internally to run.
	 */
	public boolean start();

	/**
	 * Stop this server.
	 * After stop, the internal threads need to be stopped.
	 */
	public void stop();

	/**
	 * The current listening port.
	 * @return
	 */
	public int getPort();

	/**
	 * Set listen port.
	 * This method must be called before start()
	 * @param port
	 */
	public void setPort(int port);

	/**
	 * @return get the current invoker
	 */
	public Invoker getInvoker();

	/**
	 * Set the server side packet invoker
	 * This method must be called before start()
	 * @param invoker the invoker to set
	 */
	public void setInvoker(Invoker invoker);

	/**
	 * @return the acceptTimeOut
	 */
	public int getAcceptTimeOut();

	/**
	 * Set the server accept timeout
	 * This method must be called before start()
	 * @param acceptTimeOut the acceptTimeOut to set
	 */
	public void setAcceptTimeOut(int acceptTimeOut);

}