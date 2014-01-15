/**
 * BaseConfigBean.java
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
package org.apache.niolex.network.cli.conf;

import java.util.HashMap;
import java.util.Map;

import org.apache.niolex.commons.reflect.FieldUtil;

/**
 * The base configuration bean.
 * We save all properties into the prop map, and save
 * all the header properties into the header map if necessary.
 * <br>
 * If you set hasHeader to false(which is the default value), we will not judge a property
 * to be header property or not, just take it as a common property.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0, Date: 2012-5-27
 */
public class BaseConfigBean {
    protected Map<String, String> prop = new HashMap<String, String>();
    protected Map<String, String> header = new HashMap<String, String>();
	protected String groupName;
	protected boolean hasHeader = false;

	/**
	 * Create a new config bean with this group name.
	 * Every config bean will save lots of properties, they are considered as a config group.
	 *
     * @param groupName The group name
     */
    public BaseConfigBean(String groupName) {
        super();
        this.groupName = groupName;
    }

    /**
     * Set configuration into this bean.
     * <br>
     * If hasHeader is set to true, any property key start with header. will
     * be considered as header property.
     *
     * @param key the property key
     * @param value the property value
     */
    public void setConfig(String key, String value) {
    	// 1. process header
		if (hasHeader && key.startsWith("header.")) {
			header.put(key.substring(7), value);
			return;
		}
		try {
			// 2. process fields
		    FieldUtil.setValueAutoConvert(this, key, value);
		} catch (Exception e) {
			// 3. save other property into map
			prop.put(key, value);
		}
	}

    /**
     * Copy the super properties into this bean.
     *
     * @param superConf the template config bean used to configure common properties
     */
    protected void setSuper(final BaseConfigBean superConf) {
	    prop.putAll(superConf.prop);
	    header.putAll(superConf.header);
	}

    // /////////////////////////////////////////////////////////////////////////////////////
    // GETTERS & SETTERS
    // /////////////////////////////////////////////////////////////////////////////////////

    public String getProp(String key) {
        return prop.get(key);
    }

    public String getHeader(String key) {
        return header.get(key);
    }

    public Map<String, String> getProp() {
        return prop;
    }

    public Map<String, String> getHeader() {
        return header;
    }

    public String getGroupName() {
        return groupName;
    }

    public boolean hasHeader() {
        return hasHeader;
    }

}
