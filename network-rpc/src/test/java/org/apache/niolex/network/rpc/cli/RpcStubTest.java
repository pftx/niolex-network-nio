package org.apache.niolex.network.rpc.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import org.apache.niolex.commons.reflect.MethodUtil;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.demo.json.RpcService;
import org.apache.niolex.network.demo.json.RpcServiceImpl;
import org.apache.niolex.network.rpc.RpcException;
import org.apache.niolex.network.rpc.conv.JsonConverter;
import org.apache.niolex.network.rpc.util.RpcUtil;
import org.junit.Test;

public class RpcStubTest {

    @Test
    public void testBlockingStub() throws Exception {
        RemoteInvoker invoker = mock(RemoteInvoker.class);
        RpcStub bs = new RpcStub(invoker, new JsonConverter());

        bs.addInferface(BlockingStRpcStubTest       bs.addInferface(RpcService.class);
    }

    @Test(expected = RpcException.class)
    public void testGetService() throws Throwable {
        RemoteInvoker invoker = mock(RemoteInvoker.class);
        RpcStub bs = new RpcStub(invoker, new JsonConverter());

        bs.addInferface(BlockingStubTest.claRpcStubTesthod method = MethodUtil.getFirstMethod(getClass(), "testGetService");
        bs.invoke(null, method, null);
    }

    @Test(expected = RpcException.class)
    public void testAddInferface() throws Exception {
        RemoteInvoker invoker = mock(RemoteInvoker.class);
        RpcStub bs = new RpcStub(invoker, new JsonConverter());

        RpcService rpc = bs.getService(RpcService.class);
        rpc.add(3);
    }

    @Test(expected = RpcException.class)
    public void testInvoke() throws Exception {
        RemoteInvoker invoker = mock(RemoteInvoker.class);
        RpcStub bs = new RpcStub(invoker, new JsonConverter());

        RpcService rpc = bs.getService(RpcService.class);
        rpc.throwEx();
    }

    @Test(expected = RpcException.class)
    public void testPrepareReturn() throws Throwable {
        RemoteInvoker invoker = mock(RemoteInvoker.class);
        RpcStub bs = new RpcStub(invoker, new JsonConverter());

        bs.addInferface(RpcService.class);

        Method method = MethodUtil.getFirstMethod(RpcServiceImpl.class, "throwEx");
        bs.invoke(null, method, null);
    }

    @Test(expected = NullPointerException.class)
    public void testGetServiceUrl() throws Exception {
        RemoteInvoker invoker = mock(RemoteInvoker.class);
        when(invoker.invoke(any(PacketData.class))).thenReturn(PacketData.getHeartBeatPacket());
        RpcStub bs = new RpcStub(invoker, new JsonConverter());

        RpcService rpc = bs.getService(RpcService.class);
        rpc.add(3);
    }

    @Test
    public void testIsReady() throws Exception {
        JsonConverter json = new JsonConverter();
        RemoteInvoker invoker = mock(RemoteInvoker.class);
        PacketData pd = new PacketData(33, json.serializeReturn(5678));
        pd.setReserved((byte) 1);

        when(invoker.invoke(any(PacketData.class))).thenReturn(pd);
        RpcStub bs = new RpcStub(invoker, json);

        RpcService rpc = bs.getService(RpcService.class);
        int r = rpc.add(3);
        assertEquals(5678, r);
    }

    @Test(expected = RpcException.class)
    public void testNotReady() throws Exception {
        JsonConverter json = new JsonConverter();
        RemoteInvoker invoker = mock(RemoteInvoker.class);
        RpcException ex = new RpcException("It is true.", RpcException.Type.ERROR_EXCEED_RETRY, null);
        PacketData pd = new PacketData(33, RpcUtil.serializeRpcException(ex));
        pd.setReserved((byte) 2);

        when(invoker.invoke(any(PacketData.class))).thenReturn(pd);
        RpcStub bs = new RpcStub(invoker, json);

        RpcService rpc = bs.getService(RpcService.class);
        int r = rpc.add(3);
        assertEquals(5678, r);
    }

    @Test
    public void testGetHandler() throws Exception {
        RemoteInvoker invoker = mock(RemoteInvoker.class);
        RpcStub bs = new RpcStub(invoker, new JsonConverter());

        Object o = bs.prepareReturn(null, null, false);
        assertNull(o);
        o = bs.prepareReturn(null, void.class, false);
        assertNull(o);

        assertFalse(bs.isReady());
        bs.notReady(null);
        assertNull(bs.getServiceUrl());
    }

    @Test
    public void testGetInvoker() throws Exception {
        RemoteInvoker invoker = mock(RemoteInvoker.class);
        RpcStub bs = new RpcStub(invoker, new JsonConverter());
        assertEquals(invoker, bs.getInvoker());
        assertEquals(bs, bs.getHandler());
    }

}
