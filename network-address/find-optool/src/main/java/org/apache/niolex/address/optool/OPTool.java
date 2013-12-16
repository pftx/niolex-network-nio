package org.apache.niolex.address.optool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.niolex.address.ext.ZKOperator;
import org.apache.niolex.commons.codec.StringUtil;
import org.apache.zookeeper.KeeperException;

/**
 * The core optool, encapsulate ZK operation.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 */
public class OPTool extends ZKOperator {

    /**
     * Invoke super Constructor.
     *
     * @param clusterAddress
     * @param sessionTimeout
     * @throws IOException
     */
    public OPTool(String clusterAddress, int sessionTimeout) throws IOException {
        super(clusterAddress, sessionTimeout);
    }


    /**
     * Set data to a node.
     *
     * @param fullpath
     * @param data
     */
    public void setDataStr(String fullpath, String data) throws KeeperException, InterruptedException {
        zk.setData(fullpath, str2byte(data), -1);
    }

    /**
     * Set data to a node.
     *
     * @param fullpath
     * @param data
     */
    public void setData(String fullpath, byte[] data) throws KeeperException, InterruptedException {
        zk.setData(fullpath, data, -1);
    }

    /**
     * Get data from a node.
     *
     * @param fullpath
     * @return the data of the node, null if the data is empty or "null"
     */
    public String getDataStr(String fullpath) throws KeeperException, InterruptedException {
        return byte2str(zk.getData(fullpath, null, null));
    }

    /**
     * List all service start by this prefix.
     *
     * @param rootPath
     * @param prefix
     * @return the service list
     * @throws KeeperException
     * @throws InterruptedException
     */
    public List<String> listServiceByPrefix(String rootPath, String prefix) throws KeeperException, InterruptedException {
        List<String> childrens = zk.getChildren(rootPath, false);
        if (prefix == null) {
            return childrens;
        }
        List<String> rets = new ArrayList<String>();
        for (String c : childrens) {
            if (c.startsWith(prefix))
                rets.add(c);
        }
        return rets;
    }

    /**
     * List all the services started on this IP.
     *
     * @param rootPath
     * @param IP
     * @return the service list
     * @throws KeeperException
     * @throws InterruptedException
     */
    public List<SVSM> listServiceByIP(String rootPath, String IP) throws KeeperException, InterruptedException {
        List<String> services = zk.getChildren(rootPath, false);
        List<SVSM> svsms = new ArrayList<SVSM>();
        for (String ser : services) {
            List<String> vers = zk.getChildren(rootPath + "/" + ser + "/versions", false);
            for (String v : vers) {
                List<String> stats = zk.getChildren(rootPath + "/" + ser + "/versions/" + v, false);
                for (String s : stats) {
                    List<String> nodes = zk.getChildren(rootPath + "/" + ser + "/versions/" + v + "/" + s, false);
                    for (String n : nodes) {
                        int i = n.indexOf(IP);
                        if (i != -1) {
                            svsms.add(new SVSM(ser, Integer.parseInt(v), s, n));
                        }
                    }
                }
            }
        }
        return svsms;
    }

    /**
     * Convert bytes into string by the UTF-8 encoding.
     *
     * @param bytes
     * @return the string
     */
    public String byte2str(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        } else {
            return StringUtil.utf8ByteToStr(bytes);
        }
    }

    /**
     * Convert string into bytes by the UTF-8 encoding.
     * @param str
     * @return the bytes
     */
    public byte[] str2byte(String str) {
        if (str == null || str.isEmpty()) {
            return null;
        } else {
            return StringUtil.strToUtf8Byte(str);
        }
    }

    // Structure for Service, Version, States, Node
    public class SVSM {
        public String service;

        public int version;

        public String stat;

        public String node;

        public SVSM(String s, int v, String stat, String n) {
            this.service = s;
            this.version = v;
            this.stat = stat;
            this.node = n;
        }
    }

}
