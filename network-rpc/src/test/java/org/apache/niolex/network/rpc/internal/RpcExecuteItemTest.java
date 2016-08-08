package org.apache.niolex.network.rpc.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;

import org.apache.niolex.commons.reflect.MethodUtil;
import org.junit.Test;

public class RpcExecuteItemTest {

    @Test
    public void testGetMethod() throws Exception {
        Method m = MethodUtil.getFirstMethod(this, "testGetMethod");
        RpcExecuteItem i = new RpcExecuteItem((short) 3, true, m, this);
        assertEquals(m, i.getMethod());
        assertEquals(this, i.getTarget());
        assertTrue(i.isOneWay());
        assertEquals(3, i.getCode());
    }

    @Test
    public void testGetTarget() throws Exception {
        Exception e = new RuntimeException("not yet implemented");
        RpcExecuteItem i = new RpcExecuteItem((short) 3, false, null, e);
        assertEquals(e, i.getTarget());
    }

}
