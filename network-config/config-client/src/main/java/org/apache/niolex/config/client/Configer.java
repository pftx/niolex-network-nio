/**
 * Configer.java
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
package org.apache.niolex.config.client;

import org.apache.niolex.config.bean.ConfigItem;
import org.apache.niolex.config.bean.ConfigGroup;
import org.apache.niolex.config.core.ConfigException;
import org.apache.niolex.config.event.ConfigListener;

/**
 * Configer是一个用来读取配置中心配置的工具类
 * <p>
 * 配置中心默认会读取classpath下的conf-client.properties配置文件进行自配置，用户可以通过
 * 系统变量-Dconfig.client.property.file=xxx.properties的形式改写配置文件位置。
 *
 * @category niolex-network-config -> 公共库 -> 配置处理
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 */

public class Configer extends ConfigClient {

	/**
	 * The internal group config storage.
	 */
    private final ConfigGroup config;

    /**
     * Create a Configer to manage the configuration of this group.
     *
     * @param groupName the group name of this config group.
     */
    public Configer(String groupName) {
		super();
		try {
			this.config = getConfigGroup(groupName);
		} catch (ConfigException e) {
			throw e;
		} catch (Exception e) {
			throw new ConfigException("Failed to load group " + groupName, e);
		}
		if (this.config == null) {
			throw new ConfigException("Failed to load group " + groupName);
		}
	}

	/**
	 * Add an event listener to watch the config with this key changes.
	 * Attention! We can only manage one listener for one key.
	 *
	 * @param key the config item key
	 * @param listener the listener
	 * @return the old listener if there is any, null otherwise.
	 */
	public ConfigListener addListener(String key, ConfigListener listener) {
		return registerEventHandler(config.getGroupName(), key, listener);
	}

    /**
     * 从配置中心读取字符串类型的配置
     *
     * @param key 待读取的配置的键
     * @return 待读取的配置的值，如果配置不存在则返回null
     */
    public final String getProperty(String key) {
    	ConfigItem item = config.getGroupData().get(key);
    	if (item != null) {
    		return item.getValue();
    	}
        return null;
    }

    /**
     * 从配置中心读取字符串类型的配置
     *
     * @param key 待读取的配置的键
     * @param defaultValue 待读取的配置的默认值
     * @return 待读取的配置的值，如果配置不存在则返回<code>defaultValue</code>
     */
    public final String getProperty(String key, String defaultValue) {
    	String value = getProperty(key);
        return value == null ? defaultValue : value;
    }

    /**
     * 从配置中心读取字符串类型的配置
     *
     * @see #getProperty(String)
     * @param key 待读取的配置的键
     * @return 待读取的配置的值，如果配置不存在则返回null
     */
    public final String getString(String key) {
        return getProperty(key);
    }

    /**
     * 从配置中心读取字符串类型的配置
     *
     * @see #getProperty(String, String)
     * @param key 待读取的配置的键
     * @param defaultValue 待读取的配置的默认值
     * @return 待读取的配置的值，如果配置不存在则返回<code>defaultValue</code>
     */
    public final String getString(String key, String defaultValue) {
        return getProperty(key, defaultValue);
    }

    /**
     * 从配置中心读取整数类型的配置
     *
     * @param key 待读取的配置的键
     * @return 待读取的配置的值，如果配置不存在则抛出NumberFormatException
     * @throws NumberFormatException 如果配置不存在,或者配置不是可以解析的整数
     */
    public final int getInteger(String key) {
        return Integer.parseInt(getProperty(key));
    }

    /**
     * 从配置中心读取整数类型的配置
     *
     * @param key 待读取的配置的键
     * @param defaultValue 待读取的配置的默认值，请确保该默认值是可以解析的整数
     * @return 待读取的配置的值，如果配置不存在则使用<code>defaultValue</code>
     * @throws NumberFormatException 如果配置不存在时<code>defaultValue</code>不是可以解析的整数，
     *         或者配置不是可以解析的整数
     */
    public final int getInteger(String key, String defaultValue) {
        return Integer.parseInt(getProperty(key, defaultValue));
    }

    /**
     * 从配置中心读取整数类型的配置
     *
     * @param key 待读取的配置的键
     * @param defaultValue 待读取的配置的默认值
     * @return 待读取的配置的值，如果配置不存在则使用<code>defaultValue</code>
     * @throws NumberFormatException 如果配置不是可以解析的整数
     */
    public final int getInteger(String key, int defaultValue) {
        return Integer.parseInt(getProperty(key, Integer.toString(defaultValue)));
    }

    /**
     * 从配置中心读取长整数类型的配置
     *
     * @param key 待读取的配置的键
     * @return 待读取的配置的值，如果配置不存在则抛出NumberFormatException
     * @throws NumberFormatException 如果配置不存在,或者配置不是可以解析的整数
     */
    public final long getLong(String key) {
    	return Long.parseLong(getProperty(key));
    }

    /**
     * 从配置中心读取长整数类型的配置
     *
     * @param key 待读取的配置的键
     * @param defaultValue 待读取的配置的默认值，请确保该默认值是可以解析的整数
     * @return 待读取的配置的值，如果配置不存在则使用<code>defaultValue</code>
     * @throws NumberFormatException 如果配置不存在时<code>defaultValue</code>不是可以解析的整数，
     *         或者配置不是可以解析的整数
     */
    public final long getLong(String key, String defaultValue) {
    	return Long.parseLong(getProperty(key, defaultValue));
    }

    /**
     * 从配置中心读取长整数类型的配置
     *
     * @param key 待读取的配置的键
     * @param defaultValue 待读取的配置的默认值
     * @return 待读取的配置的值，如果配置不存在则使用<code>defaultValue</code>
     * @throws NumberFormatException 如果配置不是可以解析的整数
     */
    public final long getLong(String key, long defaultValue) {
    	return Long.parseLong(getProperty(key, Long.toString(defaultValue)));
    }

    /**
     * 从配置中心读取布尔类型的配置
     * 当且仅当配置为字符串"true"(忽略大小写)时，返回true,其他情况一概返回false
     *
     * @param key 待读取的配置的键
     * @return 待读取的配置的值，（请注意）如果配置不存在则返回false
     */
    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(getProperty(key));
    }

    /**
     * 从配置中心读取布尔类型的配置
     * 当且仅当配置为字符串"true"(忽略大小写)时，返回true,其他情况一概返回false
     *
     * @param key 待读取的配置的键
     * @param defaultValue 待读取的配置的默认值
     * @return 待读取的配置的值，如果配置不存在则使用<code>defaultValue</code>
     */
    public boolean getBoolean(String key, String defaultValue) {
        return Boolean.parseBoolean(getProperty(key, defaultValue));
    }

    /**
     * 从配置中心读取布尔类型的配置
     * 当且仅当配置为字符串"true"(忽略大小写)时，返回true,其他情况一概返回false
     *
     * @param key 待读取的配置的键
     * @param defaultValue 待读取的配置的默认值
     * @return 待读取的配置的值，如果配置不存在则使用<code>defaultValue</code>
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        return Boolean.parseBoolean(getProperty(key, Boolean.toString(defaultValue)));
    }
}
