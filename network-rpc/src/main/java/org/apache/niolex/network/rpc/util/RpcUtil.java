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
package org.apache.niolex.network.rpc.util;

import java.lang.reflect.InvocationTargetException;

import org.apache.niolex.commons.codec.Base64Util;
import org.apache.niolex.commons.codec.StringUtil;
import org.apache.niolex.commons.util.ThrowableUtil;
import org.apache.niolex.network.ConnStatus;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.rpc.RpcException;
import org.apache.niolex.network.rpc.cli.BaseInvoker;
import org.apache.niolex.network.rpc.cli.RemoteInvoker;
import org.apache.niolex.network.rpc.cli.RpcStub;
import org.apache.niolex.network.rpc.cli.SocketInvoker;

/**
 * Common utilities for Rpc.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0, Date: 2012-6-1
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
	 * @param ex the exception need to serialize
	 * @return the serialized byte array
	 */
	public static final byte[] serializeRpcException(RpcException ex) {
		StringBuilder sb = new StringBuilder();
		sb.append(ex.getMessage()).append(SEP_RPCEX).append(ex.getType()).append(SEP_RPCEX);

		Throwable cause = ex.getCause();
		if (cause != null) {
		    if (cause instanceof InvocationTargetException) {
		        cause = cause.getCause();
		    }
		    sb.append(ThrowableUtil.throwableToString(cause));
		}

		return StringUtil.strToUtf8Byte(sb.toString());
	}

	/**
	 * Parse the rpc exception from this byte array.
	 *
	 * @param data the serialized byte array
	 * @return the exception
	 */
	public static final RpcException parseRpcException(byte[] data) {
		String[] strs = StringUtil.split(StringUtil.utf8ByteToStr(data), SEP_RPCEX, true);

		RpcException.Type type = RpcException.Type.valueOf(strs[1]);
		Throwable root = null;
		if (strs[2].length() != 0) {
		    try {
                root = ThrowableUtil.strToThrowable(strs[2]);
            } catch (Exception e) {
                root = e;
            }
		}

		RpcException ex = new RpcException(strs[0], type, root);
		return ex;
	}

	/**
	 * Generate Key for this PacketData.
	 * The first two bytes are packet code, then the packet version, then the reserved.
	 *
	 * @param rc the packet
	 * @return the generated key
	 */
	public static final int generateKey(PacketData rc) {
		byte r = rc.getReserved();
		if (r % 2 == 0) {
			--r;
		}
		return generateKey(rc.getCode(), rc.getVersion(), r);
	}

	/**
	 * Generate Key from a short and two bytes.
	 * The output value will be "abc" concatenation.
	 *
	 * @param a the packet code
	 * @param b the packet version
	 * @param c the reserved field
	 * @return the generated key
	 */
	public static final int generateKey(short a, byte b, byte c) {
		int l = a << 16;
		l += ((b & 0xFF) << 8) + (c & 0xFF);
		return l;
	}

	/**
	 * Generate the HTTP basic Authentication header.
	 *
	 * @param username the user name
	 * @param password the password
	 * @return the basic authentication header
	 */
    public static String authHeader(String username, String password) {
        String authString = username + ":" + password;
        return "Basic " + Base64Util.byteToBase64(StringUtil.strToUtf8Byte(authString));
    }

    /**
     * Generate a random string as the session ID of this client.
     * The generation is based on random number and system time stamp.
     *
     * @param length the length of session ID user needed
     * @return the generated session ID
     */
    public static String genSessionId(int length) {
        StringBuilder sb = new StringBuilder();
        while (sb.length() < length) {
            sb.append(Long.toHexString((long) (Math.random() * 1000000000000000L)));
            sb.append(Long.toHexString(System.nanoTime()));
        }

        return sb.substring(0, length);
    }

    /**
     * Check whether the connection behind the specified Rpc stub is closed.
     * 
     * @param stub the rpc stub
     * @return true if connection is closed, false otherwise
     */
    public static boolean connectionClosed(RpcStub stub) {
        RemoteInvoker invoker = stub.getInvoker();
        if (invoker instanceof BaseInvoker) {
            BaseInvoker bi = (BaseInvoker) invoker;
            return bi.getConnStatus() == ConnStatus.CLOSED;
        } else if (invoker instanceof SocketInvoker) {
            SocketInvoker si = (SocketInvoker) invoker;
            return si.isStoped();
        }
        return false;
    }

    /**
     * Check whether this Rpc stub is ready for use by the next time.
     *
     * @param stub the rpc stub
     * @return true if it's ready and not marked as abandon, false otherwise
     */
    public static boolean isInUse(RpcStub stub) {
        RemoteInvoker invoker = stub.getInvoker();
        if (invoker instanceof BaseInvoker) {
            BaseInvoker bi = (BaseInvoker) invoker;
            return bi.getConnectRetryTimes() > 0;
        } else if (invoker instanceof SocketInvoker) {
            SocketInvoker si = (SocketInvoker) invoker;
            return si.getConnectRetryTimes() > 0;
        }
        return true;
    }

    /**
     * Mark this stub as abandon.
     * 
     * @param stub the rpc stub
     */
    public static void markAbandon(RpcStub stub) {
        RemoteInvoker invoker = stub.getInvoker();
        if (invoker instanceof BaseInvoker) {
            BaseInvoker bi = (BaseInvoker) invoker;
            bi.setConnectRetryTimes(0);
        } else if (invoker instanceof SocketInvoker) {
            SocketInvoker si = (SocketInvoker) invoker;
            si.setConnectRetryTimes(0);
        }
    }

    /**
     * Set the connect retry times of the specified Rpc stub.
     * 
     * @param stub the rpc stub
     * @param connectRetryTimes the new connect retry times to set
     */
    public static void setConnectRetryTimes(RpcStub stub, int connectRetryTimes) {
        RemoteInvoker invoker = stub.getInvoker();
        if (invoker instanceof BaseInvoker) {
            BaseInvoker bi = (BaseInvoker) invoker;
            bi.setConnectRetryTimes(connectRetryTimes);
        } else if (invoker instanceof SocketInvoker) {
            SocketInvoker si = (SocketInvoker) invoker;
            si.setConnectRetryTimes(connectRetryTimes);
        }
    }

}
