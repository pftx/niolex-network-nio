/**
 * AdvancedProducer.java
 *
 * Copyright 2013 The original author or authors.
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

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.niolex.address.core.FindException;
import org.apache.niolex.address.core.RecoverableWatcher;
import org.apache.niolex.address.server.Producer;
import org.apache.niolex.address.util.PathUtil;
import org.apache.niolex.commons.codec.StringUtil;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.data.Stat;

/**
 * The Advanced Producer is for access the meta data from ZK.
 * 元数据的路径：/<root>/services/<service>/clients/<version>/<client-name> ==> [meta-key]:[meta-value]
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5
 * @since 2013-3-15
 */
public class AdvancedProducer extends Producer {

    /**
     * 构造函数,继承自父类
     *
     * @param clusterAddress zookeeper集群的地址
     * @param sessionTimeout 会话超时的时间
     * @throws IOException 如果与集群建立连接失败，则会抛出异常
     */
    public AdvancedProducer(String clusterAddress, int sessionTimeout) throws IOException {
        super(clusterAddress, sessionTimeout);
    }


    /**
     * Get the meta data under the version node and listen changes.
     * This is a wrap of method: {@link #getMetaData(String, int)}
     * 获取存储在version节点下的元数据并监听他的变化。
     * <p>
     * 本方法是对{@link #getMetaData(String, int)}方法的封装。
     *
     * @param service 服务的唯一名称，例如org.apache.niolex.address.Test
     * @param version 支持3种格式，参考[version的格式]章节
     * @return 当前的元数据；系统会监听该元数据的变化
     * @throws FindException 当发生异常时
     */
    public ConcurrentHashMap<String, MetaData> getMetaData(String service, String version) {
        return getMetaData(service, this.getCurrentVersion(service, version));
    }

    /**
     * Get the meta data of this service version, including IP list etc.
     * 返回值里面的数据是动态更新的，您不需要再次调用本接口。
     *
     * @param service 服务的唯一名称，例如org.apache.niolex.address.Test
     * @param version 服务的版本信息，例如100
     * @return 当前的元数据；系统会监听该元数据的变化
     * @throws FindException 当发生异常时
     */
    public ConcurrentHashMap<String, MetaData> getMetaData(String service, int version) {
        if (this.root == null) {
            throw new IllegalStateException("Root not set.");
        }
        if (version < 1) {
            throw new IllegalArgumentException("Version must greater than 0.");
        }
        String path = PathUtil.makeMetaPath(root, service, version);
        try {
            LOG.info("Try to get meta data from: {}.", path);
            ConcurrentHashMap<String, MetaData> map = new ConcurrentHashMap<String, MetaData>();
            DataWatcher wat = new DataWatcher(map);
            byte[] b = (byte[]) this.submitWatcher(path, wat, false);
            wat.setData(b);
            // Merge all the meta data for the first time.
            List<String> clients = this.getChildren(path);
            for (String client : clients) {
                wat.parseMetaData(path, client);
            }
            return map;
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException)e;
            }
            throw FindException.makeInstance("Failed to get meta data.", e);
        }
    }

    /**
     * Parse the meta signature.
     *
     * @param data
     * @return the map
     */
    public HashMap<String, String> parseMap(byte[] data) {
        HashMap<String, String> map = new HashMap<String, String>();
        String[] arr = StringUtil.utf8ByteToStr(data).split(" *\r*\n");
        for (String item : arr) {
            String[] kv = item.split(" *= *", 2);
            if (kv.length == 2) {
                map.put(kv[0], kv[1]);
            }
        }
        return map;
    }

    /**
     * The inner class to really watch node data changes and invoke listener.
     * This is for inner use only. Please do not use this class manually.
     *
     * @author Xie, Jiyun
     */
    public class DataWatcher implements RecoverableWatcher {
        private ConcurrentHashMap<String, MetaData> map;
        private byte[] before;

        /**
         * Create a DataWatcher.
         *
         * @param map
         */
        public DataWatcher(ConcurrentHashMap<String, MetaData> map) {
            super();
            this.map = map;
        }

        /**
         * Update the before data for latter to compare.
         *
         * @param b the before data
         */
        public void setData(byte[] b) {
            before = b;
        }

        /**
         * Parse the meta data of this client.
         *
         * @param path the path to meta data
         * @param client the client to parse
         * @throws InterruptedException
         * @throws KeeperException
         */
        public void parseMetaData(String path, String client) throws KeeperException, InterruptedException {
            Stat st = new Stat();
            byte[] data = zk.getData(path + "/" + client, false, st);
            MetaData m = MetaData.parse(data);
            map.put(client, m);
        }

        /**
         * Compare the meta signature and update if necessary.
         *
         * @param path the path to meta data
         * @param after the after signature
         * @throws InterruptedException
         * @throws KeeperException
         */
        private void compareAndUpdate(String path, byte[] after) throws KeeperException, InterruptedException {
            if (Arrays.equals(before, after)) {
                return;
            }
            // Data updated, Let's do this.
            HashMap<String, String> beforeMap = parseMap(before);
            // Before is now update with after.
            before = after;
            HashMap<String, String> afterMap = parseMap(after);
            for (Entry<String, String> entry : afterMap.entrySet()) {
                String be = beforeMap.get(entry.getKey());
                if (be == null || !be.equals(entry.getValue())) {
                    LOG.info("Meta data changed for: {}/{}.", path, entry.getKey());
                    parseMetaData(path, entry.getKey());
                }
            }
        }

        /**
         * Override super method
         * @see org.apache.zookeeper.Watcher#process(org.apache.zookeeper.WatchedEvent)
         */
        @Override
        public void process(WatchedEvent event) {
            if (event.getType() != EventType.NodeDataChanged) {
                return;
            }
            try {
                Stat st = new Stat();
                byte[] after = zk.getData(event.getPath(), this, st);
                // Compare before with after.
                compareAndUpdate(event.getPath(), after);
            } catch (Exception e) {
                LOG.error("Failed to watch Data.", e);
            }
        }

        /**
         * Override super method
         * @see org.apache.niolex.address.core.RecoverableWatcher#reconnected(java.lang.String)
         */
        @Override
        public void reconnected(String path) {
            try {
                Stat st = new Stat();
                byte[] after = zk.getData(path, this, st);
                // Compare before with after.
                compareAndUpdate(path, after);
            } catch (Exception e) {
                LOG.error("Failed to watch Data.", e);
            }
        }

    }
}
