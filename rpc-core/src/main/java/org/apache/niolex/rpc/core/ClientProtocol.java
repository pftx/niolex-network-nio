/**
 * ClientProtocol.java
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

import java.lang.reflect.Type;

/**
 * This is the interface for client side object serialization protocol.
 * One can implement this interface to extend the rpc-core to support
 * new protocols.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-8-19
 */
public interface ClientProtocol {

	/**
	 * Serialize arguments objects into byte array.
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public byte[] serializeParams(Object[] args) throws Exception;

	/**
	 * De-serialize returned byte array into objects.
	 * @param ret
	 * @param type
	 * @return
	 * @throws Exception
	 */
	public Object prepareReturn(byte[] ret, Type type) throws Exception;

}
