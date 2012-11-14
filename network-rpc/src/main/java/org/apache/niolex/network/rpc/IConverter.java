/**
 * IConverter.java
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

import java.lang.reflect.Type;

/**
 * The Rpc framework use this interface to translate objects into byte array
 * and vice-versa.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0, Date: 2012-11-7
 */
public interface IConverter {

	/**
	 * Read parameters from the data.
	 * generic can not be null, we already checked.
	 *
	 * @param data the byte array
	 * @param generic the parameters type array
	 * @return the parameters array
	 * @throws Exception when error occurs
	 */
	public Object[] prepareParams(byte[] data, Type[] generic) throws Exception;

	/**
	 * Serialize arguments objects into byte array.
	 *
	 * @param args the parameters array
	 * @return the serialized byte array
	 * @throws Exception when error occurs
	 */
	public byte[] serializeParams(Object[] args) throws Exception;

	/**
	 * De-serialize returned byte array into objects.
	 *
	 * @param ret the return byte array
	 * @param type the return object type
	 * @return the returned object
	 * @throws Exception when error occurs
	 */
	public Object prepareReturn(byte[] ret, Type type) throws Exception;

	/**
	 * Serialize returned object into byte array.
	 * ret can not be null, we already checked.
	 *
	 * @param ret the object to return
	 * @return the serialized byte array
	 * @throws Exception when error occurs
	 */
	public byte[] serializeReturn(Object ret) throws Exception;

}
