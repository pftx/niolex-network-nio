/**
 * RpcException.java
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
package org.apache.niolex.network.rpc;

/**
 * The Base Exception thrown in this Rpc framework.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-1
 */
public class RpcException extends RuntimeException {

	/**
	 * Generated serial number.
	 */
	private static final long serialVersionUID = -4027742478277292216L;

	/**
	 * The exception type.
	 */
	private Type type;


	/**
	 * Default constructor, used by Serial Tools.
	 */
	public RpcException() {
		super();
	}


	/**
	 * Create a RpcException with a message and a throwable.
	 *
	 * @param message
	 * @param type
	 * @param cause
	 */
	public RpcException(String message, Type type, Throwable cause) {
		super(message, cause);
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public static enum Type {
		TIMEOUT, NOT_CONNECTED, CONNECTION_CLOSED, METHOD_NOT_FOUND, ERROR_PARSE_PARAMS,
		ERROR_INVOKE, ERROR_PARSE_RETURN, UNKNOWN
	}

}
