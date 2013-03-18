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
import java.util.Map;

import org.apache.niolex.commons.bean.MutableOne;
import org.apache.niolex.commons.codec.StringUtil;

/**
 * 元数据的Java表现形式。
 * 
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5
 * @since 2013-1-6
 */
public class MetaData {
    
    /**
     * Wrap the raw meta data into Java bean style meta data.
     * 
     * @param rawData the raw meta data
     * @return the Java bean style meta data
     */
    public static MutableOne<MetaData> wrap(MutableOne<byte[]> rawData) {
        final MetaData m = new MetaData();
        final MutableOne<MetaData> ret = new MutableOne<MetaData>(m);
        // Listen to data change.
        rawData.addListener(new MutableOne.DataChangeListener<byte[]>() {
            @Override
            public void onDataChange(byte[] newData) {
                m.parse(newData);
                ret.updateData(m);
            }});
        // Parse the first data.
        m.parse(rawData.data());
        return ret;
    }
    
    /**
     * Store all the meta data here.
     */
    private final Map<String, String> propMap = new HashMap<String, String>();
    
    /**
     * This map is for store java bean, extra help.
     */
    private final Map<String, Object> beanMap = new HashMap<String, Object>();
    
    /**
     * Parse meta data from this byte array.
     * 
     * @param data the byte array
     */
    public void parse(byte[] data) {
        String s = StringUtil.utf8ByteToStr(data);
        String[] carr = s.split("\r*\n");
        propMap.clear();
        for (String item : carr) {
            String[] c2 = item.split(" *= *", 2);
            if (c2.length == 2) {
                propMap.put(c2[0], c2[1]);
            }
        }
        constructBeans();
    }
    
    /**
     * Construct Beans when properties updated.
     */
    protected void constructBeans() {
        beanMap.clear();
        String ips = propMap.get("IPS");
        if (ips != null) {
            beanMap.put("IPS", Arrays.asList(ips.split(" *[,;:] *")));
        }
        String quotas = propMap.get("QUOTAS");
        if (quotas != null) {
            beanMap.put("QUOTAS", QuotaInfo.parse(quotas));
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
     * @return the propMap
     */
    public Map<String, String> getPropMap() {
        return propMap;
    }

    /**
     * @return the beanMap
     */
    public Map<String, Object> getBeanMap() {
        return beanMap;
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
