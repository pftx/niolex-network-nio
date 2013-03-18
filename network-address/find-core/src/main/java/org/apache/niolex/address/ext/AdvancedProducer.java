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

import org.apache.niolex.address.core.FindException;
import org.apache.niolex.address.core.RecoverableWatcher;
import org.apache.niolex.address.server.Producer;
import org.apache.niolex.address.util.PathUtil;
import org.apache.niolex.commons.bean.MutableOne;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.data.Stat;

/**
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
     * 获取存储在version节点下的元数据并监听他的变化。
     *
     * 如果想要在元数据信息发生变化的时候知晓，请在{@link org.apache.niolex.commons.bean.MutableOne}
     * 上面添加监听器。返回值里面的数据是动态更新的，您不需要再次调用本接口。
     *
     * @param service 服务的唯一名称，例如com.Niolex.ad.find
     * @param version 支持3种格式，参考[version的格式]章节
     * @return 当前的元数据；系统会监听该元数据的变化，将信息变化设置到返回的MutableOne里
     * @throws FindException 当发生异常时
     */
    public MutableOne<byte[]> getMetaData(String service, String version) {
        String path = PathUtil.makeService2StatePath(root, service, this.getCurrentVersion(service, version));
        try {
            LOG.info("Try to watch data of: {}.", path);
            MutableOne<byte[]> ret = new MutableOne<byte[]>();
            byte[] b = (byte[]) this.submitWatcher(path, new DataWatcher(ret), false);
            ret.updateData(b);
            return ret;
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException)e;
            }
            throw FindException.makeInstance("Failed to get meta data.", e);
        }
    }

    /**
     * Get the meta data of this service version, including IP list etc.
     * This is a wrap of method: {@link #getMetaData(String, String)}
     *
     * 获取元数据的Java表现版本。本方法是对{@link #getMetaData(String, String)}方法的封装。
     *
     * @param service 服务的唯一名称，例如com.Niolex.ad.find
     * @param version 服务的版本信息，例如100
     * @return 元数据的可变表达方式；系统会监听该信息的变化，将信息变化设置到返回的MutableOne里
     * @throws FindException 当发生异常时
     */
    public MutableOne<MetaData> getMetaData(String service, int version) {
        if (version < 1) {
            throw new IllegalArgumentException("Version must greater than 0.");
        }
        MutableOne<byte[]> rawData = getMetaData(service, "" + version);
        return MetaData.wrap(rawData);
    }


    /**
     * The inner class to really watch node data changes and invoke listener.
     * This is for inner use only. Please do not use this class manually.
     *
     * @author Xie, Jiyun
     */
    public class DataWatcher implements RecoverableWatcher {
        private MutableOne<byte[]> listener;

        /**
         * @param listener
         */
        public DataWatcher(MutableOne<byte[]> listener) {
            super();
            this.listener = listener;
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
                byte[] ls = zk.getData(event.getPath(), this, st);
                listener.updateData(ls);
            } catch (Exception e) {
                LOG.error("Failed to watch Data.", e);
            }
        }

        /**
         * Override super method
         * @see org.apache.niolex.find.core.RecoverableWatcher#reconnected(java.lang.String)
         */
        @Override
        public void reconnected(String path) {
            try {
                Stat st = new Stat();
                byte[] ls = zk.getData(path, this, st);
                listener.updateData(ls);
            } catch (Exception e) {
                LOG.error("Failed to watch Data.", e);
            }

        }

    }
}
