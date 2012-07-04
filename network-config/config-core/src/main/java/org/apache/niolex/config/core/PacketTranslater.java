/**
 * PacketTranslater.java
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
package org.apache.niolex.config.core;

import java.util.List;

import org.apache.niolex.commons.codec.StringUtil;
import org.apache.niolex.commons.compress.JacksonUtil;
import org.apache.niolex.config.bean.ConfigItem;
import org.apache.niolex.config.bean.GroupConfig;
import org.apache.niolex.config.bean.SubscribeBean;
import org.apache.niolex.config.bean.SyncBean;
import org.apache.niolex.network.PacketData;

/**
 * Translate beans into packets and translate packets into beans.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-7-3
 */
public class PacketTranslater {

	/**
	 * Translate SubscribeBean into PacketData
	 * @param bean
	 * @return
	 */
	public static final PacketData translate(SubscribeBean bean) {
		try {
			byte[] data = StringUtil.strToUtf8Byte(JacksonUtil.obj2Str(bean));
			return new PacketData(CodeMap.AUTH_SUBS, data);
		} catch (Exception e) {
			throw new ConfigException("Failed to translate SubscribeBean.", e);
		}
	}

	/**
	 * Translate GroupConfig into PacketData
	 * @param bean
	 * @return
	 */
	public static final PacketData translate(GroupConfig conf) {
		try {
			byte[] data = StringUtil.strToUtf8Byte(JacksonUtil.obj2Str(conf));
			return new PacketData(CodeMap.GROUP_DAT, data);
		} catch (Exception e) {
			throw new ConfigException("Failed to translate GroupConfig.", e);
		}
	}

	/**
	 * Translate list of SyncBean into PacketData
	 * @param bean
	 * @return
	 */
	public static final PacketData translate(List<SyncBean> list) {
		try {
			byte[] data = StringUtil.strToUtf8Byte(JacksonUtil.obj2Str(list));
			return new PacketData(CodeMap.GROUP_SYN, data);
		} catch (Exception e) {
			throw new ConfigException("Failed to translate GroupConfig.", e);
		}
	}

	public static final GroupConfig toGroupConfig(PacketData sc) {
		try {
			String s = StringUtil.utf8ByteToStr(sc.getData());
			return JacksonUtil.str2Obj(s, GroupConfig.class);
		} catch (Exception e) {
			throw new ConfigException("Failed to translate GroupConfig.", e);
		}
	}

	public static final ConfigItem toConfigItem(PacketData sc) {
		try {
			String s = StringUtil.utf8ByteToStr(sc.getData());
			return JacksonUtil.str2Obj(s, ConfigItem.class);
		} catch (Exception e) {
			throw new ConfigException("Failed to translate GroupConfig.", e);
		}
	}
}
