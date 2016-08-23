package org.apache.niolex.address.rpc.svr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.niolex.address.rpc.DemoService;
import org.junit.Test;

public class RpcServerTest implements Serializable {

    private static final long serialVersionUID = 7785626622078765763L;

    @Test
    public void testRpcServer() throws Exception {
        System.setProperty("rpc.selector.threadsnumber", "8");
        System.setProperty("rpc.handler.threadsnumber", "2");
        RpcServer r = new RpcServer();
        assertEquals(8, r.getSelectorThreadsNumber());
        assertEquals(2, r.getHandlerThreadsNumber());
        assertEquals(0, r.getQueueSize());

        // Just stop it.
        r.stop();
    }

    @Test
    public void testStart() throws Exception {
        System.setProperty("zk.cluster.address", "localhost:9181");
        System.setProperty("zk.session.timeout", "10000");
        System.setProperty("zk.svr.username", "redis");
        System.setProperty("zk.svr.password", "mailto:xiejiyun");
        System.setProperty("zk.root", "find");
        RpcServer r = new RpcServer();
        r.setHandlerThreadsNumber(0);
        List<RpcExpose> exposeList = new ArrayList<RpcExpose>();
        exposeList.add(new RpcExpose(this));
        r.setExposeList(exposeList);

        assertFalse(r.start());
    }

    @Test
    public void testStartTwoType() throws Exception {
        System.setProperty("zk.cluster.address", "localhost:9181");
        System.setProperty("zk.session.timeout", "10000");
        System.setProperty("zk.svr.username", "redis");
        System.setProperty("zk.svr.password", "mailto:xiejiyun");
        System.setProperty("zk.root", "find");
        RpcServer r = new RpcServer();
        List<RpcExpose> exposeList = new ArrayList<RpcExpose>();
        RpcExpose r1 = new RpcExpose(DemoService.class, this, "A", 2);
        r1.setServiceType("abc");
        exposeList.add(r1);
        RpcExpose r2 = new RpcExpose(DemoService.class, this, "A", 3);
        r2.setServiceType("abd");
        exposeList.add(r2);
        r.setExposeList(exposeList);

        assertFalse(r.start());
    }

    @Test
    public void testStartOneTypeNotFound() throws Exception {
        System.setProperty("zk.cluster.address", "localhost:9181");
        System.setProperty("zk.session.timeout", "10000");
        System.setProperty("zk.svr.username", "redis");
        System.setProperty("zk.svr.password", "mailto:xiejiyun");
        System.setProperty("zk.root", "find");
        RpcServer r = new RpcServer();
        List<RpcExpose> exposeList = new ArrayList<RpcExpose>();
        RpcExpose r1 = new RpcExpose(DemoService.class, this, "A", 2);
        r1.setServiceType("abc");
        exposeList.add(r1);
        RpcExpose r2 = new RpcExpose(DemoService.class, this, "A", 3);
        r2.setServiceType("abc");
        exposeList.add(r2);
        r.setExposeList(exposeList);

        assertFalse(r.start());
    }

    @Test
    public void testStartInvalidZKAddr() throws Exception {
        System.setProperty("zk.cluster.address", "oeoifj.ije.asi.com:3344");
        System.setProperty("zk.session.timeout", "10000");
        System.setProperty("zk.svr.username", "redis");
        System.setProperty("zk.svr.password", "mailto:xiejiyun");
        System.setProperty("zk.root", "find");
        RpcServer r = new RpcServer();
        List<RpcExpose> exposeList = new ArrayList<RpcExpose>();
        RpcExpose r1 = new RpcExpose(DemoService.class, this, "A", 2);
        exposeList.add(r1);
        RpcExpose r2 = new RpcExpose(DemoService.class, this, "A", 3);
        exposeList.add(r2);
        r.setExposeList(exposeList);

        assertFalse(r.start());
    }

    @Test
    public void testStartServerCantStart() throws Exception {
        System.setProperty("zk.cluster.address", "localhost:9181");
        System.setProperty("zk.session.timeout", "10000");
        System.setProperty("zk.svr.username", "redis");
        System.setProperty("zk.svr.password", "mailto:xiejiyun");
        System.setProperty("zk.root", "find");
        RpcServer r = new RpcServer();
        List<RpcExpose> exposeList = new ArrayList<RpcExpose>();
        RpcExpose r1 = new RpcExpose(DemoService.class, this, "A", 2);
        exposeList.add(r1);
        RpcExpose r2 = new RpcExpose(DemoService.class, this, "A", 3);
        exposeList.add(r2);
        r.setExposeList(exposeList);
        r.setPort(9181);

        assertFalse(r.start());
    }

    @Test
    public void testGetQueueSize() throws Exception {
        System.setProperty("zk.cluster.address", "localhost:9181");
        System.setProperty("zk.session.timeout", "10000");
        System.setProperty("zk.svr.username", "redis");
        System.setProperty("zk.svr.password", "mailto:xiejiyun");
        System.setProperty("zk.root", "find");
        RpcServer r = new RpcServer();
        List<RpcExpose> exposeList = new ArrayList<RpcExpose>();
        RpcExpose r1 = new RpcExpose(DemoService.class, this, "A", 2);
        exposeList.add(r1);
        RpcExpose r2 = new RpcExpose(DemoService.class, this, "A", 3);
        exposeList.add(r2);
        r.setExposeList(exposeList);
        r.setRpcHostIp("127.1.2.3");

        assertTrue(r.start());
        assertEquals(0, r.getQueueSize());

        r.stop();
    }

    @Test
    public void testGetZkEnvironment() throws Exception {
        RpcServer r = new RpcServer();
        r.setZkClusterAddress("12.34.56.78:9120");
        r.setZkEnvironment("find-me");
        assertEquals("12.34.56.78:9120", r.getZkClusterAddress());
        assertEquals("find-me", r.getZkEnvironment());

        r.setZkUserName("udhud");
        r.setZkPassword("*jf*l3d)Df9");
        assertEquals("udhud", r.getZkUserName());
        assertEquals("*jf*l3d)Df9", r.getZkPassword());

        r.setZkSessionTimeout(10303);
        assertEquals(10303, r.getZkSessionTimeout());
    }

    @Test
    public void testSetRpcHostIp() throws Exception {
        System.setProperty("rpc.host.ip", "2.10.33.251");
        RpcServer r = new RpcServer();
        assertEquals("2.10.33.251", r.getRpcHostIp());
        // -
        r.setRpcHostIp("12.10.33.251");
        assertEquals("12.10.33.251", r.getRpcHostIp());

        // -
        r.setHandlerThreadsNumber(33);
        assertEquals(33, r.getHandlerThreadsNumber());

        // -
        r.setSelectorThreadsNumber(45);
        assertEquals(45, r.getSelectorThreadsNumber());

        // -
        r.setAcceptTimeout(3030);
        assertEquals(3030, r.getAcceptTimeout());

        assertNull(r.getExposeList());
        System.setProperty("rpc.host.ip", "127.0.1.2");
    }

}
