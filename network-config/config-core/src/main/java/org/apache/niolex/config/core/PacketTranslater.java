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

import java.util.ArrayList;
import java.util.List;

import org.apache.niolex.commons.codec.StringUtil;
import org.apache.niolex.commons.compress.JacksonUtil;
import org.apache.niolex.config.bean.ConfigItem;
import org.apache.niolex.config.bean.GroupConfig;
import org.apache.niolex.config.bean.SubscribeBean;
import org.apache.niolex.config.bean.SyncBean;
import org.apache.niolex.network.PacketData;
import org.codehaus.jackson.map.type.TypeFactory;

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
			throw new ConfigException("Failed to translate from SubscribeBean.", e);
		}
	}


	/**
	 * Translate PacketData into SubscribeBean
	 * @param sc
	 * @return
	 */
	public static final SubscribeBean toSubscribeBean(PacketData sc) {
		try {
			String s = StringUtil.utf8ByteToStr(sc.getData());
			return JacksonUtil.str2Obj(s, SubscribeBean.class);
		} catch (Exception e) {
			throw new ConfigException("Failed to translate to SubscribeBean.", e);
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
			throw new ConfigException("Failed to translate from GroupConfig.", e);
		}
	}

	/**
	 * Translate PacketData into GroupConfig
	 * @param sc
	 * @return
	 */
	public static final GroupConfig toGroupConfig(PacketData sc) {
		try {
			String s = StringUtil.utf8ByteToStr(sc.getData());
			return JacksonUtil.str2Obj(s, GroupConfig.class);
		} catch (Exception e) {
			throw new ConfigException("Failed to translate to GroupConfig.", e);
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
			throw new ConfigException("Failed to translate from SyncBean.", e);
		}
	}


	/**
	 * Translate PacketData into SyncBean
	 * @param sc
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static final List<SyncBean> toSyncBean(PacketData sc) {
		try {
			String s = StringUtil.utf8ByteToStr(sc.getData());
			return JacksonUtil.str2Obj(s, TypeFactory.collectionType(ArrayList.class, SyncBean.class));
		} catch (Exception e) {
			throw new ConfigException("Failed to translate to SyncBean.", e);
		}
	}

	/**
	 * Translate ConfigItem into PacketData
	 * @param bean
	 * @return
	 */
	public static final PacketData translate(ConfigItem item) {
		try {
			byte[] data = StringUtil.strToUtf8Byte(JacksonUtil.obj2Str(item));
			return new PacketData(CodeMap.GROUP_DIF, data);
		} catch (Exception e) {
			throw new ConfigException("Failed to translate from ConfigItem.", e);
		}
	}

	/**
	 * Translate PacketData into ConfigItem
	 * @param sc
	 * @return
	 */
	public static final ConfigItem toConfigItem(PacketData sc) {
		try {
			String s = StringUtil.utf8ByteToStr(sc.getData());
			return JacksonUtil.str2Obj(s, ConfigItem.class);
		} catch (Exception e) {
			throw new ConfigException("Failed to translate to ConfigItem.", e);
		}
	}
}
