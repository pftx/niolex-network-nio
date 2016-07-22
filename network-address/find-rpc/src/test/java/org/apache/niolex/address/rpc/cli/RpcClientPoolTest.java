package org.apache.niolex.address.rpc.cli;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.net.InetSocketAddress;
import java.util.Set;

import org.apache.niolex.network.rpc.RpcClient;
import org.junit.Test;

public class RpcClientPoolTest {
    
    InetSocketAddress key1 = new InetSocketAddress("localhost", 38202);
    InetSocketAddress key2 = new InetSocketAddress("localhost", 38202);

    @Test
    public void testGetPool() throws Exception {
        assertEquals(RpcClientPool.getPool(), RpcClientPool.getPool());
    }

    @Test
    public void testGetClients() throws Exception {
        RpcClientPool.getPool().getClients(key1).add(mock(RpcClient.class));
        Set<RpcClient> set1 = RpcClientPool.getPool().getClients(key2);
        assertNotNull(set1);
        assertEquals(set1.size(), 1);
        Set<RpcClient> set2 = RpcClientPool.getPool().removeClients(key2);
        assertEquals(set1, set2);
        assertNull(RpcClientPool.getPool().removeClients(key1));
    }

    @Test
    public void testRemoveClients() throws Exception {
        RpcClientPool.getPool().getClients(key1).add(mock(RpcClient.class));
        Set<RpcClient> set = RpcClientPool.getPool().removeClients(key2);
        assertNotNull(set);
        assertEquals(set.size(), 1);
        set = RpcClientPool.getPool().removeClients(key1);
        assertNull(set);
    }

}
