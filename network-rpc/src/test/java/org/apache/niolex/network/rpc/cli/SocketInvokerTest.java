package org.apache.niolex.network.rpc.cli;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.niolex.commons.concurrent.ThreadUtil;
import org.apache.niolex.commons.reflect.FieldUtil;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.demo.DemoServer;
import org.apache.niolex.network.rpc.RpcException;
import org.apache.niolex.network.rpc.cli.SocketInvoker.Notifier;
import org.apache.niolex.network.rpc.util.RpcUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class SocketInvokerTest implements Runnable {

    private SocketInvoker si = new SocketInvoker(new InetSocketAddress("localhost", 9858));

    @BeforeClass
    public static void beforeClass() throws IOException {
        DemoServer.main(new String[] { "-p", "9858" });
    }

    @AfterClass
    public static void afterClass() {
        DemoServer.stop();
    }

    @Test
    public void testSocketInvoker() throws Exception {
        si.connect();
        assertTrue(si.isWorking());
        assertFalse(si.isStoped);
        si.checkStatus();

        si.stop();
        assertFalse(si.isReady());
        assertFalse(si.isWorking());
        assertTrue(si.isStoped);
    }

    @Test(expected = RpcException.class)
    public void testConnectClose() throws Exception {
        si.isStoped = true;
        try {
            si.checkStatus();
        } catch (RpcException e) {
            assertEquals(RpcException.Type.CONNECTION_CLOSED, e.getType());
            throw e;
        }
    }

    @Test(expected = RpcException.class)
    public void testConnectLoss() throws Exception {
        si.setConnectRetryTimes(0);
        si.isStoped = false;
        FieldUtil.setValue(si, "socket", new Socket());
        try {
            si.checkStatus();
        } catch (RpcException e) {
            assertEquals(RpcException.Type.NOT_CONNECTED, e.getType());
            throw e;
        }
    }

    @Test
    public void testCheckStatus() throws Exception {
        si.setSleepBetweenRetryTime(1);
        si.isStoped = false;
        assertFalse(si.isWorking());
        si.fireRetry();
        si.fireRetry();
        si.fireRetry();
        ThreadUtil.sleepAtLeast(50);
        assertTrue(si.isWorking());
        assertFalse(si.isStoped);

        si.stop();
        assertFalse(si.isReady());
        assertFalse(si.isWorking());
        assertTrue(si.isStoped);
    }

    @Test
    public void testSendPacket() throws Exception {
        si.connect();
        si.sendPacket(PacketData.getHeartBeatPacket());
        si.stop();
    }

    @Test
    public void testInvoke() throws Exception {
        si.connect();
        PacketData packet = new PacketData(2, "(D(J@LKJDO");
        for (int i = 0; i < 65536; ++i) {
            packet.setVersion((byte) (i >> 8));
            packet.setReserved((byte) (i));
            PacketData r = si.invoke(packet);
            assertArrayEquals(r.getData(), packet.getData());
        }
        si.checkStatus();
        System.out.println(" XXX Connected To > " + si.getRemoteAddress());
        si.stop();
    }

    @Test
    public void testWaitForResult() throws Exception {
        assertNull(si.waitForResult(null, null));
        si.setRpcHandleTimeout(-1);
        assertNull(si.waitForResult(null, null));
        FieldUtil.setValue(si, "isWorking", true);
        assertNull(si.waitForResult(null, null));
    }

    @Test(expected = RpcException.class)
    public void testInvokeWithExceptionOut() throws Exception {
        si.setConnectRetryTimes(0);
        si.isStoped = false;
        FieldUtil.setValue(si, "isWorking", true);
        Socket so = mock(Socket.class);
        when(so.isConnected()).thenReturn(false);
        FieldUtil.setValue(si, "socket", so);
        OutputStream out = mock(OutputStream.class);

        InputStream in = mock(InputStream.class);
        doThrow(new IOException("iv in")).when(in).read(any(byte[].class), anyInt(), anyInt());

        FieldUtil.setValue(si, "out", out);
        FieldUtil.setValue(si, "in", in);
        si.invoke(PacketData.getHeartBeatPacket());
    }

    @Test(expected = RpcException.class)
    public void testHandleWrite() throws Exception {
        si.setConnectRetryTimes(0);
        si.isStoped = false;
        FieldUtil.setValue(si, "socket", new Socket());
        OutputStream out = mock(OutputStream.class);
        doThrow(new IOException("dd out")).when(out).write(any(byte[].class));
        FieldUtil.setValue(si, "out", out);
        si.handleWrite(PacketData.getHeartBeatPacket());
    }

    @Test
    public void testHandleWriteOK() throws Exception {
        si.setConnectRetryTimes(0);
        si.isStoped = false;
        Socket so = mock(Socket.class);
        when(so.isConnected()).thenReturn(true);
        FieldUtil.setValue(si, "socket", so);
        OutputStream out = mock(OutputStream.class);
        doThrow(new IOException("dd in")).when(out).write(any(byte[].class));
        FieldUtil.setValue(si, "out", out);
        si.handleWrite(PacketData.getHeartBeatPacket());
    }

    @Test
    public void testStop() throws Exception {
        si.setConnectRetryTimes(0);
        Socket so = mock(Socket.class);
        doThrow(new IOException("dd stop")).when(so).close();
        FieldUtil.setValue(si, "socket", so);
        si.stop();
    }

    private final AtomicInteger codeGen = new AtomicInteger(303);
    private final Map<Integer, Integer> errMap = new HashMap<Integer, Integer>();

    @Override
    public void run() {
        int code = codeGen.incrementAndGet();
        PacketData packet = new PacketData(code, "(D(J@OIUFP - " + code);
        int err = 0;

        for (int i = 0; i < 4096; ++i) {
            packet.setVersion((byte) (i >> 8));
            packet.setReserved((byte) (i));
            PacketData r = si.invoke(packet);
            try {
                if (r != null) {
                    assertArrayEquals(r.getData(), packet.getData());
                } else {
                    ++err;
                }
            } catch (AssertionError e) {
                ++err;
            }
        }

        errMap.put(code, err);
    }

    @Test
    public void testRun() throws Exception {
        si.connect();

        int threads = 6;
        Thread[] thr = new Thread[threads];
        for (int i = 0; i < threads; ++i) {
            thr[i] = new Thread(this);
            thr[i].start();
        }

        PacketData packet = new PacketData(2, "(D(J@LKJDO");
        int err = 0;

        for (int i = 0; i < 4096; ++i) {
            packet.setVersion((byte) (i >> 8));
            packet.setReserved((byte) (i));
            PacketData r = si.invoke(packet);
            if (r != null) {
                assertArrayEquals(r.getData(), packet.getData());
            } else {
                ++err;
            }
        }

        errMap.put(2, err);
        si.checkStatus();
        System.out.println(" XXX Connected To > " + si.getRemoteAddress());

        for (int i = 0; i < threads; ++i) {
            thr[i].join();
        }
        System.out.println(" XXX error map -> " + errMap);
        si.stop();
    }

    @Test
    public void testRetryConnect() throws Exception {
        // 1. Set invalid address.
        si.setServerAddress("localhost:7080");
        si.setConnectRetryTimes(2);
        si.setSleepBetweenRetryTime(1);
        assertFalse(si.retryConnect());
    }

    @Test
    public void testSetSleepBetweenRetryTime() throws Exception {
        si.setSleepBetweenRetryTime(445);
        assertEquals(445, si.getSleepBetweenRetryTime());
    }

    @Test
    public void testSetConnectRetryTimes() throws Exception {
        si.setConnectRetryTimes(5566);
        assertEquals(5566, si.getConnectRetryTimes());
    }

    @Test
    public void testSetServerAddress() throws Exception {
        si.setServerAddress("localhost:7788");
        assertEquals(new InetSocketAddress("localhost", 7788), si.getServerAddress());
    }

    @Test
    public void testSetConnectTimeout() throws Exception {
        si.setConnectTimeout(4050);
        assertEquals(4050, si.getConnectTimeout());
    }

    @Test
    public void testSetSocketBufferSize() throws Exception {
        si.setSocketBufferSize(2030);
        assertEquals(2030, si.getSocketBufferSize());
    }

    @Test
    public void testSetRpcHandleTimeout() throws Exception {
        si.setRpcHandleTimeout(10203);
        assertEquals(10203, si.getRpcHandleTimeout());
    }

    @Test
    public void testReleaseAll() throws Exception {
        ConcurrentMap<Integer, Notifier> notifyMap = FieldUtil.getValue(si, "notifyMap");
        notifyMap.put(33, new Notifier());
        si.releaseAll();
        assertTrue(Thread.interrupted());
    }

    @Test
    public void testReadFromSocket() throws Exception {
        si.setConnectRetryTimes(0);
        si.isStoped = false;
        Socket so = mock(Socket.class);
        when(so.isConnected()).thenReturn(true);
        FieldUtil.setValue(si, "socket", so);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        FieldUtil.setValue(si, "out", out);

        PacketData sc = new PacketData(23, "This is random.");
        for (int i = 0; i < 66; ++i) {
            sc.setReserved((byte) (i));
            si.handleWrite(sc);
        }
        int key = RpcUtil.generateKey(sc);
        
        InputStream in = new ByteArrayInputStream(out.toByteArray());
        FieldUtil.setValue(si, "in", in);

        PacketData rc = si.readFromSocket(key);
        assertArrayEquals(rc.getData(), sc.getData());
    }

    @Test(expected = NullPointerException.class)
    public void testReadFromSocketErr() throws Exception {
        si.readFromSocket(33);
    }

    @Test(expected = RpcException.class)
    public void testReadFromSocketError() throws Exception {
        InputStream in = mock(InputStream.class);
        doThrow(new IOException("rf in")).when(in).read(any(byte[].class), anyInt(), anyInt());

        FieldUtil.setValue(si, "in", in);
        si.readFromSocket(33);
    }

    @Test(expected = RpcException.class)
    public void testReadFromSocketErro() throws Exception {
        si.setConnectRetryTimes(0);
        si.isStoped = false;
        Socket so = mock(Socket.class);
        when(so.isConnected()).thenReturn(true, false);
        FieldUtil.setValue(si, "socket", so);

        InputStream in = mock(InputStream.class);
        doThrow(new IOException("rf in")).when(in).read(any(byte[].class), anyInt(), anyInt());
        FieldUtil.setValue(si, "in", in);
        si.readFromSocket(33);
    }

    @Test
    public void testCleanAndNotify() throws Exception {
        @SuppressWarnings("unchecked")
        Iterator<Entry<Integer, Notifier>> iterator = mock(Iterator.class);

        when(iterator.hasNext()).thenReturn(true);
        when(iterator.next()).thenThrow(new NoSuchElementException());

        si.notifyIt(iterator);
    }

}
