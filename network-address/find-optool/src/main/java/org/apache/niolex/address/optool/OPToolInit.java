/**
 * @(#)OPToolInit.java, 2012-8-20. Copyright 2012 Niolex, Inc. All rights reserved.
 */
package org.apache.niolex.address.optool;

import java.util.ArrayList;
import java.util.List;

import org.apache.niolex.address.cmd.CommandOptions;
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

/**
 * @author Xie, Jiyun
 */
public class OPToolInit {

    public static CommandOptions CL;

    public static List<ACL> getAll4Super() throws Exception {
        List<ACL> acls = new ArrayList<ACL>();

        String pwd = Base64Util.byteToBase64(Base16Util.base16toByte(SHAUtil.sha1(CL.auth)));
        String name = CL.auth.substring(0, CL.auth.indexOf(':'));
        Id sid = new Id("digest", name + ":" + pwd);
        ACL su = new ACL(Perms.ALL, sid);
        acls.add(su);
        return acls;
    }

    /**
     * Init a new root.
     *
     * @param cl
     * @throws Exception
     */
    public static void initRoot(CommandOptions cl) throws Exception {
        CL = cl;
        ZooKeeper zk = new ZooKeeper(cl.host, cl.timeout, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                System.out.println("ZK Status - " + event);
            }
        });

        zk.addAuthInfo("digest", cl.auth.getBytes());

        String root = cl.root.charAt(0) == '/' ? cl.root : "/" + cl.root;

        zk.create(root, null, getAll4Super(), CreateMode.PERSISTENT);
        zk.create(root + "/services", null, getAll4Super(), CreateMode.PERSISTENT);
        zk.create(root + "/operators", null, getAll4Super(), CreateMode.PERSISTENT);
        zk.create(root + "/operators/operator", null, getAll4Super(), CreateMode.PERSISTENT);
        zk.create(root + "/clients", null, getAll4Super(), CreateMode.PERSISTENT);
        zk.create(root + "/servers", null, getAll4Super(), CreateMode.PERSISTENT);
        zk.close();
    }

}
