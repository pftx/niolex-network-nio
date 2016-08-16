package org.apache.niolex.address.rpc.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

import java.net.InetSocketAddress;
import java.util.Set;

import org.apache.niolex.network.rpc.cli.RpcStub;
import org.junit.Test;

public class RpcStubPoolTest {
    
    InetSocketAddress key1 = new InetSocketAddress("localhost", 38202);
    InetSocketAddress key2 = new InetSocketAddress("localhost", 38202);

    @Test
    public void testGetPool() throws Exception {
        assertEquals(RpcStubPool.getPool(), RpcStubPool.getPool());
    }

    @Test
    public void testGetClients() throws Exception {
        RpcStubPool.getPool().getClients(key1).add(mock(RpcStub.class));
        Set<RpcStub> set1 = RpcStubPool.getPool().getClients(key2);
        assertNotNull(set1);
        assertEquals(set1.size(), 1);
        Set<RpcStub> set2 = RpcStubPool.getPool().removeClients(key2);
        assertEquals(set1, set2);
        assertNull(RpcStubPool.getPool().removeClients(key1));
    }

    @Test
    public void testRemoveClients() throws Exception {
        RpcStubPool.getPool().getClients(key1).add(mock(RpcStub.class));
        Set<RpcStub> set = RpcStubPool.getPool().removeClients(key2);
        assertNotNull(set);
        assertEquals(set.size(), 1);
        set = RpcStubPool.getPool().removeClients(key1);
        assertNull(set);
    }

}
