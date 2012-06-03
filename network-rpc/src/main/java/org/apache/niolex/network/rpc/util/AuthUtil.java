/**
 * AuthUtil.java
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

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.niolex.commons.codec.Base64Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class. Do not initialize.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-5-27
 */
public abstract class AuthUtil {
	private static final Logger LOG = LoggerFactory.getLogger(AuthUtil.class);

	/**
	 * Generate the HTTP basic Authentication header.
	 *
	 * @param username
	 * @param password
	 * @return
	 */
    public static String authHeader(String username, String password) {
        String authString = username + ":" + password;
        String auth = null;
        try {
            auth = "Basic " + Base64Util.byteToBase64(authString.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Basic Auth Header not set: " + e.toString());
        }
        return auth;
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
