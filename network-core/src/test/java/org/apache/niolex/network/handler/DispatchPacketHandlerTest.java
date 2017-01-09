/**
 * DispatchPacketHandlerTest.java
 *
 * Copyright 2011 Niolex, Inc.
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
package org.apache.niolex.network.handler;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.handler.DispatchPacketHandler.Handle;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DispatchPacketHandlerTest {

    DispatchPacketHandler handler;

    @Mock
    IPacketHandler pHandler1;

    @Mock
    IPacketHandler pHandler2;

    @Mock
    IPacketHandler pHandler3;

    @Before
    public void setUp() {
        handler = new DispatchPacketHandler();
        handler.addHandler((short) 3, pHandler1);
        handler.addHandler((short) 5, pHandler2);
        handler.setDefaultHandler(pHandler3);
    }
    
    @Test
    public void testHandle() {
        Handle h = new Handle((short)3, pHandler1);
        assertEquals(pHandler1, h.getHandler());
        assertEquals(3, h.getEndCode());
        assertEquals(3, h.getStartCode());
        assertEquals("[3 - 3]", h.toString());
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testAddRangeHandler() {
        IPacketHandler pHandler4 = mock(IPacketHandler.class);
        IPacketHandler pHandler5 = mock(IPacketHandler.class);
        handler.addHandler(7, 8, pHandler4);
        handler.addHandler(5, 6, pHandler5);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testAddHandler() {
        handler.addHandler(9,7, handler);
    }
    
    @Test
    public void testAddRangeHandlerOverlap() {
        IPacketHandler pHandler4 = new SummaryPacketHandler();
        handler.addHandler(8, 90, pHandler4);
        try {
            handler.addHandler(50, 51, handler);
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            assertEquals("Overlapped with another handler. Handler class: org.apache.niolex.network.handler.SummaryPacketHandler, range: [8, 90].", e.getMessage());
        }
        try {
            handler.addHandler(6, 9, handler);
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            assertEquals("Overlapped with another handler. Handler class: org.apache.niolex.network.handler.SummaryPacketHandler, range: [8, 90].", e.getMessage());
        }
        try {
            handler.addHandler(88, 99, handler);
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            assertEquals("Overlapped with another handler. Handler class: org.apache.niolex.network.handler.SummaryPacketHandler, range: [8, 90].", e.getMessage());
        }
        try {
            handler.addHandler(6, 99, handler);
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            assertEquals("Overlapped with another handler. Handler class: org.apache.niolex.network.handler.SummaryPacketHandler, range: [8, 90].", e.getMessage());
        }
        try {
            handler.addHandler((short)66, handler);
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            assertEquals("Overlapped with another handler. Handler class: org.apache.niolex.network.handler.SummaryPacketHandler, range: [8, 90].", e.getMessage());
        }
    }
    
    @Test
    public void testAddRangeHandlerOverlapChange() {
        IPacketHandler pHandler4 = new SummaryPacketHandler();
        IPacketHandler pHandler5 = new SummaryPacketHandler();
        handler.addHandler(80, 90, pHandler4);
        try {
            handler.addHandler(50, 81, handler);
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            assertEquals("Overlapped with another handler. Handler class: org.apache.niolex.network.handler.SummaryPacketHandler, range: [80, 90].", e.getMessage());
        }
        handler.addHandler(66, 77, pHandler5);
        try {
            handler.addHandler(50, 81, handler);
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            assertEquals("Overlapped with another handler. Handler class: org.apache.niolex.network.handler.SummaryPacketHandler, range: [66, 77].", e.getMessage());
        }
    }

    @Test
    public void testHandleClose() {
        PacketData sc = mock(PacketData.class);
        when(sc.getCode()).thenReturn((short) 3, (short) 3, (short) 4);
        IPacketWriter ip = mock(IPacketWriter.class);
        handler.handlePacket(sc, ip);
        handler.handlePacket(sc, ip);
        handler.handlePacket(sc, ip);
        handler.handlePacket(sc, ip);
        handler.handleClose(ip);
        handler.handleClose(ip);
        handler.handleClose(ip);
        verify(pHandler1, times(3)).handleClose(ip);
        verify(pHandler2, times(3)).handleClose(ip);
        verify(pHandler3, times(3)).handleClose(ip);
        verify(pHandler1, times(2)).handlePacket(sc, ip);
        verify(pHandler2, times(0)).handlePacket(sc, ip);
        verify(pHandler3, times(2)).handlePacket(sc, ip);
        assertEquals(2, handler.getDispatchSize());
    }

    @Test
    public void testHandlePacket_NoDefault() {
        PacketData sc = mock(PacketData.class);
        when(sc.getCode()).thenReturn((short) 3, (short) 3, (short) 4, (short) 4, (short) 5);
        IPacketWriter ip = mock(IPacketWriter.class);
        handler.setDefaultHandler(null);
        handler.handlePacket(sc, ip);
        handler.handlePacket(sc, ip);
        handler.handlePacket(sc, ip);
        handler.handlePacket(sc, ip);
        handler.handlePacket(sc, ip);
        verify(pHandler1, times(2)).handlePacket(sc, ip);
        verify(pHandler2, times(1)).handlePacket(sc, ip);
        verify(pHandler3, times(0)).handlePacket(sc, ip);
        assertEquals(2, handler.getDispatchSize());
        handler.setDefaultHandler(pHandler3);
    }

    @Test
    public void testHandlePacket() {
        PacketData sc = mock(PacketData.class);
        when(sc.getCode()).thenReturn((short) 3, (short) 3, (short) 4, (short) 4, (short) 5);
        IPacketWriter ip = mock(IPacketWriter.class);
        handler.handlePacket(sc, ip);
        handler.handlePacket(sc, ip);
        handler.handlePacket(sc, ip);
        handler.handlePacket(sc, ip);
        handler.handlePacket(sc, ip);
        verify(pHandler1, times(2)).handlePacket(sc, ip);
        verify(pHandler2, times(1)).handlePacket(sc, ip);
        verify(pHandler3, times(2)).handlePacket(sc, ip);
        assertEquals(2, handler.getDispatchSize());
    }
    
    @Test
    public void testHandlePacketOverlap() {
        IPacketHandler pHandler4 = mock(IPacketHandler.class);
        IPacketHandler pHandler5 = mock(IPacketHandler.class);
        handler.addHandler(7, 8, pHandler4);
        handler.addHandler(1, 2, pHandler5);
        
        PacketData sc = mock(PacketData.class);
        when(sc.getCode()).thenReturn((short) 0, (short) 1, (short) 2, (short) 1, (short) 0, (short) 3, (short) 3, (short) 4, (short) 4, (short) 5, (short) 6, (short) 7, (short) 8, (short) 9, (short) 8);
        IPacketWriter ip = mock(IPacketWriter.class);
        for (int i = 0; i < 15; ++i)
            handler.handlePacket(sc, ip);
        
        verify(pHandler1, times(2)).handlePacket(sc, ip); // 3
        verify(pHandler2, times(1)).handlePacket(sc, ip); // 5
        verify(pHandler3, times(6)).handlePacket(sc, ip); // d
        verify(pHandler4, times(3)).handlePacket(sc, ip);
        verify(pHandler5, times(3)).handlePacket(sc, ip);
        assertEquals(4, handler.getDispatchSize());
    }

    @Test
    public void testSetDefaultHandler() throws Exception {
        assertEquals(pHandler3, handler.getDefaultHandler());
    }

}
