package org.apache.niolex.network.cli.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;

import com.google.common.collect.Lists;

public class ThreadLocalLogContextTest {

    private static final ExecutorService tPool = Executors.newFixedThreadPool(2);
    private static final ThreadLocalLogContext tCon = ThreadLocalLogContext.instance();

    public static class A implements Callable<String> {

        /**
         * This is the override of super method.
         * 
         * @see java.util.concurrent.Callable#call()
         */
        @Override
        public String call() throws Exception {
            tCon.setLogPrefix(Thread.currentThread().getName());
            return tCon.getLogPrefix();
        }

    }

    @Test
    public void testWireup() throws Exception {
        Collection<A> tasks = Lists.newArrayList(new A(), new A());
        List<Future<String>> invokeAll = tPool.invokeAll(tasks);

        assertEquals(2, invokeAll.size());
        assertNotEquals(invokeAll.get(0).get(), invokeAll.get(1).get());
    }

    @Test
    public void testInstance() throws Exception {
        Collection<A> tasks = Lists.newArrayList(new A(), new A(), new A(), new A());
        List<Future<String>> invokeAll = tPool.invokeAll(tasks);

        assertEquals(4, invokeAll.size());
        assertNotEquals(invokeAll.get(0).get(), invokeAll.get(1).get());
    }

}
