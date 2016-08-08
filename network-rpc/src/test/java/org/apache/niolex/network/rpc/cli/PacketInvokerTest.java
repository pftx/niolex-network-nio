/**
 * BaseInvokerTest.java
 *
 * Copyright 2012 Niolex, Inc.
 *
 * Niolex licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.apache.niolex.network.rpc.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;

import org.apache.niolex.commons.reflect.MethodUtil;
import org.apache.niolex.network.ConnStatus;
import org.apache.niolex.network.IClient;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.client.PacketClient;
import org.apache.niolex.network.demo.json.RpcService;
import org.apache.niolex.network.rpc.RpcException;
import org.apache.niolex.network.rpc.conv.JsonConverter;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-6-13
 */
public class PacketInvokerTest {

	@Test
	public void testInvoke() throws Exception {
        final IClient client = mock(IClient.class);
        final BaseInvoker in = new BaseInvoker(client);
        in.connect();
		final PacketData rc = new PacketData(56, new byte[76]);
		rc.setVersion((byte) 77);
		rc.setReserved((byte) 127);
		final CountDownLatch latch = new CountDownLatch(1);
		final CountDownLatch latch2 = new CountDownLatch(1);
		final PacketData qq = rc.clone();
		rc.setReserved((byte) 128);
		Thread r = new Thread() {
			public void run() {
				latch2.countDown();
                PacketData sc = in.invoke(rc);
				assertEquals(sc, qq);
				latch.countDown();
			}
		};
		r.start();
		latch2.await();
		Thread.sleep(50);
		in.handlePacket(qq, client);
		latch.await();
	}

	/**
     * Test method for
     * {@link org.apache.niolex.network.rpc.BaseInvoker#handleClose(org.apache.niolex.network.IPacketWriter)}.
     * 
     * @throws InterruptedException
     * @throws IOException
     */
	@Test
    public void testInvokeTimeout() throws InterruptedException, IOException {
        final IClient client = mock(IClient.class);
        final BaseInvoker in = new BaseInvoker(client);
        in.connect();
		final PacketData rc = new PacketData(56, new byte[76]);
		final CountDownLatch latch = new CountDownLatch(1);
		Thread r = new Thread() {
			public void run() {
				latch.countDown();
                try {
                Object o = in.invoke(rc);
				assertNull(o);
                } catch (RpcException e) {
                    assertEquals(e.getType(), RpcException.Type.INTERRUPTED);
                }
			}
		};
		r.start();
		latch.await();
		assertTrue(!r.isInterrupted());
		r.interrupt();
		Thread.sleep(10);
		r.join();
	}

    /**
     * Test method for
     * {@link org.apache.niolex.network.rpc.BaseInvoker#handleClose(org.apache.niolex.network.IPacketWriter)}.
     */
    @Test
    public void testHandlePacketIgnore() {
        final IClient client = mock(IClient.class);
        BaseInvoker in = new BaseInvoker(client);
        in.handlePacket(PacketData.getHeartBeatPacket(), null);
    }

	@Test
	public void testHandleClose() throws Exception {
        final IClient client = mock(IClient.class);
        BaseInvoker in = new BaseInvoker(client);
        in.setSleepBetweenRetryTime(10);
        in.setConnectRetryTimes(2);
	    in.handleClose(null);
	}

    @Test
    public void testHandleCloseNoRetry() throws Exception {
        final IClient client = mock(IClient.class);
        BaseInvoker in = new BaseInvoker(client);
        in.setSleepBetweenRetryTime(10);
        in.setConnectRetryTimes(2);
        in.stop();
        in.handleClose(null);
    }

	/**
     * Test method for {@link org.apache.niolex.network.rpc.BaseInvoker#getRpcHandleTimeout()}.
     */
	@Test
	public void testGetRpcHandleTimeout() {
        final IClient client = mock(IClient.class);
        BaseInvoker in = new BaseInvoker(client);
		in.setRpcHandleTimeout(412312);
		assertEquals(412312, in.getRpcHandleTimeout());
	}

    @Test
    public void testIsException() throws Throwable {
        final IClient client = mock(IClient.class);
        BaseInvoker in = new BaseInvoker(client);
        RpcStub rr = new RpcStub(in, new JsonConverter());

        rr.connect(); // 1
        assertTrue(rr.isReady());
        rr.connect(); // 2
        assertTrue(rr.isReady());

        Method m = MethodUtil.getFirstMethod(rr, "isException");
        m.setAccessible(true);
        assertTrue((Boolean) m.invoke(rr, 1));
        assertTrue((Boolean) m.invoke(rr, -255));

        rr.stop(); // -- stopped
        assertFalse(rr.isReady());
        rr.stop(); // -- this time will be skipped
        assertFalse(rr.isReady());
    }

    @Test
    public void testHandleCloseAlreadyClosed() throws Throwable {
        PacketClient pc = new PacketClient(new InetSocketAddress("localhost", 8808));
        BaseInvoker in = new BaseInvoker(pc);
        RpcStub rr = new RpcStub(in, new JsonConverter());
        rr.addInferface(RpcService.class);
        rr.getServiceUrl();
        in.setSleepBetweenRetryTime(10);
        Assert.assertEquals(10, in.getSleepBetweenRetryTime());
        in.setConnectTimeout(120);
        rr.stop();
        in.handleClose(pc);
    }

    @Test
    public void testHandleCloseFailedToReconnect() throws Throwable {
        PacketClient pc = new PacketClient(new InetSocketAddress("localhost", 8808));
        BaseInvoker in = new BaseInvoker(pc);
        RpcStub rr = new RpcStub(in, new JsonConverter());
        rr.addInferface(RpcService.class);
        in.setSleepBetweenRetryTime(10);
        in.setConnectTimeout(10);
        in.setConnectRetryTimes(1);
        Assert.assertEquals(1, in.getConnectRetryTimes());
        in.handleClose(pc);
    }

    @Test
    public void testPrepareReturn() throws Exception {
        BaseInvoker in = mock(BaseInvoker.class);
        RpcStub rr = new RpcStub(in, new JsonConverter());
        assertNull(rr.prepareReturn(null, null, false));
        assertNull(rr.prepareReturn(null, void.class, false));
    }

    @Test
    public void testSetServerAddressString() throws Exception {
        BaseInvoker pc = new BaseInvoker(new PacketClient());
        RpcStub rr = new RpcStub(pc, new JsonConverter());
        rr.addInferface(RpcService.class);
        pc.setServerAddress("localhost:9876");
        assertFalse(pc.isReady());
        assertEquals(ConnStatus.INNITIAL, pc.getConnStatus());
    }

}
