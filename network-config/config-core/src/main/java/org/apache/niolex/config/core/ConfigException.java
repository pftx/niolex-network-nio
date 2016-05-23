/**
 * ConfigException.java
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
package org.apache.niolex.config.core;

/**
 * The exception thrown from config internal methods.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-6-27
 */
public class ConfigException extends RuntimeException {

	/**
	 * Generated
	 */
	private static final long serialVersionUID = -6865552546945138458L;

	/**
	 * @param message the exception message
	 */
	public ConfigException(String message) {
		super(message);
	}

	/**
	 * @param message the exception message
	 * @param cause the cause
	 */
	public ConfigException(String message, Throwable cause) {
		super(message, cause);
	}

}
