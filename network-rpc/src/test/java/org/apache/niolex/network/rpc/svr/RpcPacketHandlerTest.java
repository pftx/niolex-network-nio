/**
 * RpcPacketHandlerTest.java
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
package org.apache.niolex.network.rpc.svr;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.apache.niolex.commons.reflect.MethodUtil;
import org.apache.niolex.network.CoreRunner;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.demo.json.RpcService;
import org.apache.niolex.network.demo.json.RpcServiceImpl;
import org.apache.niolex.network.rpc.IConverter;
import org.apache.niolex.network.rpc.RpcException;
import org.apache.niolex.network.rpc.conv.JsonConverter;
import org.apache.niolex.network.rpc.svr.RpcPacketHandler;
import org.apache.niolex.network.rpc.util.RpcUtil;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-6-4
 */
public class RpcPacketHandlerTest {

	/**
	 * Test method for
	 * {@link org.apache.niolex.network.rpc.svr.RpcPacketHandler#handlePacket(org.apache.niolex.network.PacketData, org.apache.niolex.network.IPacketWriter)}
	 * .
	 */
	@Test
	public void testRpcPacketHandler() {
		RpcPacketHandler rr = new RpcPacketHandler(3);
		rr.setConverter(new JsonConverter());
		IPacketWriter tt = mock(IPacketWriter.class);
		rr.handlePacket(new PacketData(5, new byte[5]), tt);
		verify(tt).handleWrite(any(PacketData.class));
		assertEquals(0, rr.getQueueSize());
	}

	@Test
	public void testHandleHB() {
		RpcPacketHandler rr = new RpcPacketHandler();
		IPacketWriter tt = mock(IPacketWriter.class);
		rr.handlePacket(PacketData.getHeartBeatPacket(), tt);
		ArgumentCaptor<PacketData> au = ArgumentCaptor.forClass(PacketData.class);
        verify(tt, times(1)).handleWrite(au.capture());
        assertEquals(0, au.getValue().getCode());
        assertEquals(1, au.getValue().getVersion());
        assertEquals(1, au.getValue().getReserved());
        RpcException r = RpcUtil.parseRpcException(au.getValue().getData());
        assertEquals(RpcException.Type.METHOD_NOT_FOUND, r.getType());
        assertNull(r.getCause());
	}

	/**
	 * Test method for
	 * {@link org.apache.niolex.network.rpc.svr.RpcPacketHandler#handleClose(org.apache.niolex.network.IPacketWriter)}.
	 */
	@Test
	public void setConfigs() {
		RpcPacketHandler rr = new RpcPacketHandler(3, new JsonConverter());
		ConfigItem[] confs = new ConfigItem[2];
		ConfigItem c = new ConfigItem();
		c.setInterface(RpcService.class);
		c.setTarget(new RpcServiceImpl());
		confs[0] = c;
		confs[1] = c;
		rr.setRpcConfigs(confs);
	}

	/**
	 * Test method for
	 * {@link org.apache.niolex.network.rpc.svr.RpcPacketHandler#handleClose(org.apache.niolex.network.IPacketWriter)}.
	 */
	@Test
	public void testHandleClose() {
		RpcPacketHandler rr = new RpcPacketHandler(1);
		rr.handleClose(null);
	}

	@Test
	public void testHandleNe() throws Exception {
	    RpcPacketHandler rr = new RpcPacketHandler(3);
        rr.setConverter(new JsonConverter());
        ConfigItem[] confs = new ConfigItem[1];
        ConfigItem c = new ConfigItem();
        c.setInterface(RpcService.class);
        c.setTarget(new RpcServiceImpl());
        confs[0] = c;
        rr.setRpcConfigs(confs);
        PacketData p = new PacketData(16, new byte[9]);
        p.setVersion((byte) 79);
        p.setReserved((byte) 127);
        IPacketWriter wt = mock(IPacketWriter.class);
        rr.handlePacket(p, wt);
        Thread.sleep(3 * CoreRunner.CO_SLEEP);
        ArgumentCaptor<PacketData> au = ArgumentCaptor.forClass(PacketData.class);
        verify(wt).handleWrite(au.capture());
        assertEquals(16, au.getValue().getCode());
        assertEquals(79, au.getValue().getVersion());
        assertEquals(-128, au.getValue().getReserved());
	}

