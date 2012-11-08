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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.niolex.commons.codec.Base64Util;
import org.apache.niolex.commons.codec.StringUtil;
import org.apache.niolex.commons.stream.JsonProxy;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.rpc.RpcException;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common utils for Rpc.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-1
 */
public abstract class RpcUtil {
	private static final Logger LOG = LoggerFactory.getLogger(RpcUtil.class);

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
		if (ex.getCause() != null) {
			sb.append(ex.getCause().getMessage());
		} else {
			sb.append("NullCause");
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
	 * This is the class to return a type.
	 *
	 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
	 * @version 1.0.0
	 * @Date: 2012-7-24
	 */
	private static class TypeRe<T> extends TypeReference<T> {
		private Type type;

		public TypeRe(Type type) {
			super();
			this.type = type;
		}

		@Override
		public Type getType() {
			return type;
		}
	}

	/**
	 * Decode parameters to JavaType.
	 *
	 * @param generic
	 * @return
	 */
	public static final List<TypeRe<?>> decodeParams(Type[] generic) {
		List<TypeRe<?>> list = new ArrayList<TypeRe<?>>(generic.length);
		for (Type tp : generic) {
			list.add(new TypeRe<String>(tp));
		}
		return list;
	}

	/**
	 * prepare parameters, read them from the data, as the type specified by the second parameter.
	 *
	 * @param data
	 * @param generic
	 * @return
	 * @throws IOException
	 */
	public static final Object[] parseJson(byte[] data, Type[] generic) throws IOException {
		List<TypeRe<?>> list = decodeParams(generic);
		Object[] ret = new Object[list.size()];
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		JsonProxy proxy = new JsonProxy(in);
		for (int i = 0; i < ret.length; ++i) {
			ret[i] = proxy.readObject(list.get(i));
		}
		return ret;
	}

	/**
	 * Generate Key for this PacketData.
	 *
	 * @param rc
	 * @return
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
	 *
	 * @param a
	 * @param b
	 * @param c
	 * @return
	 */
	public static final int generateKey(short a, byte b, byte c) {
		int l = a << 16;
		l += ((b & 0xFF) << 8) + (c & 0xFF);
		return l;
	}

	/**
	 * Generate the HTTP basic Authentication header.
	 *
	 * @param username
	 * @param password
	 * @return
	 */
    public static String authHeader(String username, String password) {
        String authString = username + ":" + password;
        return "Basic " + Base64Util.byteToBase64(StringUtil.strToUtf8Byte(authString));
    }

    /**
     * Generate a random string as the session ID of this client.
     *
     * @param length
     * @return
     */
    public static String genSessionId(int length) {
        String tempId = "";
        int curLen = 0;
        while (curLen < length) {
            tempId = tempId + Long.toHexString((long) (Math.random() * 1000000000000000L))
                    + Long.toHexString(System.nanoTime());
            curLen = tempId.length();
        }

        return tempId.substring(0, length);
    }

    /**
     * Check the connectivity of this url according to the given timeout.
     *
     * @param completeUrl
     * @param connectTimeout
     * @param readTimeout
     * @return
     */
    public static boolean checkServerStatus(String completeUrl, int connectTimeout, int readTimeout) {
		try {
			URL u = new URL(completeUrl);
			URLConnection proxy = u.openConnection();
			proxy.setConnectTimeout(connectTimeout);
			proxy.setReadTimeout(readTimeout);
			proxy.setDoInput(true);
			proxy.setDoOutput(false);
			proxy.connect();
			if (proxy.getContentLength() <= 1) {
				LOG.warn("Failed to connect to " + completeUrl + " : Server response too short.");
				return false;
			}
			String serverStatus = proxy.getHeaderField(0);
			if (serverStatus != null && serverStatus.matches(".*[45][0-9][02-9].*")) {
				LOG.warn("Failed to connect to " + completeUrl + " : Invalid server response " + serverStatus);
				return false;
			}
			LOG.info("Server [" + completeUrl + "] status: " + serverStatus);
			return true;
		} catch (Exception e) {
			LOG.warn("Failed to connect to " + completeUrl + " : " + e.getMessage());
			return false;
		}
	}

}
