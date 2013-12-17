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

import static org.apache.niolex.address.util.ACLUtil.*;
import static org.apache.niolex.address.util.PathUtil.*;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.niolex.address.ext.ZKOperator;
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
public class OPToolService extends ZKOperator {

    /**
     * Invoke super constructor.
     *
     * @param clusterAddress
     * @param sessionTimeout
     * @throws IOException
     */
    public OPToolService(String clusterAddress, int sessionTimeout) throws IOException {
        super(clusterAddress, sessionTimeout);
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
        byte[] data = this.getData(oldNode);
        List<ACL> acls = this.getACL(oldNode);
        this.createNode(newNode, data, acls);
    }

    /**
     * Get all the permissions for all the operators.
     *
     * @return the permission list
     * @throws Exception
     */
    public List<ACL> getAllPerm4Op() throws Exception {
        List<ACL> list = this.getACL(root + "/" + PathUtil.OP_ROOT);
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
        List<ACL> list = this.getACL(root + "/" + PathUtil.OP_ROOT);
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
        return path.startsWith(root + "/" + PathUtil.SERVICES) && countPath(path) >= 3;
    }

    /**
     * Check whether the path is at the service path: /root/services/xxx
     */
    public boolean isAtServicePath(String path) {
        if (path.startsWith(root + "/" + PathUtil.SERVICES) && countPath(path) == 3 && !path.endsWith("/")) {
            return true;
        } else {
            return false;
        }
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
        return root + "/" + PathUtil.OP_ROOT;
    }

    /**
     * @return /root/clients
     */
    public String getClientsPath() {
        return root + "/" + PathUtil.CLI_ROOT;
    }

    /**
     * @return /root/servers
     */
    public String getServersPath() {
        return root + "/" + PathUtil.SVR_ROOT;
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

    /**
     * Make the version path to a new version path walk through the clients.
     *
     * @param path
     * @return the path
     */
    protected String makeClientVersionPath(String path) {
        String[] curl = path.split("/");
        StringBuilder ret = new StringBuilder();
        ret.append(getRoot()).append("/").append(PathUtil.SERVICES).append("/");
        ret.append(curl[3]).append("/").append(PathUtil.CLI_ROOT).append("/");
        ret.append(curl[5]);
        return ret.toString();
    }

}
