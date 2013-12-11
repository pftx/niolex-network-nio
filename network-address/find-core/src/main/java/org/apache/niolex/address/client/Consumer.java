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

import org.apache.niolex.address.util.PathUtil;
import org.apache.niolex.commons.bean.MutableOne;
import org.apache.niolex.zookeeper.core.ZKConnector;
import org.apache.niolex.zookeeper.core.ZKException;
import org.apache.niolex.zookeeper.core.ZKListener;


/**
 * This is the main class clients used to get service address list.
 * Clients can also get all states and listen to states list changes.
 * <br>
 * The Path of One Service: "/&lt;root>/services/&lt;service&gt;/versions/&lt;version&gt;/&lt;state&gt;/&lt;node&gt;"
 * <br><pre>
 * [The style of Version]
 * 1. Number
 * The common version maybe with 3 or 4 digits separated by dot. i.e. "1.0.3.445", but we
 * don't want to do this. Our version is just one 4-bytes-int. So in order to represent
 * the common version, one may want to use this int like this:
 *       XXYYZZFFF
 *      2147483647(The Max Value)
 * XX for major version, YY for minor version, ZZ for path version, FFF for anything else.
 *
 * 2. Number+
 * i.e. 10003445+ means versions greater than "1.0.3.445"
 *
 * 3. Number-Number
 * i.e. 10003445-10005000 means versions greater than "1.0.3.445" but smaller than "1.0.5.000"
 * Please note that the end version is not included.
 *
 *
 * [Version的格式]
 * 1. 数字
 * 例如5表示版本号5,100表示版本号100.
 *
 * 2. 数字+
 * 例如100+，表示取服务器最新的版本号，如果版本号小于100则报错。
 *
 * 3. 数字-数字
 * 例如100-300，表示取服务器版本号在[100, 300)区间的最大的version。
 * 数字是半开半闭区间，即100是可用的版本，300是不可以的版本。
 * &gt;/pre>
 *
 * @author Xie, Jiyun
 *
 */
public class Consumer extends ZKConnector {

    /**
     * The current cluster root.
     */
    protected String root;

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
        return getCurrentVersionInner(service, version, 0);
    }

    /**
     * Get the int version according to the string version. We will find the greatest version for you.
     * 根据输入的字符串version获取当前最高的可用版本号。
     *
     * @param service 服务的唯一名称，例如org.apache.niolex.address.Test
     * @param version 支持3种格式，参考[version的格式]章节
     * @param type 获取version的位置，0 service，1 client
     * @return 当前最高的可用版本号
     */
    protected int getCurrentVersionInner(String service, String version, int type) {
        // We only support three kinds of version:
        PathUtil.Result res = PathUtil.validateVersion(version);
        if (!res.isValid()) {
            throw new IllegalArgumentException("Version not recognised: " + version);
        }
        if (this.root == null) {
            throw new IllegalStateException("Root not set.");
        }
        if (!res.isRange()) {
            return res.getLow();
        }
        String path = null;
        switch (type) {
            case 1:
                path = PathUtil.makeMeta2ClientPath(root, service);
                break;
            case 0:
            default:
                path = PathUtil.makeService2VersionPath(root, service);
                break;
        }
        // Get all children.
        List<String> ls = this.getChildren(path);
        LOG.info("Current versions for [{}] is: {}", path, ls);
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
        if (mver < res.getLow()) {
            throw new IllegalStateException("Version not matched: max available - {" + mver + "} range - " + version);
        }
        LOG.info("Picked version: {} for range {}.", mver, version);
        return mver;
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
     * @throws ZKException 当发生异常时
     */
    public MutableOne<List<String>> getAllStats(String service, String version) {
        String path = PathUtil.makeService2StatePath(root, service, this.getCurrentVersion(service, version));
        LOG.info("Watch states: [{}]", path);
        MutableOne<List<String>> ret = new MutableOne<List<String>>();
        ret.updateData(this.watchChildren(path, new NodeWatcher(ret)));
        return ret;
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
     * @throws ZKException 当发生异常时
     */
    public MutableOne<List<String>> getAddressList(String service, String version, String stat) {
        String path = PathUtil.makeService2NodePath(root, service, getCurrentVersion(service, version), stat);
        LOG.info("watch AddressList: " + path);
        MutableOne<List<String>> ret = new MutableOne<List<String>>();
        ret.updateData(this.watchChildren(path, new NodeWatcher(ret)));
        return ret;
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
     * @throws ZKException 当发生异常时
     */
    public MutableOne<List<String>> getAddressList(String service, int version, String stat) {
        String path = PathUtil.makeService2NodePath(root, service, version, stat);
        LOG.info("watch AddressList: " + path);
        MutableOne<List<String>> ret = new MutableOne<List<String>>();
        ret.updateData(this.watchChildren(path, new NodeWatcher(ret)));
        return ret;
    }

    /**
     * The inner class to really watch the nodes list change and invoke listener.
     * This is for inner use only. Please do not use this class manually.
     *
     * @author Xie, Jiyun
     */
    public class NodeWatcher implements ZKListener {
        private MutableOne<List<String>> ret;

        /**
         * @param ret
         */
        public NodeWatcher(MutableOne<List<String>> ret) {
            super();
            this.ret = ret;
        }

        /**
         * This is the override of super method.
         * @see org.apache.niolex.zookeeper.core.ZKListener#onDataChange(byte[])
         */
        @Override
        public void onDataChange(byte[] data) {
            // This method is not used.
        }

        /**
         * This is the override of super method.
         * @see org.apache.niolex.zookeeper.core.ZKListener#onChildrenChange(java.util.List)
         */
        @Override
        public void onChildrenChange(List<String> list) {
            ret.updateData(list);
        }

    }

    /**
     * @return the root
     */
    public String getRoot() {
        return root;
    }

    /**
     * 设置当前使用的root。
     * root是用来区分不同地址状态的，例如online表示线上服务，test表示线下测试服务
     * @param root
     *            the root to set
     */
    public void setRoot(String root) {
        if (root.charAt(0) != '/') {
            this.root = '/' + root;
        } else {
            this.root = root;
        }
    }

}
