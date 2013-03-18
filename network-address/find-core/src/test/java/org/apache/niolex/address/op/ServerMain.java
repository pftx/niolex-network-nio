/**
 * @(#)ZKMain.java, 2012-8-20. Copyright 2012 Niolex, Inc. All rights reserved.
 */
package org.apache.niolex.address.op;

import java.util.List;

import org.apache.niolex.address.core.CoreTest;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

/**
 * @author Xie, Jiyun
 */
public class ServerMain {

    public static String SVR_NAME = "find-svr";
    public static String SVR_PASSWORD = "Niolex";

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

        zk.addAuthInfo("digest", (SVR_NAME + ":" + SVR_PASSWORD).getBytes());
        String param = "/find/services/org.apache.niolex.address.Test/versions/1/B";

        zk.create(param + "/10.1.2.3:8808", null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        zk.create(param + "/10.1.2.4:8808", null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        zk.create(param + "/10.1.2.5:8808", null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

        Stat stat = new Stat();
        List<ACL> list = zk.getACL(param + "/10.1.2.3:8808", stat);
        System.out.println("[NODES] created, ACL: " + list);
    }

}
