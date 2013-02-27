/**
 * Invocation.java
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
package org.apache.niolex.rpc.core;

import java.util.concurrent.RejectedExecutionException;

import org.apache.niolex.commons.codec.StringUtil;
import org.apache.niolex.network.Config;
import org.apache.niolex.network.Packet;

/**
 * Invoke the invoker, and write the result to rpc core.
 * This class implement the Runnable interface, so user can submit it to any thread pool.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-8-19
 */
public class Invocation implements Runnable {

	private RpcCore rpcCore;
	private Invoker invoker;
	private Packet parameter;

	/**
	 * The Constructor.
	 *
	 * @param rpcCore
	 * @param invoker
	 */
	public Invocation(RpcCore rpcCore, Invoker invoker) {
		super();
		this.rpcCore = rpcCore;
		this.invoker = invoker;
		this.parameter = rpcCore.readFinished();
	}



	/**
	 * Get the packet from rpc core, Invoke the invoker, and Write the result to rpc core.
	 * This is the override of super method.
	 *
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		Packet res = invoker.process(parameter);
		if (res == null) {
			prepareError("Rpc Invoker returned null.");
		} else {
			rpcCore.prepareWrite(res);
		}
	}

	/**
	 * Prepare an error to write to client.
	 *
	 * @param e
	 * @param pc
	 */
	public void prepareError(String e) {
		Packet ret = new Packet(Config.CODE_RPC_ERROR, StringUtil.strToUtf8Byte(e));
		ret.setSerial(parameter.getSerial());
		rpcCore.prepareWrite(ret);
	}



	/**
	 * Prepare an error to write to client.
	 *
	 * @param e
	 */
	public void prepareError(Exception e) {
		String s;
		if (e instanceof RejectedExecutionException) {
			s = "Server too busy.";
		} else {
			s = "Server internal error.";
		}
		prepareError(s);
	}

}
