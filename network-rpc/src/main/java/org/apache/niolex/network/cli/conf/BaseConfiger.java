/**
 * BaseConfiger.java
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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.niolex.commons.stream.StreamUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The base configer to read and parse config file, build config beans.
 * We support property file(which must in classpath) and {@link java.io.InputStream} for now.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0, Date: 2012-6-3
 *
 * @param <B> the real config bean class
 */
public abstract class BaseConfiger<B extends BaseConfigBean> {
    private static final Logger LOG = LoggerFactory.getLogger(BaseConfiger.class);

    public static final String SUPER = "superList";
    public static final String GROUP = "groupList";
    public static final String DEFAULT = "default-group";

    private Properties props;

    protected Map<String, B> superMap = new HashMap<String, B>();
    protected Map<String, B> groupMap = new HashMap<String, B>();

    /**
     * Init the Configer with a property file, relative to the class path of this class.
     *
     * @param fileName property file path name(which must in classpath)
     * @throws IOException when error occurred
     */
    public BaseConfiger(String fileName) throws IOException {
    	this(BaseConfiger.class.getResourceAsStream(fileName), fileName);
    }

    /**
     * Reads a property list (key and element pairs) from the input byte stream.
     * The input stream is in a simple line-oriented format and is assumed to use the ISO 8859-1 character encoding;
     * that is each byte is one Latin1 character. Characters not in Latin1, and certain special characters,
     * are represented in keys and elements using Unicode escapes.
     *
     * The specified stream will be closed after this method returns.
     *
     * @param inStream the stream contains property list
     * @param instanceMark the string mark this configer
     * @throws IOException when error occurred
     */
    public BaseConfiger(InputStream inStream, String instanceMark) throws IOException {
    	props = new Properties();
        try {
            props.load(inStream);
            readSuperList();
            readConfigList();
            LOG.info("Instantiate a new Configer for " + instanceMark + " succeeded.");
        } finally {
        	StreamUtil.closeStream(inStream);
        }
    }

    // ----------------------------------------------------------------------
    // read SuperList
    // ----------------------------------------------------------------------

    private void readSuperList() {
        buildSuper();
        configSuper();
    }

    private void buildSuper() {
        String str = props.getProperty(SUPER);
        if (!StringUtils.isBlank(str)) {
            String[] superArr = str.split(" *[,;] *");
            for (String superName : superArr) {
                superMap.put(superName, newConfigBean(superName));
            }
            LOG.info("[superList] loaded, super list: " + superMap.keySet());
        }
    }

    private void configSuper() {
        if (superMap.isEmpty())
            return;
        for (Entry<Object, Object> entry : props.entrySet()) {
            String key = entry.getKey().toString();
            for (String superName : superMap.keySet()) {
                if (key.startsWith(superName + ".")) {
                    superMap.get(superName).setConfig(key.substring(superName.length() + 1),
                            entry.getValue().toString());
                    break;
                }
            }
        }
    }

    // ----------------------------------------------------------------------
    // read ConfigList
    // ----------------------------------------------------------------------

    private void readConfigList() {
        buildGroup();
        loadSuperConfig();
        configGroup();
    }

    private void buildGroup() {
        String str = props.getProperty(GROUP);
        if (!StringUtils.isBlank(str)) {
            String[] groupArr = str.split(" *[,;] *");
            for (String groupName : groupArr) {
                groupMap.put(groupName, newConfigBean(groupName));
            }
            LOG.info("[groupList] loaded, groups: " + groupMap.keySet());
        } else {
            guessGroup();
            LOG.info("[groupList] property not found, try to guess groups: " + groupMap.keySet());
        }
    }

    private void guessGroup() {
        for (Entry<Object, Object> entry : props.entrySet()) {
            String key = entry.getKey().toString();
            if (key.matches("[\\w-_]+.serviceUrl")) {
                String groupName = key.substring(0, key.indexOf(".serviceUrl"));
                groupMap.put(groupName, newConfigBean(groupName));
            }
        }
        if (groupMap.isEmpty()) {
            groupMap.put(DEFAULT, newConfigBean(DEFAULT));
        }
    }

    private void loadSuperConfig() {
        if (superMap.isEmpty()) {
            return;
        }
        for (String groupName : groupMap.keySet()) {
            String superName = props.getProperty(groupName + ".superName");
            if (!StringUtils.isBlank(superName)) {
                BaseConfigBean superConf = superMap.get(superName);
                if (superConf != null)
                    groupMap.get(groupName).setSuper(superConf);
            }
        }
    }

    private void configGroup() {
        for (Entry<Object, Object> entry : props.entrySet()) {
            String key = entry.getKey().toString();
            if (groupMap.containsKey(DEFAULT)) {
                groupMap.get(DEFAULT).setConfig(key, entry.getValue().toString());
                continue;
            }
            for (String groupName : groupMap.keySet()) {
                if (key.startsWith(groupName + ".")) {
                    groupMap.get(groupName).setConfig(key.substring(groupName.length() + 1),
                            entry.getValue().toString());
                    break;
                }
            }
        }
    }

    abstract protected B newConfigBean(String groupName);

    // ----------------------------------------------------------------------
    // get ConfigList
    // ----------------------------------------------------------------------

    public B getConfig(String groupName) {
        return groupMap.get(groupName);
    }

    public Map<String, B> getConfigs() {
        return groupMap;
    }
}
