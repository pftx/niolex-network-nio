/**
 * Consumer.java, 2012-6-21.
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
package org.apache.niolex.address.client;

import java.io.IOException;
import java.util.List;

import org.apache.niolex.address.core.FindException;
import org.apache.niolex.address.core.RecoverableWatcher;
import org.apache.niolex.address.core.ZKConnector;
import org.apache.niolex.address.util.PathUtil;
import org.apache.niolex.commons.bean.MutableOne;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.EventType;


/**
 * This is the main class clients used to get service address list.
 * Clients can also get all states and listen to states list changes.
 *
 * 服务的路径：/<root>/services/<service>/versions/<version>/<state>/<node>
 *
 * Version的格式
 * 1. 数字
 *
 * 例如5表示版本号5,100表示版本号100.
 *
 * 2. 数字+
 *
 * 例如100+，表示取服务器最新的版本号，如果版本号小于100则报错。
 *
 * 3. 数字-数字
 *
 * 例如100-300，表示取服务器版本号在[100, 300)区间的最大的version。
 * 数字是半开半闭区间，即100是可用的版本，300是不可以的版本。
 *
 *
 * @author Xie, Jiyun
 *
 */
public class Consumer extends ZKConnector {

    /**
     * The only constructor extends super class.
     * 构造函数,继承自父类
     *
     * @param clusterAddress zookeeper集群的地址
     * @param sessionTimeout 会话超时的时间
     * @throws IOException 如果与集群建立连接失败，则会抛出异常
     */
    public Consumer(String clusterAddress, int sessionTimeout) throws IOException {
        super(clusterAddress, sessionTimeout);
    }

