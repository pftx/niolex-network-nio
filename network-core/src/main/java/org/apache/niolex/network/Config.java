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
 * The main configuration file.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-5-31
 */
public interface Config {

	/**
	 * Server internal configuration, do not change them if you don't
	 * understand.
	 */

	/**
	 * The NIO Byte buffer size for server socket.
	 */
	int SERVER_NIO_BUFFER_SIZE = 8192;

	/**
	 * The max packet size for this server. Default to 10MB
	 */
	int SERVER_MAX_PACKET_SIZE = 1024 * 10240;

	/**
	 * The fault tolerate map size, which contains all the fault client data.
	 * Configure this size too much will consume much memory.
	 */
	int SERVER_FAULT_TOLERATE_MAP_SIZE = 256;

	/**
	 * Save this number of sent packets. Nio sent packets asynchronously, so there may
	 * be some packet sent to buffer but failed to send to client.
	 */
	int SERVER_CACHE_TOLERATE_PACKETS_SIZE = 5;


	/**
	 * Server socket accept timeout, set it too large will cause server no responding to stop command.
	 */
	int SERVER_ACCEPT_TIMEOUT = 5000;

	/**
	 * Server heart beat packet interval. If there is no data to send to client between this time,
	 * server will send a heart beat to client.
	 */
	int SERVER_HEARTBEAT_INTERVAL = 10000;

	/**
	 * The default server listen port.
	 */
	int SERVER_DEFAULT_PORT = 8808;

	/**
	 * The Rpc packet handler internal thread pool size.
	 */
	int RPC_HANDLER_POOL_SIZE = 200;

	/**
	 * Sleep between each retry to connect to rpc server.
	 */
	int RPC_SLEEP_BT_RETRY = 1000;

	/**
	 * Rpc retry times try to connect to rpc server. If the client can not get a valid
	 * connection after this times of retry, client will stop.
	 */
	int RPC_CONNECT_RETRY_TIMES = 3;

	/**
	 * Socket connect and read timeout. this is the low level timeout. Please do not configure
	 * this time small than 20 seconds, for that there will be at least heart beat packets
	 * every 10 seconds.
	 */
	int SO_CONNECT_TIMEOUT = 20000;

	/**
	 * Rpc handle timeout. If the client can not get response packet after this time, it will throw
	 * an exception to the upper layer.
	 */
	int RPC_HANDLE_TIMEOUT = 30000;

	/**
	 * Server internal character encoding. UTF-8 is commonly used.
	 */
	String SERVER_ENCODING = "UTF-8";

	/**
	 * Field separator of name service.
	 */
	String NAME_FIELD_SEP = "/*/";

	/**
	 * The regex representation of field separator.
	 */
	String NAME_FIELD_SEP_REGEX = "/\\*/";

	/**
	 * Handler attachment key, all system key will start with SYS_
	 * Please keep away from them.
	 */

	/**
	 * Attach session handler to the packet writer with this key.
	 */
	String ATTACH_KEY_SESS_HANDLER = "SYS_HAND_SESS_HANDLER";

	/**
	 * Attach client uuid which is for fault tolerate.
	 */
	String ATTACH_KEY_FAULTTO_UUID = "SYS_HAND_FAULTTO_UUID";

	/**
	 * Attach the fault packet data round robin list
	 */
	String ATTACH_KEY_FAULT_RRLIST = "SYS_HAND_FAULT_RRLIST";

	/**
	 * Attach last packet send time which is for heart beat.
	 */
	String ATTACH_KEY_HEART_BEAT = "SYS_HAND_HEART_BEAT";

	/**
	 * Attach the registered service address.
	 */
	String ATTACH_KEY_REGIST_ADDR = "SYS_HAND_REGIST_ADDR";

	/**
	 * Attach the service address listen key.
	 */
	String ATTACH_KEY_OBTAIN_ADDR = "SYS_HAND_OBTAIN_ADDR";

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

	/**
	 * Register the client UUID to server.
	 */
	short CODE_REGR_UUID = (short)65501;

	/**
	 * Register the client Need heart beat.
	 */
	short CODE_REGR_HBEAT = (short)65502;

	/**
	 * Register the name service address.
	 */
	short CODE_NAME_PUBLISH = (short)65503;

	/**
	 * Listen the name service address.
	 */
	short CODE_NAME_OBTAIN = (short)65504;

	/**
	 * The name service address data.
	 */
	short CODE_NAME_DATA = (short)65505;

	/**
	 * The name service address difference increment package.
	 */
	short CODE_NAME_DIFF = (short)65506;

	/**
	 * Packet not recognized.
	 */
	short CODE_NOT_RECOGNIZED = -1;
}
