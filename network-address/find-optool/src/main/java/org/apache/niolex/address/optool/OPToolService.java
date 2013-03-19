/**
 * OPToolService.java
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
package org.apache.niolex.address.optool;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.niolex.address.util.PathUtil;
import org.apache.niolex.commons.codec.Base16Util;
import org.apache.niolex.commons.codec.Base64Util;
import org.apache.niolex.commons.codec.SHAUtil;
import org.apache.niolex.commons.codec.StringUtil;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.BadVersionException;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.ZooDefs.Perms;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.data.Stat;

/**
 * This class encapsulates Atomic methods.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5
 * @since 2013-3-18
 */
public class OPToolService extends OPTool {

    /**
     * Invoker super constructor.
     *
     * @param clusterAddress
     * @param sessionTimeout
     * @throws IOException
     */
    public OPToolService(String clusterAddress, int sessionTimeout) throws IOException {
        super(clusterAddress, sessionTimeout);
    }

    /**
     * Generate the digest for authentication.
     *
     * @param userName
     * @param passwd
     * @return the generated digest
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    public Id generateDigest(String userName, String passwd) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        String pwd = Base64Util.byteToBase64(Base16Util.base16toByte(SHAUtil.sha1(userName + ":" + passwd)));
        return new Id("digest", userName + ":" + pwd);
    }

    /**
     * Add ACLs to a node
     *
     * @param fullpath
     * @param ACLs
     * @throws KeeperException
     * @throws InterruptedException
     */
    public void addACLs(String fullpath, List<ACL> ACLs) throws KeeperException, InterruptedException {
        while (true) {
            try {
                Stat st = new Stat();
                List<ACL> acls = zk.getACL(fullpath, st);
                acls.addAll(ACLs);
                zk.setACL(fullpath, acls, st.getAversion());
                return;
            } catch (BadVersionException e){} catch (NoNodeException e){return;}
        }
    }

    /**
     * Add the new ACLs for this service and it's descendants.
     *
     * @param service
     * @param ACLs
     * @throws KeeperException
     * @throws InterruptedException
     */
    public void addACLs4Service(String service, List<ACL> ACLs) throws KeeperException, InterruptedException {
        String servicePath = getServicePath() + "/" + service;
        List<String> superVers = zk.getChildren(servicePath, false);
        for (String sp : superVers) {
            List<String> vers = zk.getChildren(servicePath + "/" + sp, false);
            for (String v : vers) {
                List<String> stats = zk.getChildren(servicePath + "/" + sp + "/" + v, false);
                for (String s : stats) {
                    addACLs(servicePath + "/" + sp + "/" + v + "/" + s, ACLs);
                }
                addACLs(servicePath + "/" + sp + "/" + v, ACLs);
            }
            addACLs(servicePath + "/" + sp, ACLs);
        }
        addACLs(servicePath, ACLs);
    }

    /**
     * Remove some ACLs from a node
     *
     * @param fullpath
     * @param ACLs
     * @throws KeeperException
     * @throws InterruptedException
     */
    public void removeACLs(String fullpath, List<ACL> ACLs) throws KeeperException, InterruptedException {
        while (true) {
            try {
                Stat st = new Stat();
                List<ACL> acls = zk.getACL(fullpath, st);
                acls.removeAll(ACLs);
                zk.setACL(fullpath, acls, st.getAversion());
                return;
            } catch (BadVersionException e){} catch (NoNodeException e){return;}
        }
    }