    /**
     * Get the int version according to the string version. We will find the greatest version for you.
     * 根据输入的字符串version获取当前最高的可用版本号。
     *
     * @param service 服务的唯一名称，例如org.apache.niolex.address.Test
     * @param version 支持3种格式，参考[version的格式]章节
     * @return 当前最高的可用版本号
     */
    public int getCurrentVersion(String service, String version) {
        // We only support three kinds of version:
        PathUtil.VersionRes res = PathUtil.validateVersion(version);
        if (!res.isValid()) {
            throw new IllegalArgumentException("Version not recognised: " + version);
        }
        if (this.root == null) {
            throw new IllegalStateException("Root not set.");
        }
        try {
            final int ver = res.getLow();
            if (res.isRange()) {
                String path = PathUtil.makeService2VersionPath(root, service);
                // Get all children.
                List<String> ls = this.zk.getChildren(path, false);
                LOG.info("Current versions for [{}] is: {}", service, ls);
                // The max version available.
                int mver = -1;
                // Parse all versions and get the maximum correct version.
                for (String s : ls) {
                    try {
                        int cv = Integer.parseInt(s);
                        if (cv > mver && cv < res.getHigh()) {
                            mver = cv;
                        }
                    } catch (Exception e) {}
                }
                if (mver < ver) {
                    throw new IllegalStateException("Version not matched: max available - {" + mver + "} range - " + version);
                }
                LOG.info("Picked version: {} for range {}.", mver, version);
                return mver;
            } else {
                return ver;
            }
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException)e;
            }
            throw FindException.makeInstance("Failed to get Current Version.", e);
        }
    }

    /**
     * Get all the states(sharding/partion) and listen to changes.
     * 获取服务的状态（分区）信息列表并且监听该信息的变化。
     * 有状态的服务（分区服务）是一种高级功能。例如，分布式缓存按照key进行hash到某一组缓存上。
     * 这时state节点例如有A\B\C\D四个状态，那么key进行hash mod 4就可以确定缓存的分组。
     *
     * 有状态的服务（分区服务）的不同状态的节点是不对等的，通过本接口可以获取这些信息。
     * 对于所有节点同构的服务，则不需要使用本接口。只需要创建一个固定的状态然后将所有的节点都放置到该状态里。
     *
     * 如果想要在状态（分区）信息发生变化的时候知晓，请在{@link org.apache.niolex.commons.bean.MutableOne}
     * 上面添加监听器。返回值里面的数据是动态更新的，您不需要再次调用本接口。
     *
     * @param service 服务的唯一名称，例如org.apache.niolex.address.Test
     * @param version 支持3种格式，参考[version的格式]章节
     * @return 当前的服务的状态信息列表；系统会监听该列表的变化，将信息变化设置到返回的MutableOne里
     * @throws FindException 当发生异常时
     */
    public MutableOne<List<String>> getAllStats(String service, String version) {
        String path = PathUtil.makeService2StatePath(root, service, this.getCurrentVersion(service, version));

        try {
            LOG.info("Watch states: [{}]", path);
            MutableOne<List<String>> ret = new MutableOne<List<String>>();
            @SuppressWarnings("unchecked")
            List<String> ls = (List<String>) this.submitWatcher(path, new NodeWatcher(ret), true);
            ret.updateData(ls);
            return ret;
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException)e;
            }
            throw FindException.makeInstance("Failed to get Stats List.", e);
        }
    }

    /**
     * Get address and listen address changes.
     * 获取服务的地址信息列表并且监听地址信息的变化。
     *
     * 如果想要在地址信息列表发生变化的时候知晓，请在{@link org.apache.niolex.commons.bean.MutableOne}
     * 上面添加监听器。返回值里面的数据是动态更新的，您不需要再次调用本接口。
     *
     * @param service 服务的唯一名称，例如org.apache.niolex.address.Test
     * @param version 支持3种格式，参考[version的格式]章节
     * @param stat 服务的状态信息，例如分区的服务则用这个表示不同的分区
     * @return 当前的服务地址信息列表；系统会监听该列表的变化，将信息变化设置到返回的MutableOne里
     * @throws FindException 当发生异常时
     */
    public MutableOne<List<String>> getAddressList(String service, String version, String stat) {
        String path = PathUtil.makeService2NodePath(root, service, getCurrentVersion(service, version), stat);
        try {
            LOG.info("watch AddressList: " + path);
            MutableOne<List<String>> ret = new MutableOne<List<String>>();
            @SuppressWarnings("unchecked")
			List<String> ls = (List<String>) this.submitWatcher(path.toString(), new NodeWatcher(ret), true);
            ret.updateData(ls);
            return ret;
        } catch (Exception e) {
        	if (e instanceof RuntimeException) {
        		throw (RuntimeException)e;
        	}
            throw FindException.makeInstance("Failed to get Address List.", e);
        }
    }

    /**
     * Get address and listen address changes.
     * 获取服务的地址信息列表并且监听地址信息的变化。
     *
     * 如果想要在地址信息列表发生变化的时候知晓，请在{@link org.apache.niolex.commons.bean.MutableOne}
     * 上面添加监听器。返回值里面的数据是动态更新的，您不需要再次调用本接口。
     *
     * @param service 服务的唯一名称，例如org.apache.niolex.address.Test
     * @param version 当前的数字版本
     * @param stat 服务的状态信息，例如分区的服务则用这个表示不同的分区
     * @return 当前的服务地址信息列表；系统会监听该列表的变化，将信息变化设置到返回的MutableOne里
     * @throws FindException 当发生异常时
     */
    public MutableOne<List<String>> getAddressList(String service, int version, String stat) {
        String path = PathUtil.makeService2NodePath(root, service, version, stat);
        try {
            LOG.info("watch AddressList: " + path);
            MutableOne<List<String>> ret = new MutableOne<List<String>>();
            @SuppressWarnings("unchecked")
            List<String> ls = (List<String>) this.submitWatcher(path.toString(), new NodeWatcher(ret), true);
            ret.updateData(ls);
            return ret;
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException)e;
            }
            throw FindException.makeInstance("Failed to get Address List.", e);
        }
    }

    /**
     * The inner class to really watch the nodes list change and invoke listener.
     * This is for inner use only. Please do not use this class manually.
     *
     * @author Xie, Jiyun
     */
    public class NodeWatcher implements RecoverableWatcher {
        private MutableOne<List<String>> ret;

        /**
         * @param ret
         */
        public NodeWatcher(MutableOne<List<String>> ret) {
            super();
            this.ret = ret;
        }

        /**
         * Override super method
         * @see org.apache.zookeeper.Watcher#process(org.apache.zookeeper.WatchedEvent)
         */
        @Override
        public void process(WatchedEvent event) {
            if (event.getType() != EventType.NodeChildrenChanged) {
                return;
            }
            try {
                List<String> ls = zk.getChildren(event.getPath(), this);
                ret.updateData(ls);
            } catch (Exception e) {
                LOG.error("Failed to watch Children.", e);
            }
        }

        /**
         * Override super method
         * @see org.apache.niolex.address.core.RecoverableWatcher#reconnected(java.lang.String)
         */
        @Override
        public void reconnected(String path) {
            try {
                List<String> ls = zk.getChildren(path, this);
                ret.updateData(ls);
            } catch (Exception e) {
                LOG.error("Failed to watch Children.", e);
            }
        }

    }

}
