/**
 * ZKOperator.java
 *
 * Copyright 2013 the original author or authors.
 *
 * We licenses this file to you under the Apache License, version 2.0
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
package org.apache.niolex.address.ext;

import static org.apache.niolex.address.util.ACLUtil.*;
import static org.apache.niolex.address.util.PathUtil.*;

import java.io.IOException;
import java.util.List;

import org.apache.niolex.zookeeper.core.ZKException;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

/**
 * This class manages all the nodes in the zookeeper server.
 * It's useful if user want to write programs to manipulate our
 * find storage.
 * <pre>
 * The whole find storage tree is like this:
 * /root =>
 *      /operators store all the operators accounts
 *      /servers store all the servers accounts
 *      /clients store all the clients accounts
 *      /services store all the data of services =>
 *              /service-name =>
 *                      /versions store all the versions of this service =>
 *                              /version-number =>
 *                                      /state-label =>
 *                                              /server-addresses
 *                      /clients store all the clients meta data of this service
 *                              /version-number =>
 *                                      /client-name
 * This class is operating on this tree.
 * </pre>
 *
 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2013-12-11
 */
public class ZKOperator extends AdvancedProducer {

    /**
     * Construct a new ZKOperator and connect to ZK server.
     * We will wait until get connected in this method.
     *
     * @param clusterAddress the zookeeper cluster servers address list
     * @param sessionTimeout the session timeout in microseconds
     * @throws IOException in cases of network failure
     * @throws IllegalArgumentException if sessionTimeout is too small
     */
    public ZKOperator(String clusterAddress, int sessionTimeout) throws IOException {
        super(clusterAddress, sessionTimeout);
    }

    /**
     * Create node with the specified ACL.
     *
     * @param path the node path
     * @param acl the access control list
     * @throws ZKException if failed to create node
     */
    public void createNode(String path, List<ACL> acl) {
        createNode(path, null, acl);
    }

    /**
     * Create node with the specified data and ACL.
     *
     * @param path the node path
     * @param data the node data
     * @param acl the access control list
     * @throws ZKException if failed to create node
     */
    public void createNode(String path, byte[] data, List<ACL> acl) {
        try {
            zk.create(path, data, acl, CreateMode.PERSISTENT);
        } catch (Exception e) {
            throw ZKException.makeInstance("Failed to create Node.", e);
        }
    }

    /**
     * Get the ACL list of the specified path.
     *
     * @param path the node path
     * @return the ACL list
     */
    public List<ACL> getACL(String path) {
        try {
            return zk.getACL(path, new Stat());
        } catch (Exception e) {
            throw ZKException.makeInstance("Failed to get ACL.", e);
        }
    }

    public void addACL(String path, List<ACL> acl) {
        try {
            // Add ACL in a while loop to ensure current add will end with
            // expected result.
            while (true) {
                Stat stat = new Stat();
                List<ACL> old = zk.getACL(path, stat);
                try {
                    zk.setACL(path, mergeACL(old, acl), stat.getAversion());
                } catch (KeeperException e) {
                    if (e.code() != KeeperException.Code.BADVERSION) {
                        throw e;
                    }
                }
            }
        } catch (Exception e) {
            throw ZKException.makeInstance("Failed to add ACL.", e);
        }
    }

    public void addACLTree(String path, List<ACL> acl) {
        ;
    }

    /**
     * Init a new tree to store all the data of find service.
     *
     * @param rootName the root user name
     * @param rootPasswd the root password
     * @return true if init success, false if the root path already exists
     */
    public boolean initTree(String rootName, String rootPasswd) {
        if (this.root == null) {
            throw new IllegalStateException("Root not set.");
        }
        if (exists(this.root)) {
            return false;
        }
        List<ACL> acl = getAllRights(rootName, rootPasswd);
        createNode(root, acl);
        createNode(makeOpPath(root), acl);
        createNode(makeOpPath(root, rootName), acl);
        createNode(makeServerPath(root), acl);
        createNode(makeClientPath(root), acl);
        createNode(makeServicePath(root), acl);
        return true;
    }

    public boolean addOperator(String opName, String opPasswd) {
        if (this.root == null) {
            throw new IllegalStateException("Root not set.");
        }
        String path = makeOpPath(root, opName);
        if (exists(path)) {
            return false;
        }
        // 1. Create this User.
        List<ACL> acl = getAllRights(opName, opPasswd);
        createNode(path, acl);
        // 2. Add ACL to all services
        addACLTree(makeServicePath(root), acl);
        // 3. Add ACL to server and client root. We don't give the ADMIN right.
        acl = getCRDRights(opName, opPasswd);
        addACL(makeServerPath(root), acl);
        addACL(makeClientPath(root), acl);
        return true;
    }

    public boolean addServer(String serverName, String serverPasswd) {
        if (this.root == null) {
            throw new IllegalStateException("Root not set.");
        }
        String path = makeServerPath(root, serverName);
        if (exists(path)) {
            return false;
        }
        // 1. Create this User.
        List<ACL> acl = getAllRights(serverName, serverPasswd);
        createNode(path, acl);
        return true;
    }

    public boolean addClient(String clientName, String clientPasswd) {
        if (this.root == null) {
            throw new IllegalStateException("Root not set.");
        }
        String path = makeClientPath(root, clientName);
        if (exists(path)) {
            return false;
        }
        // 1. Create this User.
        List<ACL> acl = getAllRights(clientName, clientPasswd);
        createNode(path, acl);
        return true;
    }

    public boolean addService(String service) {
        if (this.root == null) {
            throw new IllegalStateException("Root not set.");
        }
        String path = makeServicePath(root, service);
        if (exists(path)) {
            return false;
        }
        List<ACL> acl = getACL(makeServicePath(root));
        createNode(path, acl);
        createNode(makeService2VersionPath(root, service), acl);
        createNode(makeMeta2ClientPath(path, service), acl);
        return true;
    }

}
