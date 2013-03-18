/**
 * @(#)OPMain.java, 2012-8-20. Copyright 2012 Niolex, Inc. All rights reserved.
 */
package org.apache.niolex.address.op;

import java.util.ArrayList;
import java.util.List;

import org.apache.niolex.address.core.CoreTest;
import org.apache.niolex.commons.codec.Base16Util;
import org.apache.niolex.commons.codec.Base64Util;
import org.apache.niolex.commons.codec.SHAUtil;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Perms;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.data.Stat;

/**
 * @author Xie, Jiyun
 */
public class OPMain {

    public static String OP_NAME = "operator";
    public static String OP_PASSWORD = "djidf3jdd23";

    public static String CLI_NAME = "find-cli";
    public static String CLI_PASSWORD = "mailto:xiejiyun";

    public static List<ACL> getAll4Op() throws Exception {
        List<ACL> acls = new ArrayList<ACL>();

        String pwd = Base64Util.byteToBase64(Base16Util.base16toByte(SHAUtil.sha1(OP_NAME + ":" + OP_PASSWORD)));
        Id sid = new Id("digest", OP_NAME + ":" + pwd);
        ACL su = new ACL(Perms.ALL, sid);
        acls.add(su);
        return acls;
    }

    public static List<ACL> getRead4Client() throws Exception {
        List<ACL> acls = new ArrayList<ACL>();

        String pwd = Base64Util.byteToBase64(Base16Util.base16toByte(SHAUtil.sha1(CLI_NAME + ":" + CLI_PASSWORD)));
        Id sid = new Id("digest", CLI_NAME + ":" + pwd);
        ACL su = new ACL(Perms.READ, sid);
        acls.add(su);
        return acls;
    }

    /**
     * @param args
     * @throws InterruptedException
     */
    public static void main(String[] args) throws Exception {
        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper(CoreTest.ZK_ADDR, 10000, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    System.out.println("ZK Status - " + event);

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        zk.addAuthInfo("digest", "operator:djidf3jdd23".getBytes());
        String param = args[0];

        if (param != null && param.equals("init")) {
            zk.create("/find", null, getAll4Op(), CreateMode.PERSISTENT);
            zk.create("/find/services", null, getAll4Op(), CreateMode.PERSISTENT);
            zk.create("/find/operators", null, getAll4Op(), CreateMode.PERSISTENT);
            zk.create("/find/operators/operator", null, getAll4Op(), CreateMode.PERSISTENT);
            zk.create("/find/clients", null, getAll4Op(), CreateMode.PERSISTENT);
            zk.create("/find/servers", null, getAll4Op(), CreateMode.PERSISTENT);

            Stat stat = new Stat();
            List<ACL> list = zk.getACL("/find/operators/operator", stat);
            System.out.println("Op created, ACL: " + list);
        } else {
            zk.create("/find/services/" + param, null, getAll4Op(), CreateMode.PERSISTENT);
            List<ACL> list = getRead4Client();
            list.addAll(getAll4Op());
            zk.create("/find/services/" + param + "/versions", null, list, CreateMode.PERSISTENT);
            zk.create("/find/services/" + param + "/versions/1", null, list, CreateMode.PERSISTENT);
            zk.create("/find/services/" + param + "/versions/2", null, list, CreateMode.PERSISTENT);
            zk.create("/find/services/" + param + "/versions/3", null, list, CreateMode.PERSISTENT);
            zk.create("/find/services/" + param + "/versions/4", null, list, CreateMode.PERSISTENT);

            Stat stat = new Stat();
            list = zk.getACL("/find/services/" + param + "/versions/4", stat);
            System.out.println("[" + param + "] created, ACL: " + list);
        }
    }

}