	@Test
	public void testHandleNeg() throws Exception {
		RpcPacketHandler rr = new RpcPacketHandler(3);
		rr.setConverter(new JsonConverter());
		ConfigItem[] confs = new ConfigItem[1];
		ConfigItem c = new ConfigItem();
		c.setInterface(RpcService.class);
		c.setTarget(new RpcServiceImpl());
		confs[0] = c;
		rr.setRpcConfigs(confs);
		PacketData p = new PacketData(14, new byte[9]);
		p.setVersion((byte) 79);
		p.setReserved((byte) 99);
		IPacketWriter wt = mock(IPacketWriter.class);
		rr.handlePacket(p, wt);
		Thread.sleep(3 * CoreRunner.CO_SLEEP);
		ArgumentCaptor<PacketData> au = ArgumentCaptor.forClass(PacketData.class);
		verify(wt).handleWrite(au.capture());
		assertEquals(14, au.getValue().getCode());
		assertEquals(79, au.getValue().getVersion());
		assertEquals(100, au.getValue().getReserved());
	}

	@Test
	public void testHandleReturn() throws Exception {
	    RpcPacketHandler rr = new RpcPacketHandler(3);
	    IConverter mm = mock(IConverter.class);
	    when(mm.prepareParams(any(byte[].class), any(Type[].class))).thenThrow(new Error("This is your fault."));
        rr.setConverter(mm);
	    ConfigItem[] confs = new ConfigItem[1];
	    ConfigItem c = new ConfigItem();
	    c.setInterface(RpcService.class);
	    c.setTarget(new RpcServiceImpl());
	    confs[0] = c;
	    rr.setRpcConfigs(confs);
	    PacketData p = new PacketData(14, new byte[9]);
	    p.setVersion((byte) 79);
	    p.setReserved((byte) 99);
	    IPacketWriter wt = mock(IPacketWriter.class);
	    rr.handlePacket(p, wt);
	    Thread.sleep(3 * CoreRunner.CO_SLEEP);
	    ArgumentCaptor<PacketData> au = ArgumentCaptor.forClass(PacketData.class);
	    verify(wt, never()).handleWrite(au.capture());
	}

    @Test
    public void testAddRpcConfigObject() throws Exception {
        RpcPacketHandler rr = new RpcPacketHandler(3);
        rr.addRpcConfig("Lex");
    }

    @Test
    public void testGetQueueSize() throws Exception {
        RpcPacketHandler rr = new RpcPacketHandler(3);
        Method m = MethodUtil.getFirstMethod(rr, "handleReturn");
        m.setAccessible(true);
        PacketData p = new PacketData(14, new byte[9]);
        p.setVersion((byte) 79);
        p.setReserved((byte) 127);
        IPacketWriter wt = mock(IPacketWriter.class);
        ArgumentCaptor<PacketData> au = ArgumentCaptor.forClass(PacketData.class);
        m.invoke(rr, p, wt, null, 1);
        verify(wt).handleWrite(au.capture());
        assertEquals(14, au.getValue().getCode());
        assertEquals(79, au.getValue().getVersion());
        assertEquals(-128, au.getValue().getReserved());
        assertEquals(0, au.getValue().getLength());
    }

    @Test
    public void testSetConverter() throws Exception {
        RpcPacketHandler rr = new RpcPacketHandler(3);
        Method m = MethodUtil.getFirstMethod(rr, "handleReturn");
        m.setAccessible(true);
        PacketData p = new PacketData(14, new byte[9]);
        p.setVersion((byte) 79);
        p.setReserved((byte) 127);
        IPacketWriter wt = mock(IPacketWriter.class);
        m.invoke(rr, p, wt, "aabbc", 0);
    }

}
