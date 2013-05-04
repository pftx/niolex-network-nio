/**
 * ZKConnector.java
 *
 * Copyright 2012 Niolex, Inc.
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
package org.apache.niolex.address.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.apache.niolex.commons.codec.StringUtil;
import org.apache.niolex.commons.util.SystemUtil;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The main Zookeeper connector, manage zookeeper and retry connection.
 *
 * @author Xie, Jiyun
 * @version 1.0.0, Date: 2012-6-10
 */
public class ZKConnector {

    protected static final Logger LOG = LoggerFactory.getLogger(ZKConnector.class);

    /**
     * The ZK cluster address.
     */
    private final String clusterAddress;

    /**
     * The watcher list. Every watcher will be recreated when reconnect to ZK.
     */
    private final List<WatcherItem> watcherList = Collections.synchronizedList(new ArrayList<WatcherItem>());

    /**
     * The map key will be the real path in ZK. It maybe different from the path in the linked CreateItem,
     * which is the original path user want to create.
     */
    private final Map<String, CreateItem> createItemMap = Collections.synchronizedMap(new HashMap<String, CreateItem>());

    private final int sessionTimeout;

    private byte[] auth;

    protected String root;

    protected ZooKeeper zk;

    /**
     * Construct a new ZKConnector and connect to ZK server.
     *
     * @param clusterAddress
     * @param sessionTimeout
     * @throws IOException
     */
    public ZKConnector(String clusterAddress, int sessionTimeout) throws IOException {
        super();
        this.clusterAddress = clusterAddress;
        this.sessionTimeout = sessionTimeout;
        if (sessionTimeout < 5000) {
            throw new IllegalArgumentException("sessionTimeout too small.");
        }
        connectToZookeeper();
    }

    /**
     * Make a connection to zookeeper, and wait until connected.
     *
     * @throws IOException
     */
    private void connectToZookeeper() throws IOException {
        // Use this to sync for connected event.
        CountDownLatch latch = new CountDownLatch(1);
        this.zk = new ZooKeeper(clusterAddress, sessionTimeout, new ZKConnWatcher(this, latch));
        waitForConnectedTillDeath(latch);
    }

    /**
     * Wait for zookeeper to be connected, if can not connect, wait forever.
     *
     * @param latch
     */
    private void waitForConnectedTillDeath(CountDownLatch latch) {
        while (true) {
            try {
                latch.await();
                return;
            } catch (InterruptedException e) {}
        }
    }

    /**
     * Add authenticate info for this client.
     * 添加client的权限认证信息
     *
     * @param username
     * @param password
     */
    public void addAuthInfo(String username, String password) {
        auth = StringUtil.strToUtf8Byte(username + ":" + password);
        this.zk.addAuthInfo("digest", auth);
    }

    /**
     * Close the connection to ZK server.
     * 注意！一但关闭连接，请立即丢弃该对象，该对象的所有的方法的结果将不确定
     */
    public void close() {
        try {
            this.zk.close();
        } catch (Exception e) {
            LOG.info("Failed to close ZK connection.", e);
        }
    }

    /**
     * Try to reconnect to zookeeper cluster.
     */
    protected void reconnect() {
        while (true) {
            try {
                connectToZookeeper();
                if (auth != null) {
                    this.zk.addAuthInfo("digest", auth);
                }
                // Do create all the temporary node.
                synchronized (createItemMap) {
                    Map<String,CreateItem> map = new HashMap<String, CreateItem>();
                    for (CreateItem item : createItemMap.values()) {
                        try {
                            String p = doCreateNode(item.getPath(), item.getData(), item.getMode());
                            map.put(p, item);
                        } catch (Exception e) {
                            LOG.error("Failed to create path: {}, data: {}.", item.getPath(),
                                    new String(item.getData()), e);
                        }
                    }
                    if (map.size() == createItemMap.size()) {
                        // Recreate all temporary node OK. So we need to update the map.
                        createItemMap.clear();
                        createItemMap.putAll(map);
                    } else {
                        LOG.error("Not all temp node re-created, expected {}, real created {}. Please check!!!",
                                createItemMap.size(), map.size());
                    }
                }
                // Re add all the watcher.
                synchronized (watcherList) {
                    for (WatcherItem item : watcherList) {
                        item.getWat().reconnected(item.getPath());
                    }
                }
                break;
            } catch (Exception e) {
                // We don't care, we will retry again and again.
                LOG.error("Error occured when reconnect, system will retry.", e);
                SystemUtil.sleep(sessionTimeout / 3);
            }
        }
    }

