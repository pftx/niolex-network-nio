package org.apache.niolex.network.rpc.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;

import org.apache.niolex.commons.reflect.MethodUtil;
import org.apache.niolex.network.ConnStatus;
import org.apache.niolex.network.client.PacketClient;
import org.apache.niolex.network.demo.json.RpcService;
import org.apache.niolex.network.rpc.conv.JsonConverter;
import org.junit.Assert;
import org.junit.Test;

public class BaseInvokerTest {

    @Test
    public void testBaseInvoker() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testConnect() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testStop() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testCheckStatus() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testSendPacket() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testInvoke() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testHandlePacket() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testHandleClose() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testCloseClient() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testGetRemoteAddress() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testSetServerAddressInetSocketAddress() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testSetServerAddressString() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testGetConnStatus() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testIsReady() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testSetConnectTimeout() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testGetSleepBetweenRetryTime() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testSetSleepBetweenRetryTime() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testGetConnectRetryTimes() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testSetConnectRetryTimes() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testGetRpcHandleTimeout() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testSetRpcHandleTimeout() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testIsException() throws Throwable {
        BaseInvoker in = mock(BaseInvoker.class);
        RpcStub rr = new RpcStub(in, new JsonConverter());

        rr.connect(); // 1
        assertTrue(pc.isReady());
        rr.connect(); // 2
        assertTrue(pc.isReady());

        Method m = MethodUtil.getFirstMethod(rr, "isException");
        m.setAccessible(true);
        assertTrue((Boolean) m.invoke(rr, 1));
        assertTrue((Boolean) m.invoke(rr, -255));

        rr.stop(); // -- stopped
        assertFalse(pc.isReady());
        rr.stop(); // -- this time will be skipped
        assertFalse(pc.isReady());
    }

    @Test
    public void testHandleCloseAlreadyClosed() throws Throwable {
        PacketClient pc = new PacketClient(new InetSocketAddress("localhost", 8808));
        RpcStub rr = new RpcStub(pc, new JsonConverter());
        rr.addInferface(RpcService.class);
        rr.getRemoteName();
        rr.setSleepBetweenRetryTime(10);
        Assert.assertEquals(10, rr.getSleepBetweenRetryTime());
        rr.setConnectTimeout(120);
        rr.stop();
        rr.handleClose(pc);
    }

    @Test
    public void testHandleCloseFailedToReconnect() throws Throwable {
        PacketClient pc = new PacketClient(new InetSocketAddress("localhost", 8808));
        RpcStub rr = new RpcStub(pc, new JsonConverter());
        rr.addInferface(RpcService.class);
        rr.getRemoteName();
        rr.setSleepBetweenRetryTime(10);
        rr.setConnectTimeout(10);
        rr.setConnectRetryTimes(1);
        Assert.assertEquals(1, rr.getConnectRetryTimes());
        rr.handleClose(pc);
    }

    @Test
    public void testPrepareReturn() throws Exception {
        PacketClient pc = mock(PacketClient.class);
        BaseInvoker in = mock(BaseInvoker.class);
        RpcStub rr = new RpcStub(pc, in, new JsonConverter());
        assertNull(rr.prepareReturn(null, null, false));
        assertNull(rr.prepareReturn(null, void.class, false));
    }

    @Test
    public void testAddInferface() throws Exception {
        System.out.println("not yet implemented");
    }

    @Test
    public void testSetServerAddressString() throws Exception {
        BaseInvoker pc = new BaseInvoker(new PacketClient());
        RpcStub rr = new RpcStub(pc, new JsonConverter());
        rr.addInferface(RpcService.class);
        rr.setServerAddress("localhost:9876");
        assertFalse(pc.isReady());
        assertEquals(ConnStatus.INNITIAL, rr.getConnStatus());
        InetSocketAddress addr = new InetSocketAddress("localhost", 9876);
        assertEquals(addr, pc.getServerAddress());
    }

}
