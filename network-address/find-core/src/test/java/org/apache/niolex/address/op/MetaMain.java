/**
 * @(#)MetaMain.java, 2012-8-20. Copyright 2012 Niolex, Inc. All rights reserved.
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
public class MetaMain {

    private static String OP_NAME = "operator";
    private static String OP_PASSWORD = "djidf3jdd23";

    public static String SVR_NAME = "find-svr";
    public static String SVR_PASSWORD = "Niolex";

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

    public static List<ACL> getRead4Server() throws Exception {
        List<ACL> acls = new ArrayList<ACL>();

        String pwd = Base64Util.byteToBase64(Base16Util.base16toByte(SHAUtil.sha1(SVR_NAME + ":" + SVR_PASSWORD)));
        Id sid = new Id("digest", SVR_NAME + ":" + pwd);
        ACL su = new ACL(Perms.READ, sid);
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
        String param = "/find/services/org.apache.niolex.address.Test";

        List<ACL> list = getRead4Client();
        list.addAll(getAll4Op());
        list.addAll(getRead4Server());
        zk.create(param + "/clients", null, list, CreateMode.PERSISTENT);
        zk.create(param + "/clients/1", "find-cli=1".getBytes(), list, CreateMode.PERSISTENT);
        String meta = "IPS=10.1.2.3,10.1.2.4\nQUOTA=100,6000";
        zk.create(param + "/clients/1/find-cli", meta.getBytes(), list, CreateMode.PERSISTENT);

        Stat stat = new Stat();
        list = zk.getACL(param + "/clients/1", stat);
        System.out.println("[META] created, ACL: " + list);
    }

}
