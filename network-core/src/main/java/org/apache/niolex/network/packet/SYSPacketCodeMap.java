/**
 * SYSPacketCodeMap.java
 *
 * Copyright 2011 Niolex, Inc.
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
package org.apache.niolex.network.packet;

/**
 * The packet code is a 2-bytes short int. The system will use some this code, according to the following map:
 *
 * CODE		USAGE
 * 0		Heart Beat
 * 1-65500	User Range
 * 65500-~	System Reserved
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-5-30
 */
public interface SYSPacketCodeMap {
	short HEART_BEAT = 0;

	short SESSN_REGR = (short)65501;
}
