package org.apache.niolex.network.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

import org.apache.niolex.commons.util.Runner;
import org.apache.niolex.network.cli.context.ThreadLocalLogContext;
import org.junit.Before;
import org.junit.Test;

public class ThreadLocalLogContextTest {
    
    private ThreadLocalLogContext context;
    
    @Before
    public void wireup() {
        context = new ThreadLocalLogContext();
        LogContext.setInstance(context);
    }
    
    public void getLogPrefixNewThread() throws Exception {
        assertNull(LogContext.prefix());
        context.setLogPrefix("GoUp!");
        assertEquals("GoUp!", LogContext.prefix());
        LogContext.serviceUrl("rtp://www.a.b.com");
        assertEquals("rtp://www.a.b.com", context.getServiceUrl());
    }

    @Test
    public void testGetLogPrefix() throws Exception {
        context.setLogPrefix("LDAP!");
        Runner.run(this, "getLogPrefixNewThread").join();
        assertEquals("LDAP!", LogContext.prefix());
        assertNull(context.getServiceUrl());
    }

    @Test
    public void testSetServiceUrl() throws Exception {
        LogContext.serviceUrl("dup://2.3.4.5");
        Runner.run(this, "getLogPrefixNewThread").join();
        assertNull(LogContext.prefix());
        assertEquals("dup://2.3.4.5", context.getServiceUrl());
    }

    @Test
    public void testWireup() throws Exception {
        assertNotEquals(context, ThreadLocalLogContext.instance());
        ThreadLocalLogContext.wireup();
    }

    @Test
    public void testInstance() throws Exception {
        assertNotEquals(context, ThreadLocalLogContext.instance());
    }

}
