package org.apache.niolex.network.rpc.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.apache.niolex.commons.concurrent.Blocker;
import org.apache.niolex.commons.concurrent.ThreadUtil;
import org.apache.niolex.commons.reflect.FieldUtil;
import org.apache.niolex.commons.util.Runner;
import org.apache.niolex.network.ConnStatus;
import org.apache.niolex.network.IClient;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.rpc.RpcException;
import org.junit.Test;

public class BaseInvokerTest {

    @Test
    public void testCheckStatus() throws Exception {
        final IClient client = mock(IClient.class);
        BaseInvoker bi = new BaseInvoker(client);
        try {
            bi.checkStatus();
            assertTrue(false);
        } catch (RpcException e) {
            assertEquals(RpcException.Type.NOT_CONNECTED, e.getType());
        }
        bi.connStatus = ConnStatus.CONNECTING;
        try {
            bi.checkStatus();
            assertTrue(false);
        } catch (RpcException e) {
            assertEquals(RpcException.Type.NOT_CONNECTED, e.getType());
        }
        bi.connect();
        try {
            bi.checkStatus();
            assertTrue(true);
        } catch (RpcException e) {
            assertTrue(false);
        }
        bi.stop();
        try {
            bi.checkStatus();
            assertTrue(false);
        } catch (RpcException e) {
            assertEquals(RpcException.Type.CONNECTION_CLOSED, e.getType());
        }
    }

    public void notifyInvoke(Blocker<PacketData> blocker) throws Exception {
        ThreadUtil.sleepAtLeast(50);
        blocker.releaseAll();
    }

    @Test
    public void testInvoke() throws Exception {
        final IClient client = mock(IClient.class);
        BaseInvoker bi = new BaseInvoker(client);
        bi.connect();

        Blocker<PacketData> blocker = FieldUtil.getValue(bi, "blocker");
        Thread t = Runner.run(this, "notifyInvoke", blocker);

        PacketData packet = new PacketData(56, new byte[76]);
        PacketData data = bi.invoke(packet);
        assertNull(data);
        t.join();
    }

}
