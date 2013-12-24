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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.niolex.zookeeper.core.ZKException;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.BadVersionException;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
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

    /////////////////////////////////////////////////////////////////////////////
    // BASIC OPERATIONS
    /////////////////////////////////////////////////////////////////////////////

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

    /**
     * Set the specified ACL list into the ZK path.
     *
     * @param path the node path
     * @param acl the ACL list
     */
    public void setACL(String path, List<ACL> acl) {
        try {
            zk.setACL(path, acl, -1);
        } catch (Exception e) {
            throw ZKException.makeInstance("Failed to set ACL.", e);
        }
    }

    /**
     * Add the specified ACL list into the ZK path.
     *
     * @param path the node path
     * @param acl the ACL list
     */
    public void addACL(String path, List<ACL> acl) {
        try {
            // Add ACL in a while loop to ensure concurrent add will end with
            // expected result.
            while (true) {
                Stat stat = new Stat();
                List<ACL> old = zk.getACL(path, stat);
                try {
                    zk.setACL(path, mergeACL(old, acl), stat.getAversion());
                    break;
                } catch (BadVersionException e) {}
            }
        } catch (Exception e) {
            throw ZKException.makeInstance("Failed to add ACL.", e);
        }
    }

    /**
     * Add the specified ACL list to the subtree of the specified path.
     *
     * @param path the root of subtree
     * @param acl the ACL list
     */
    public void addACLTree(String path, List<ACL> acl) {
        addACL(path, acl);
        for (String end : getChildren(path)) {
            addACLTree(path + "/" + end, acl);
        }
    }

    /**
     * Remove all the ACLs of this specified Id from this path.
     *
     * @param path the node path
     * @param id the id to be removed
     */
    public void removeACL(String path, Id id) {
        try {
            // Remove ACL in a while loop to ensure concurrent remove will end with
            // expected result.
            while (true) {
                Stat stat = new Stat();
                List<ACL> old = zk.getACL(path, stat);
                try {
                    zk.setACL(path, removeId(old, id), stat.getAversion());
                    break;
                } catch (BadVersionException e) {}
            }
        } catch (Exception e) {
            throw ZKException.makeInstance("Failed to remove ACL.", e);
        }
    }

    /**
     * Remove all the ACLs of this specified Id from the subtree of the
     * specified path.
     *
     * @param path the root of subtree
     * @param id the id to be removed
     */
    public void removeACLTree(String path, Id id) {
        removeACL(path, id);
        for (String end : getChildren(path)) {
            removeACLTree(path + "/" + end, id);
        }
    }

    /////////////////////////////////////////////////////////////////////////////
    // LOGIN OPERATIONS
    /////////////////////////////////////////////////////////////////////////////

    /**
     * Login as an operator.
     *
     * @param username the user name
     * @param password the password
     * @return true if success, false otherwise
     */
    public boolean loginOp(String username, String password) {
        String path = makeOpPath(root, username);
        addAuthInfo(username, password);
        try {
            getData(path);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Login as a server.
     *
     * @param username the user name
     * @param password the password
     * @return true if success, false otherwise
     */
    public boolean loginServer(String username, String password) {
        String path = makeServerPath(root, username);
        addAuthInfo(username, password);
        try {
            getData(path);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Login as a client.
     *
     * @param username the user name
     * @param password the password
     * @return true if success, false otherwise
     */
    public boolean loginClient(String username, String password) {
        String path = makeClientPath(root, username);
        addAuthInfo(username, password);
        try {
            getData(path);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get the super user's name.
     *
     * @return the super user name
     */
    public String getSuperUser() {
        List<ACL> acl = getACL(root);
        return getUserName(acl.get(0).getId());
    }

    /////////////////////////////////////////////////////////////////////////////
    // USER OPERATIONS
    /////////////////////////////////////////////////////////////////////////////

    /**
     * Add a new operator into find service.
     *
     * @param opName the operator name
     * @param opPasswd the operator password
     * @return true if success, false if this operator already exists
     */
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
        // 3. Add ACL to server and client root. We don't give the ADMIN right,
        // It's reserved for root.
        acl = getCRDRights(opName, opPasswd);
        addACL(makeServerPath(root), acl);
        addACL(makeClientPath(root), acl);
        return true;
    }

    /**
     * Add a new server account into find service.
     *
     * @param serverName the server account name
     * @param serverPasswd the server account password
     * @return true if success, false if this server account already exists
     */
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

    /**
     * Add a new client account into find service.
     *
     * @param clientName the client account name
     * @param clientPasswd the client account password
     * @return true if success, false if this client account already exists
     */
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

    /**
     * Get server ACL list by server account name.
     *
     * @param serverName the server account name
     * @return the server ACL list
     */
    public List<ACL> getServerACL(String serverName) {
        String serverPath = makeServerPath(root, serverName);
        if (!exists(serverPath)) {
            throw new IllegalArgumentException("server account not found.");
        }
        return getACL(serverPath);
    }

    /**
     * Get client ACL list by client account name.
     *
     * @param clientName the client account name
     * @return the client ACL list
     */
    public List<ACL> getClientACL(String clientName) {
        String clientPath = makeClientPath(root, clientName);
        if (!exists(clientPath)) {
            throw new IllegalArgumentException("client account not found.");
        }
        return getACL(clientPath);
    }

    /////////////////////////////////////////////////////////////////////////////
    // COMPOSITE OPERATIONS
    /////////////////////////////////////////////////////////////////////////////

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

    /**
     * Add a new service into find service storage.
     *
     * @param service the new service name
     * @return true if success, false if this service already exists
     */
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
        createNode(makeMeta2ClientPath(root, service), acl);
        return true;
    }

    /**
     * Initialize the service tree with the specified version and states.
     *
     * @param service the service name
     * @param version the version number
     * @param states the states list
     * @return true if success, no false
     */
    public boolean initServiceTree(String service, int version, String[] states) {
        if (this.root == null) {
            throw new IllegalStateException("Root not set.");
        }
        if (!exists(makeServicePath(root, service))) {
            addService(service);
        }
        addVersion(service, version);
        addMetaVersion(service, version);
        List<ACL> acl = getACL(makeService2VersionPath(root, service));
        for (String state : states) {
            createNode(makeService2NodePath(root, service, version, state), acl);
        }
        return true;
    }

    /**
     * Add a new version into this service.
     *
     * @param service the service name
     * @param version the new version number
     * @return true if success, false if this service not found
     */
    public boolean addVersion(String service, int version) {
        if (this.root == null) {
            throw new IllegalStateException("Root not set.");
        }
        String path = makeService2VersionPath(root, service);
        if (!exists(path)) {
            return false;
        }
        List<ACL> acl = getACL(path);
        createNode(makeService2StatePath(root, service, version), acl);
        return true;
    }

    /**
     * Add a new meta data version into this service.
     *
     * @param service the service name
     * @param version the new version number
     * @return true if success, false if this service not found
     */
    public boolean addMetaVersion(String service, int version) {
        if (this.root == null) {
            throw new IllegalStateException("Root not set.");
        }
        String path = makeMeta2ClientPath(root, service);
        if (!exists(path)) {
            return false;
        }
        List<ACL> acl = getACL(path);
        createNode(makeMeta2VersionPath(root, service, version), acl);
        return true;
    }

    /**
     * Copy all the states and related ACL from one version to another version.
     *
     * @param service the service name
     * @param fromVersion the from version number
     * @param toVersion the to version number
     * @return true if success, false if this service or version not found
     */
    public boolean copyVersion(String service, int fromVersion, int toVersion) {
        if (this.root == null) {
            throw new IllegalStateException("Root not set.");
        }
        String path = makeService2StatePath(root, service, fromVersion);
        if (!exists(path)) {
            return false;
        }
        // Make service version tree.
        createNode(makeService2StatePath(root, service, toVersion), getACL(path));
        for (String state : getChildren(path)) {
            createNode(makeService2NodePath(root, service, toVersion, state),
                    getACL(makeService2NodePath(root, service, fromVersion, state)));
        }
        path = makeMeta2VersionPath(root, service, fromVersion);
        if (!exists(path)) {
            return true;
        }
        // Make client version tree.
        createNode(makeMeta2VersionPath(root, service, toVersion),
                getData(path), getACL(path));
        for (String client : getChildren(path)) {
            String fromPath = makeMeta2NodePath(root, service, fromVersion, client);
            createNode(makeMeta2NodePath(root, service, toVersion, client),
                    getData(fromPath), getACL(fromPath));
        }
        return true;
    }

    /**
     * Add a new state to the specified service and version.
     *
     * @param service the service name
     * @param version the version number
     * @param state the new state label
     * @return true if success, false if already exists
     */
    public boolean addState(String service, int version, String state) {
        if (this.root == null) {
            throw new IllegalStateException("Root not set.");
        }
        String path = makeService2NodePath(root, service, version, state);
        if (exists(path)) {
            return false;
        }
        createNode(path, getACL(makeService2StatePath(root, service, version)));
        return true;
    }

    /////////////////////////////////////////////////////////////////////////////
    // AUTHORIZATION OPERATIONS
    /////////////////////////////////////////////////////////////////////////////

    /**
     * Add server create read delete rights to all the states nodes of this version.
     *
     * @param serverName the server account name
     * @param service the service name
     * @param version the version number
     * @return true if success, false otherwise
     * @throws IllegalArgumentException if server account not found
     */
    public boolean addServerAuth(String serverName, String service, int version) {
        if (this.root == null) {
            throw new IllegalStateException("Root not set.");
        }
        String path = makeService2StatePath(root, service, version);
        if (!exists(path)) {
            return false;
        }
        List<String> list = getChildren(path);
        for (String state : list) {
            addServerAuth(serverName, service, version, state);
        }
        return true;
    }


    /**
     * Add server read right to all the meta data nodes of this version.
     *
     * @param serverName the server account name
     * @param service the service name
     * @param version the version number
     * @return true if success, false otherwise
     * @throws IllegalArgumentException if server account not found
     */
    public boolean addServerMetaAuth(String serverName, String service, int version) {
        if (this.root == null) {
            throw new IllegalStateException("Root not set.");
        }
        String path = makeMeta2VersionPath(root, service, version);
        if (!exists(path)) {
            return false;
        }
        List<ACL> acl = getServerACL(serverName);
        acl = getReadRight(serverName, acl);
        addACLTree(path, acl);
        return true;
    }

    /**
     * Add server create read delete rights to the specified state.
     *
     * @param serverName the server account name
     * @param service the service name
     * @param version the version number
     * @param state the state label
     * @return true if success, false otherwise
     * @throws IllegalArgumentException if server account not found
     */
    public boolean addServerAuth(String serverName, String service, int version, String state) {
        if (this.root == null) {
            throw new IllegalStateException("Root not set.");
        }
        String path = makeService2NodePath(root, service, version, state);
        if (!exists(path)) {
            return false;
        }
        List<ACL> acl = getServerACL(serverName);
        acl = getCRDRights(serverName, acl);
        addACL(path, acl);
        return true;
    }

    /**
     * Add client read rights to all the versions of the specified service.
     *
     * @param clientName the client account name
     * @param service the service name
     * @return true if success, false otherwise
     * @throws IllegalArgumentException if client account not found
     */
    public boolean addClientAuth(String clientName, String service) {
        if (this.root == null) {
            throw new IllegalStateException("Root not set.");
        }
        String path = makeService2VersionPath(root, service);
        if (!exists(path)) {
            return false;
        }
        List<ACL> acl = getClientACL(clientName);
        acl = getReadRight(clientName, acl);
        addACLTree(path, acl);
        return true;
    }

    /**
     * Add client read rights to the specified version.
     *
     * @param clientName the client account name
     * @param service the service name
     * @param version the version number
     * @return true if success, false otherwise
     * @throws IllegalArgumentException if client account not found
     */
    public boolean addClientAuth(String clientName, String service, int version) {
        if (this.root == null) {
            throw new IllegalStateException("Root not set.");
        }
        String path = makeService2StatePath(root, service, version);
        if (!exists(path)) {
            return false;
        }
        List<ACL> acl = getClientACL(clientName);
        acl = getReadRight(clientName, acl);
        addACLTree(path, acl);
        return true;
    }

    /**
     * Remove server authorization from this version.
     *
     * @param serverName the server account name
     * @param service the service name
     * @param version the version number
     * @return true if removed, false if version not found
     */
    public boolean removeServerAuth(String serverName, String service, int version) {
        if (this.root == null) {
            throw new IllegalStateException("Root not set.");
        }
        String path = makeService2StatePath(root, service, version);
        if (!exists(path)) {
            return false;
        }
        List<String> list = getChildren(path);
        for (String state : list) {
            removeServerAuth(serverName, service, version, state);
        }
        return true;
    }

    /**
     * Remove server authorization from the meta nodes of this version.
     *
     * @param serverName the server account name
     * @param service the service name
     * @param version the version number
     * @return true if removed, false if version not found
     */
    public boolean removeServerMetaAuth(String serverName, String service, int version) {
        if (this.root == null) {
            throw new IllegalStateException("Root not set.");
        }
        String path = makeMeta2VersionPath(root, service, version);
        if (!exists(path)) {
            return false;
        }
        List<ACL> acl = getServerACL(serverName);
        removeACLTree(path, acl.get(0).getId());
        return true;
    }

    /**
     * Remove server authorization from this state.
     *
     * @param serverName the server account name
     * @param service the service name
     * @param version the version number
     * @param state the state
     * @return true if removed, false if state not found
     */
    public boolean removeServerAuth(String serverName, String service, int version, String state) {
        if (this.root == null) {
            throw new IllegalStateException("Root not set.");
        }
        String path = makeService2NodePath(root, service, version, state);
        if (!exists(path)) {
            return false;
        }
        List<ACL> acl = getServerACL(serverName);
        removeACLTree(path, acl.get(0).getId());
        return true;
    }

    /**
     * Remove client authorization from this service.
     *
     * @param clientName the client name
     * @param service the service name
     * @return true if removed, false if service not found
     */
    public boolean removeClientAuth(String clientName, String service) {
        if (this.root == null) {
            throw new IllegalStateException("Root not set.");
        }
        String path = makeService2VersionPath(root, service);
        if (!exists(path)) {
            return false;
        }
        List<ACL> acl = getClientACL(clientName);
        removeACLTree(path, acl.get(0).getId());
        return true;
    }

    /**
     * Remove client authorization from the specified version of this service.
     *
     * @param clientName the client name
     * @param service the service name
     * @param version the version number
     * @return true if removed, false if version not found
     */
    public boolean removeClientAuth(String clientName, String service, int version) {
        if (this.root == null) {
            throw new IllegalStateException("Root not set.");
        }
        String path = makeService2StatePath(root, service, version);
        if (!exists(path)) {
            return false;
        }
        List<ACL> acl = getClientACL(clientName);
        removeACLTree(path, acl.get(0).getId());
        return true;
    }

    /////////////////////////////////////////////////////////////////////////////
    // META OPERATIONS
    /////////////////////////////////////////////////////////////////////////////

    /**
     * Get all the meta data of this client.
     *
     * @param clientName the client name
     * @param service the service name
     * @param version the version number
     * @return the meta data map, or null if not found
     */
    public Map<String, String> getMetaData(String clientName, String service, int version) {
        String node = makeMeta2NodePath(root, service, version, clientName);
        if (!exists(node)) {
            return null;
        } else {
            return parseMap(getData(node));
        }
    }

    /**
     * Update the meta data of this client: add or replace the value of the specified key.
     *
     * @param clientName the client name
     * @param service the service name
     * @param version the version number
     * @param key the key of the meta data
     * @param value the new value
     * @return true if update success, false otherwise
     */
    public boolean updateMetaData(String clientName, String service, int version,
            String key, String value) {
        Map<String, String> map = getMetaData(clientName, service, version);
        if (map == null) {
            map = new HashMap<String, String>();
        }
        map.put(key, value);
        byte[] newData = toByteArray(map);
        return updateMetaData(clientName, service, version, newData);
    }

    /**
     * Update the meta data of this client to this new data.
     *
     * @param clientName the client name
     * @param service the service name
     * @param version the version number
     * @param newData the new meta data
     * @return true if update success, false otherwise
     */
    public boolean updateMetaData(String clientName, String service, int version, byte[] newData) {
        String path = makeMeta2VersionPath(root, service, version);
        if (!exists(path)) {
            return false;
        }
        String node = makeMeta2NodePath(root, service, version, clientName);
        if (!exists(node)) {
            createNode(node, newData);
        } else {
            updateNodeData(node, newData);
        }
        updateClientTrigger(path, clientName);
        return true;
    }

    /**
     * Update the client trigger in the version node.
     *
     * @param path the trigger path
     * @param clientName the client name
     */
    public void updateClientTrigger(String path, String clientName) {
        try {
            // Update data in a while loop to ensure concurrent update will end with
            // expected result.
            while (true) {
                Stat stat = new Stat();
                byte[] oldData = zk.getData(path, false, stat);
                HashMap<String, String> map = oldData == null ?
                        new HashMap<String, String>() : parseMap(oldData);
                String value = map.get(clientName);
                if (value != null) {
                    try {
                        int i = Integer.parseInt(value);
                        value = Integer.toString(i + 1);
                    } catch (Exception e) {
                        value = "6";
                    }
                } else {
                    value = "1";
                }
                map.put(clientName, value);
                byte[] newData = toByteArray(map);
                try {
                    zk.setData(path, newData, stat.getVersion());
                    break;
                } catch (BadVersionException e) {}
            }
        } catch (Exception e) {
            throw ZKException.makeInstance("Failed to update Client Trigger.", e);
        }
    }

}