    /**
     * Attach a watcher to the path you want to watch.
     *
     * @param path
     * @param wat
     * @param isChildren
     * @return the current data
     */
    protected Object submitWatcher(String path, RecoverableWatcher wat, boolean isChildren) {
        WatcherItem item = new WatcherItem(path, wat, isChildren);
        Object r = doWatch(item);
        // Add this item to the list, so the system will
        // add them after reconnected.
        watcherList.add(item);
        return r;
    }

    /**
     * Do real watch. Please use submitWatcher instead. This method is for internal use.
     *
     * @param item the item to do watch
     * @return the current data
     */
    private Object doWatch(WatcherItem item) {
        try {
            if (item.isChildren()) {
                return this.zk.getChildren(item.getPath(), item.getWat());
            } else {
                Stat st = new Stat();
                return this.zk.getData(item.getPath(), item.getWat(), st);
            }
        } catch (Exception e) {
            throw FindException.makeInstance("Failed to do Watch.", e);
        }
    }

    /**
     * Get the ZNode data of the specified path.
     *
     * @param path the path to get
     * @return the ZNode data
     */
    public byte[] getData(String path) {
        try {
            Stat st = new Stat();
            return this.zk.getData(path, false, st);
        } catch (Exception e) {
            throw FindException.makeInstance("Failed to get Data.", e);
        }
    }

    /**
     * Get the children of the specified path.
     *
     * @param path the path to get
     * @return the children list
     */
    public List<String> getChildren(String path) {
        try {
            return this.zk.getChildren(path, false);
        } catch (Exception e) {
            throw FindException.makeInstance("Failed to get Children.", e);
        }
    }

    /**
     * Create node and add request to local map if it is a temporary node.
     * Temporary node will be recreated when reconnected to zookeeper.
     *
     * @param path
     * @param isTmp
     * @throws FindException
     */
    protected void createNode(String path, boolean isTmp) {
        createNode(path, null, isTmp, false);
    }

    /**
     * Create node and add request to local map if it is a temporary node.
     * Temporary node will be recreated when reconnected to zookeeper.
     *
     * @param path
     * @param data
     * @param isTmp
     * @param isSequential
     * @return the actual path of the created node
     * @throws FindException
     */
    protected String createNode(String path, byte[] data, boolean isTmp, boolean isSequential) {
        try {
            CreateMode createMode = null;
            if (isTmp) {
                if (isSequential) {
                    //临时自增
                    createMode = CreateMode.EPHEMERAL_SEQUENTIAL;
                } else {
                    //临时固定
                    createMode = CreateMode.EPHEMERAL;
                }
            } else {
                if (isSequential) {
                    //永久自增
                    createMode = CreateMode.PERSISTENT_SEQUENTIAL;
                } else {
                    //永久固定
                    createMode = CreateMode.PERSISTENT;
                }
            }
            String s = doCreateNode(path, data, createMode);
            if (isTmp) {
                final CreateItem item = new CreateItem(path, data, createMode);
                createItemMap.put(s, item);
            }
            return s;
        } catch (Exception e) {
            throw FindException.makeInstance("Failed to create Node.", e);
        }
    }

    /**
     * Do create a new ZK node.
     *
     * @param path
     * @param data
     * @param createMode
     * @return the actual path of the created node
     * @throws KeeperException
     * @throws InterruptedException
     */
    protected String doCreateNode(String path, byte[] data, CreateMode createMode)
            throws KeeperException, InterruptedException {
        return zk.create(path, data, Ids.OPEN_ACL_UNSAFE, createMode);
    }

    /**
     * update data of a node and update the local cache.
     *
     * @param path
     * @param data
     * @throws FindException
     */
    public void updateNode(String path, byte[] data) {
        try {
            zk.setData(path, data, -1);
            CreateItem item = createItemMap.get(path);
            if (item != null) {
                item.setData(data);
            }
        } catch (Exception e) {
            throw FindException.makeInstance("Failed to update Node data.", e);
        }
    }

    /**
     * Delete a node from zookeeper.
     * This is very important, so we only open this method for subclasses.
     *
     * @param path
     * @return true if deleted, or we will throw an exception.
     * @throws FindException
     */
    protected boolean deleteNode(String path) {
        try {
            zk.delete(path, -1);
            createItemMap.remove(path);
            return true;
        } catch (Exception e) {
            throw FindException.makeInstance("Failed to delete Node.", e);
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
