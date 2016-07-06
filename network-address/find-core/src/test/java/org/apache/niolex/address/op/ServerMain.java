/**
 * @(#)ZKMain.java, 2012-8-20. Copyright 2012 Niolex, Inc. All rights reserved.
 */
package org.apache.niolex.address.op;

import java.util.List;

import org.apache.niolex.address.core.CoreTest;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

/**
 * @author Xie, Jiyun
 */
public class ServerMain {

    /**
     * Init server service nodes.
     *
     * @param args
     * @throws InterruptedException
     */
    public static void main(String[] args) throws Exception {
        ZooKeeper zk = CoreTest.CON_SU.zooKeeper();

        zk.addAuthInfo("digest", (OPMain.SVR_NAME + ":" + OPMain.SVR_PASSWORD).getBytes());
        String param = "/find/services/org.apache.niolex.address.Test/versions/3/C";
        if (args != null && args.length != 0) {
            param = args[0];
        }

        zk.create(param + "/10.1.2.3:8808", null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        zk.create(param + "/10.1.2.4:8808", null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        zk.create(param + "/10.1.2.5:8808", null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

        Stat stat = new Stat();
        List<ACL> list = zk.getACL(param + "/10.1.2.3:8808", stat);
        System.out.println("[NODES] created, ACL: " + list);
    }

}
