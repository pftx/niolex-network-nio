package org.apache.niolex.address.rpc.cli.pool;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.niolex.address.rpc.AddressUtil;
import org.apache.niolex.address.rpc.DemoService;
import org.apache.niolex.address.rpc.cli.NodeInfo;
import org.apache.niolex.commons.bean.MutableOne;
import org.apache.niolex.commons.reflect.FieldUtil;
import org.apache.niolex.network.cli.RetryHandler;
import org.apache.niolex.network.rpc.cli.RpcStub;
import org.apache.niolex.network.rpc.cli.SingleInvoker;
import org.junit.Before;
import org.junit.Test;

public class RetryStubTest {

    private MutableOne<List<String>> mutableOne = new MutableOne<List<String>>();
    private RetryStub<DemoService> pool;
    private Set<NodeInfo> readySet;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        pool = new RetryMock(DemoService.class, mutableOne);
        List<RpcStub> handlers = new ArrayList<RpcStub>();
        for (int i = 0; i < 3; ++i) {
            handlers.add(mock(RpcStub.class));
            if (i == 1) {
                SingleInvoker value = new SingleInvoker(null);
                value.setConnectRetryTimes(0);
                when(handlers.get(i).getInvoker()).thenReturn(value);
            }
        }

        RetryHandler<RpcStub> handler = new RetryHandler<RpcStub>(handlers, 2, 12);

        FieldUtil.setValue(pool, "handler", handler);
        FieldUtil.setValue(pool, "isWorking", true);
        readySet = FieldUtil.getValue(pool, "readySet");
    }

    @Test
    public void testFireChanges() throws Exception {
        Set<NodeInfo> delSet = new HashSet<NodeInfo>();
        delSet.add(AddressUtil.parseAddress("json^rpc#10.254.2.10:8090#368#10100001010"));

        pool.fireChanges(delSet, delSet);
        assertEquals(1, readySet.size());
    }

    @Test
    public void testDestroy() throws Exception {
        pool.destroy();
        pool.destroy();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRetryStub() throws Exception {
        pool.destroy();
        pool.build();
    }

    @Test
    public void testBuild() throws Exception {
        assertEquals(pool, pool.build());
    }

    @Test
    public void testMarkNew() throws Exception {
        Set<NodeInfo> addSet = new HashSet<NodeInfo>();
        addSet.add(AddressUtil.parseAddress("json^rpc#10.254.2.10:8090#368#10100001010"));
        addSet.add(AddressUtil.parseAddress("json^rpc#10.254.2.11:8090#368#10100001010"));

        pool.markNew(addSet);
    }

}

class RetryMock extends RetryStub<DemoService> {

    public RetryMock(Class<DemoService> interfaze, MutableOne<List<String>> mutableOne) {
        super(interfaze, mutableOne);
    }

    @Override
    protected Set<RpcStub> buildClients(NodeInfo info) {
        Set<RpcStub> clientSet = POOL.getClients(info.getAddress());
        clientSet.add(mock(RpcStub.class));
        return clientSet;
    }

}