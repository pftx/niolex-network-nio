/**
 * Config.java
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
package org.apache.niolex.network;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-5-31
 */
public interface Config {

	/**
	 * Server internal configuration, do not change them if you don't
	 * understand.
	 */
	int SERVER_NIO_BUFFER_SIZE = 8192;
	int SERVER_FAULT_TOLERATE_SIZE = 100;
	int SERVER_CACHE_TOLERATE_SIZE = 5;
	int RPC_HANDLER_POOL_SIZE = 20;
	int SO_CONNECT_TIMEOUT = 5000;
	int RPC_HANDLE_TIMEOUT = 10000;
	String SERVER_ENCODING = "UTF-8";

	/**
	 * Handler attachment key, all system key will start with SYS_
	 * Please keep away from them.
	 */
	String ATTACH_KEY_SESS_HANDLER = "SYS_HAND_SESS_HANDLER";
	String ATTACH_KEY_SESS_SESSID = "SYS_HAND_SESS_SESSID";

	/**
	 * The packet code is a 2-bytes short int. The system will use some this code,
	 * according to the following map:
	 *
	 * CODE		USAGE
	 * 0		Heart Beat
	 * 1-65500	User Range
	 * 65500-~	System Reserved
	 *
	 */
	short CODE_HEART_BEAT = 0;
	short CODE_SESSN_REGR = (short)65501;
}
