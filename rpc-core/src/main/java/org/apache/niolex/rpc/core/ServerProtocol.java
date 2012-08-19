/**
 * ServerProtocol.java
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
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-8-19
 */
public interface ServerProtocol {

	/**
	 * Read parameters from the data.
	 * generic can not be null, we already checked.
	 *
	 * @param data
	 * @param generic
	 * @return
	 * @throws Exception
	 */
	public Object[] prepareParams(byte[] data, Type[] generic) throws Exception;

	/**
	 * Serialize returned object into byte array.
	 * ret can not be null, we already checked.
	 *
	 * @param ret
	 * @return
	 * @throws Exception
	 */
	public byte[] serializeReturn(Object ret) throws Exception;

}
