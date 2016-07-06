/**
 * RpcUtil.java
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
package org.apache.niolex.rpc.util;

import java.lang.reflect.Type;

import org.apache.niolex.commons.codec.StringUtil;
import org.apache.niolex.rpc.RpcException;

/**
 * Common utils for Rpc.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-11-8
 */
public abstract class RpcUtil {

	/**
	 * The field separator to split the fields in rpc exception.
	 */
	private static final String SEP_RPCEX = "#~@&";

	/**
	 * Serialize the rpc exception into byte array.
	 * This is for those protocol which needs static code change to do
	 * object serialization.
	 *
	 * @param ex
	 * @return
	 */
	public static final byte[] serializeRpcException(RpcException ex) {
	    StringBuilder sb = new StringBuilder();
        sb.append(ex.getMessage()).append(SEP_RPCEX).append(ex.getType()).append(SEP_RPCEX);
        Throwable cause = ex.getCause();
        // Find the root cause.
        while (cause != null && cause.getCause() != null) {
            cause = cause.getCause();
        }
        // Mark the cause.
        if (cause != null) {
            sb.append(cause.toString());
            StackTraceElement[] sarr = cause.getStackTrace();
            if (sarr != null && sarr.length >= 1) {
                sb.append('@').append(sarr[0].toString());
            }
        } else {
            sb.append("NullCause@air cloud");
        }
        return StringUtil.strToUtf8Byte(sb.toString());
	}

	/**
	 * Parse the rpc exception from this byte array.
	 *
	 * @param data
	 * @return
	 */
	public static final RpcException parseRpcException(byte[] data) {
		String[] strs = StringUtil.utf8ByteToStr(data).split(SEP_RPCEX);
		RpcException.Type type = RpcException.Type.valueOf(strs[1]);
		RpcException ex = new RpcException(strs[0], type,
				new Throwable(strs[2]));
		return ex;
	}

    /**
     * Check the generic array is in fact a class array.
     *
     * @param generic the generic type array
     * @return the checked class array
     * @throws ClassCaseException if any item in the generic array is not a class
     */
    public static final Class<?>[] checkParams(Type[] generic) {
        Class<?>[] array = new Class<?>[generic.length];
        for (int i = 0; i < generic.length; ++i) {
            array[i] = (Class<?>) generic[i];
        }
        return array;
    }
    
}
