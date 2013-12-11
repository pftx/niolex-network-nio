/**
 * MetaData.java
 *
 * Copyright 2013 Niolex, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.niolex.address.ext;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.niolex.commons.codec.StringUtil;

/**
 * 元数据的Java表现形式。
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5
 * @since 2013-1-6
 */
public class MetaData {

    public static final String KEY_IPS = "IPS";
    public static final String KEY_QUOTA = "QUOTA";

    /**
     * Parse meta data from this byte array.
     *
     * @param data the byte array
     * @return the Java bean style meta data
     */
    public static final MetaData parse(byte[] data) {
        String s = StringUtil.utf8ByteToStr(data);
        String[] carr = s.split("\r*\n");
        final MetaData m = new MetaData();
        for (String item : carr) {
            String[] c2 = item.split("=", 2);
            if (c2.length == 2) {
                m.propMap.put(c2[0].trim(), c2[1].trim());
            }
        }
        m.constructBeans();
        return m;
    }

    /**
     * Store all the meta data here.
     */
    private final Map<String, String> propMap = new HashMap<String, String>();

    /**
     * This map is for store java bean, for extra help. the {@link #propMap} will
     * have all the data.
     */
    private final Map<String, Object> beanMap = new HashMap<String, Object>();

    /**
     * Construct Beans when properties updated.
     */
    protected void constructBeans() {
        beanMap.clear();
        String ips = propMap.get(KEY_IPS);
        if (ips != null) {
            beanMap.put(KEY_IPS, Arrays.asList(ips.split("[^\\w.-]+")));
        }
        String quota = propMap.get(KEY_QUOTA);
        if (quota != null) {
            beanMap.put(KEY_QUOTA, QuotaInfo.parse(quota));
        }
    }

    /**
     * Serialize this meta data into byte array.
     *
     * @return the byte array
     */
    public byte[] toByteArray() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String,String> en : propMap.entrySet()) {
            sb.append(en.getKey()).append("=").append(en.getValue()).append("\n");
        }
        return StringUtil.strToUtf8Byte(sb.toString());
    }

    /**
     * @return the properties map
     */
    public Map<String, String> getPropMap() {
        return propMap;
    }

    /**
     * @return the Java beans map
     */
    public Map<String, Object> getBeanMap() {
        return beanMap;
    }

    /**
     * @return the IP list of this meta data
     */
    @SuppressWarnings("unchecked")
    public List<String> getIPs() {
        return (List<String>) beanMap.get(KEY_IPS);
    }

    /**
     * @return the quota info of this meta data
     */
    public QuotaInfo getQuota() {
        return (QuotaInfo) beanMap.get(KEY_QUOTA);
    }

    /**
     * Override super method
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MetaData [propMap=").append(propMap).append(", beanMap=").append(beanMap).append("]");
        return builder.toString();
    }

}
