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
 * @version 1.0.0, Date: 2012-6-1
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
	 * Create a RpcException with a message, an exception type, and a throwable.
	 *
	 * @param message
	 * @param type
	 * @param cause
	 */
	public RpcException(String message, Type type, Throwable cause) {
		super(message, cause);
		this.type = type;
	}

	/**
	 * Get the Rpc exception Type
	 *
	 * @return the Rpc exception Type
	 */
	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	/**
	 * The rpc exception type. User can get the detailed explanation from this enum.
	 *
	 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
	 * @version 1.0.0, Date: 2012-11-8
	 */
	public static enum Type {
		TIMEOUT("Rpc timeout, maybe network problem, server busy etc."),
		NOT_CONNECTED("Client has not connected to server yet."),
		CONNECTION_CLOSED("Client connection to server is closed due to previous problems."),
		METHOD_NOT_FOUND("The method client want to invoke is not found on server side."),
		ERROR_PARSE_PARAMS("Error occured when server try to parse parameters."),
		ERROR_INVOKE("Error occured when server invoke this method on site."),
		ERROR_PARSE_RETURN("Error occured when client try to parse the value returned from server."),
		UNKNOWN("Unknown other error."),
		NO_SERVER_READY("No rpc server is ready for now."),
		ERROR_EXCEED_RETRY("We retried the number of times according to config, but still error.");

		/**
		 * the detailed explanation for error type.
		 */
		private final String explanation;

		/**
		 * Create a type with detailed explanation.
		 *
		 * @param explanation
		 */
		private Type(String explanation) {
			this.explanation = explanation;
		}

		/**
		 * Get the detailed explanation for error type.
		 * @return
		 */
		public String getExplanation() {
			return explanation;
		}

	}

}
