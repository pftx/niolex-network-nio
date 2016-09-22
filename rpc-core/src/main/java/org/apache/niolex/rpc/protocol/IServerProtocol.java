/**
 * IServerProtocol.java
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
package org.apache.niolex.rpc.protocol;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * This is the interface for server side object serialization protocol.
 * One can implement this interface to extend the rpc-core to support
 * new protocols.
 *
 * @see IClientProtocol
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-8-19
 */
public interface IServerProtocol {

	/**
     * Read parameters from the data.
     * generic can not be null, we already checked.
     *
     * @param data the parameters data
     * @param generic the parameters types array
     * @return the parameters
     * @throws IOException if I / O related error occurred
     */
    public Object[] prepareParams(byte[] data, Type[] generic) throws IOException;

	/**
     * Serialize returned object into byte array.
     * <tt>ret</tt> can not be null, we already checked.
     *
     * @param ret the return object
     * @return the result byte array
     * @throws IOException if I / O related error occurred
     */
    public byte[] serializeReturn(Object ret) throws IOException;

}
