/**
 * @(#)ZKMain.java, 2012-8-20. Copyright 2012 Niolex, Inc. All rights reserved.
 */
package org.apache.niolex.address.op;

import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

/**
 * @author Xie, Jiyun
 */
public class StatesMain extends OPMain {

    /**
     * Init state nodes.
     *
     * @param args
     * @throws InterruptedException
     */
    public static void main(String[] args) throws Exception {
        ZooKeeper zk = getZooKeeper();
        String param = "/find/services/org.apache.niolex.address.Test/versions/3";

        List<ACL> list = getRead4Client();
        list.addAll(getAll4Op());
        list.addAll(getCDR4Server());
        zk.create(param + "/A", null, list, CreateMode.PERSISTENT);
        zk.create(param + "/B", null, list, CreateMode.PERSISTENT);
        zk.create(param + "/C", null, list, CreateMode.PERSISTENT);

        list = zk.getACL(param + "/C", new Stat());
        System.out.println("[STATES] created, ACL: " + list);
    }

}
