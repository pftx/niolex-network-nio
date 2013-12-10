/**
 * @(#)MetaMain.java, 2012-8-20. Copyright 2012 Niolex, Inc. All rights reserved.
 */
package org.apache.niolex.address.op;

import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

/**
 * Init meta data.
 *
 * @author Xie, Jiyun
 */
public class MetaMain extends OPMain {

    /**
     * @param args
     * @throws InterruptedException
     */
    public static void main(String[] args) throws Exception {
        ZooKeeper zk = getZooKeeper();
        String param = "/find/services/org.apache.niolex.address.Test";

        List<ACL> list = getRead4Client();
        list.addAll(getAll4Op());
        list.addAll(getRead4Server());
        zk.create(param + "/clients", null, list, CreateMode.PERSISTENT);
        zk.create(param + "/clients/1", "find-cli=1".getBytes(), list, CreateMode.PERSISTENT);
        zk.create(param + "/clients/4", "find-cli=1".getBytes(), list, CreateMode.PERSISTENT);
        String meta = "IPS=10.1.2.3,10.1.2.4\nQUOTA=100,6000";
        zk.create(param + "/clients/1/find-cli", meta.getBytes(), list, CreateMode.PERSISTENT);
        zk.create(param + "/clients/4/find-cli", meta.getBytes(), list, CreateMode.PERSISTENT);

        list = zk.getACL(param + "/clients/1", new Stat());
        System.out.println("[META] created, ACL: " + list);
    }

}