    /**
     * Serialize this meta data into byte array.
     *
     * @return the byte array
     */
    public byte[] toByteArray(HashMap<String, String> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String,String> en : map.entrySet()) {
            sb.append(en.getKey()).append("=").append(en.getValue()).append("\n");
        }
        return StringUtil.strToUtf8Byte(sb.toString());
    }

    /**
     * Update the client trigger in the version node.
     *
     * @param fullpath
     * @param clientName
     * @throws KeeperException
     * @throws InterruptedException
     */
    public void updateClientTrigger(String fullpath, String clientName) throws KeeperException, InterruptedException {
        while (true) {
            try {
                Stat st = new Stat();
                byte[] data = zk.getData(fullpath, false, st);
                HashMap<String, String> map = this.parseMap(data);
                String v = map.get(clientName);
                if (v != null) {
                    int i = Integer.parseInt(v);
                    ++i;
                    v = Integer.toString(i);
                } else {
                    v = "1";
                }
                map.put(clientName, v);
                zk.setData(fullpath, toByteArray(map), st.getVersion());
                return;
            } catch (BadVersionException e){}
        }
    }

    /**
     * Copy the node data and ACL from the old node to the new node.
     *
     * @param oldNode
     * @param newNode
     * @throws KeeperException
     * @throws InterruptedException
     */
    public void copyNode(String oldNode, String newNode) throws KeeperException, InterruptedException {
        String data = this.getDataStr(oldNode);
        List<ACL> acls = this.getACLs(oldNode);
        this.create(newNode, data, acls);
    }

    /**
     * Copy an existing version and its recursive nodes to the a new version.
     *
     * @param servicePath
     * @param fromVersion
     * @param toVersion
     * @return true if success, false otherwise
     * @throws KeeperException
     * @throws InterruptedException
     */
    public boolean copyServiceVersion(String servicePath, String fromVersion, String toVersion)
            throws KeeperException, InterruptedException {
        servicePath += "/" + PathUtil.VERSIONS;
        if (exists(servicePath + "/" + toVersion)) {
            return false;
        }
        // 1. create the new version node.
        this.copyNode(servicePath + "/" + fromVersion, servicePath + "/" + toVersion);
        List<String> statsNodes = zk.getChildren(servicePath + "/" + fromVersion, false);
        if (statsNodes.isEmpty())
            return true;
        // 2. create all states node.
        for (String statsNode : statsNodes) {
            this.copyNode(servicePath + "/" + fromVersion + "/" + statsNode, servicePath + "/" + toVersion + "/"
                    + statsNode);
        }
        return true;
    }

    /**
     * Copy an existing version and its recursive nodes to the a new version.
     *
     * @param servicePath
     * @param fromVersion
     * @param toVersion
     * @return true if success, false otherwise
     * @throws KeeperException
     * @throws InterruptedException
     */
    public boolean copyClientsVersion(String servicePath, String fromVersion, String toVersion)
            throws KeeperException, InterruptedException {
        servicePath += "/" + PathUtil.CLIENTS;
        if (exists(servicePath + "/" + toVersion)) {
            return false;
        }
        // 1. create the new version node.
        this.copyNode(servicePath + "/" + fromVersion, servicePath + "/" + toVersion);
        List<String> clientsNodes = zk.getChildren(servicePath + "/" + fromVersion, false);
        if (clientsNodes.isEmpty())
            return true;
        // 2. create all states node.
        for (String clientNode : clientsNodes) {
            this.copyNode(servicePath + "/" + fromVersion + "/" + clientNode, servicePath + "/" + toVersion + "/"
                    + clientNode);
        }
        return true;
    }

    /**
     * Get all the permissions for all the operators.
     *
     * @return the permission list
     * @throws Exception
     */
    public List<ACL> getAllPerm4Op() throws Exception {
        List<ACL> list = this.getACLs(root + "/" + PathUtil.OPS);
        for (ACL a : list) {
            a.setPerms(Perms.ALL);
        }
        return list;
    }

    /**
     * Get all the permissions for all the Super operators.
     *
     * @return the permission list
     * @throws Exception
     */
    public List<ACL> getAllPerm4Super() throws Exception {
        List<ACL> list = this.getACLs(root + "/" + PathUtil.OPS);
        List<ACL> list2 = new ArrayList<ACL>();
        for (ACL a : list) {
            if (a.getPerms() == Perms.ALL) {
                list2.add(a);
            }
        }
        return list2;
    }

    /**
     * Check whether the path is in the service path: /root/services
     *
     * @param path the path to check
     * @return true if in service path
     */
    public boolean isInServicePath(String path) {
        return path.startsWith(root + "/" + PathUtil.SERVICES);
    }

    /**
     * Check whether the path is inside the service path: /root/services/xxx...
     *
     * @param path the path to check
     * @return true if inside service path
     */
    public boolean isInSideServicePath(String path) {
        String serviceRoot = root + "/" + PathUtil.SERVICES;
        return path.startsWith(serviceRoot) && path.length() > serviceRoot.length() + 1;
    }

    /**
     * Check whether the path is inside the version path: /root/services/xxx/versions/123...
     */
    public boolean isInsideVersionPath(String path) {
        if (path.startsWith(root + "/" + PathUtil.SERVICES) && countPath(path) >= 5) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return /root/operators
     */
    public String getOpPath() {
        return root + "/" + PathUtil.OPS;
    }

    /**
     * @return /root/clients
     */
    public String getClientsPath() {
        return root + "/" + PathUtil.CLIENTS;
    }

    /**
     * @return /root/servers
     */
    public String getServersPath() {
        return root + "/" + PathUtil.SERVERS;
    }

    /**
     * @return /root/services
     */
    public String getServicePath() {
        return root + "/" + PathUtil.SERVICES;
    }

    /**
     * Count the number of '/' in this path.
     *
     * @param path the path to count
     * @return the count
     */
    public int countPath(String path) {
        int j = 0;
        for (int i = 0; i < path.length(); ++i) {
            if (path.charAt(i) == '/') ++j;
        }
        return j;
    }

}
