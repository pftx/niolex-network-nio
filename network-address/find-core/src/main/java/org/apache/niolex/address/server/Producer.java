/**
 * Producer.java, 2012-6-21.
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
package org.apache.niolex.address.server;

import java.io.IOException;

import org.apache.niolex.address.client.Consumer;
import org.apache.niolex.address.util.PathUtil;


/**
 * This is the main class server used to publish service and get IP addresses who
 * can use this service.
 *
 * 服务的路径：/<root>/services/<service>/versions/<version>/<state>/<node>
 *
 * @author Xie, Jiyun
 */
public class Producer extends Consumer {

    /**
     * 构造函数,继承自父类
     *
     * @param clusterAddress zookeeper集群的地址
     * @param sessionTimeout 会话超时的时间
     * @throws IOException 如果与集群建立连接失败，则会抛出异常
     */
    public Producer(String clusterAddress, int sessionTimeout) throws IOException {
        super(clusterAddress, sessionTimeout);
    }

    /**
     * Publish the service address to zookeeper.
     * 将当前服务的地址信息发布到zookeeper集群。
     *
     * @param service 服务的唯一名称，例如org.apache.niolex.address.Test
     * @param version 服务的版本信息，例如100
     * @param stat 服务的状态信息，例如分区的服务则用这个表示不同的分区
     * @param address 服务的地址信息，这就是本机打算发布的信息
     * @param isTmp 是否临时节点。临时节点在服务器关闭后会自动从zookeeper集群删除，否则永久存在。
     * @throws FindException假如发布失败
     */
    public void publishService(String service, int version, String stat, String address, boolean isTmp) {
        publishService(service, version, stat, address, null, isTmp, false);
    }

    /**
     * Publish the service address to zookeeper.
     * 将当前服务的地址信息发布到zookeeper集群。
     *
     * @param service 服务的唯一名称，例如org.apache.niolex.address.Test
     * @param version 服务的版本信息，例如100
     * @param stat 服务的状态信息，例如分区的服务则用这个表示不同的分区
     * @param address 服务的地址信息，这就是本机打算发布的信息
     * @param config 服务的配置信息，例如一个key/value属性文件: disabled=true
     * @param isTmp 是否临时节点。临时节点在服务器关闭后会自动从zookeeper集群删除，否则永久存在。
     * @throws FindException假如发布失败
     */
    public void publishService(String service, int version, String stat, String address, byte[] config, boolean isTmp) {
        publishService(service, version, stat, address, config, isTmp, false);
    }

    /**
     * Publish the service address to zookeeper.
     * 将当前服务的地址信息发布到zookeeper集群。
     *
     * @param service 服务的唯一名称，例如org.apache.niolex.address.Test
     * @param version 服务的版本信息，例如100
     * @param stat 服务的状态信息，例如分区的服务则用这个表示不同的分区
     * @param address 服务的地址信息，这就是本机打算发布的信息
     * @param config 服务的配置信息，例如一个key/value属性文件: disabled=true
     * @param isTmp 是否临时节点。临时节点在服务器关闭后会自动从zookeeper集群删除，否则永久存在。
     * @param isSequential 是否自增节点。自增节点zookeeper集群会在address后面追加一个10位的自增整数，
     *         zookeeper会保证该整数在同一个目录里面不会重复。可利用该整数进行节点的外部排序。
     * @return the actual path of the created ZK node
     * @throws FindException假如发布失败
     */
    public String publishService(String service, int version, String stat, String address, byte[] config,
            boolean isTmp, boolean isSequential) {
        if (this.root == null) {
            throw new IllegalStateException("Root not set.");
        }
        if (version < 1) {
            throw new IllegalArgumentException("Version must greater than 0.");
        }
        StringBuilder path = new StringBuilder();
        path.append(PathUtil.makeService2NodePath(root, service, version, stat));
        path.append("/").append(address);
        LOG.info("Try to publish address: " + path);

        return this.createNode(path.toString(), config, isTmp, isSequential);
    }

    /**
     * Withdraw the service address from zookeeper.
     * 将当前的服务地址信息从zookeeper集群撤销。
     * 该方法用于撤销永久节点，临时节点zookeeper会自己管理。
     *
     * @param service 服务的唯一名称，例如org.apache.niolex.address.Test
     * @param version 服务的版本信息，例如100
     * @param stat 服务的状态信息，例如分区的服务则用这个表示不同的分区
     * @param address 服务的地址信息，这就是本机打算撤销的地址
     * @throws FindException假如撤销服务失败
     */
    public void withdrawService(String service, int version, String stat, String address) {
        if (this.root == null) {
            throw new IllegalStateException("Root not set.");
        }
        if (version < 1) {
            throw new IllegalArgumentException("Version must greater than 0.");
        }
        StringBuilder path = new StringBuilder();
        path.append(PathUtil.makeService2NodePath(root, service, version, stat));
        path.append("/").append(address);
        LOG.info("Try to withdraw address: " + path);
        this.deleteNode(path.toString());
    }

    /**
     * Withdraw the service address from zookeeper. This method is for sequential address,
     * which will append a 10bits integer after the address.
     *
     * 将当前的服务地址信息从zookeeper集群撤销。用于撤销增节点。
     * 自增节点zookeeper集群会在address后面追加一个10位的自增整数，无法使用withdrawService方法撤销。
     * 该方法用于撤销永久节点，临时节点zookeeper会自己管理。
     *
     * @param wholePath the actual path of the created ZK node
     * @throws FindException假如撤销服务失败
     */
    public void withdrawSequentialService(String wholePath) {
        LOG.info("Try to withdraw address: " + wholePath);
        this.deleteNode(wholePath);
    }

}
