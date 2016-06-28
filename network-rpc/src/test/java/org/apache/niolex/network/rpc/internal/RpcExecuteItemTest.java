package org.apache.niolex.network.rpc.internal;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;

import org.apache.niolex.commons.reflect.MethodUtil;
import org.junit.Test;

public class RpcExecuteItemTest {

    @Test
    public void testGetMethod() throws Exception {
        RpcExecuteItem i = new RpcExecuteItem();
        Method m = MethodUtil.getFirstMethod(this, "testGetMethod");
        i.setMethod(m);
        assertEquals(m, i.getMethod());
    }

    @Test
    public void testGetTarget() throws Exception {
        RpcExecuteItem i = new RpcExecuteItem();
        Exception e = new RuntimeException("not yet implemented");
        i.setTarget(e);
        assertEquals(e, i.getTarget());
    }

}
