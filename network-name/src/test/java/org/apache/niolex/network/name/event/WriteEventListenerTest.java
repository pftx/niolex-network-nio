/**
 * WriteEventListenerTest.java
 *
 * Copyright 2013 the original author or authors.
 *
 * We licenses this file to you under the Apache License, version 2.0
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
package org.apache.niolex.network.name.event;


import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import org.apache.niolex.commons.event.Event;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2013-11-29
 */
public class WriteEventListenerTest {

    @Test
    public void testObtain() throws Exception {
        IPacketWriter w = mock(IPacketWriter.class);
        WriteEventListener l = WriteEventListener.obtain(w);
        verify(w).getAttached(WriteEventListener.ATTACH_KEY);
        verify(w).attachData(WriteEventListener.ATTACH_KEY, l);
        when(w.getAttached(WriteEventListener.ATTACH_KEY)).thenReturn(l);
        WriteEventListener r = WriteEventListener.obtain(w);
        assertEquals(l, r);
    }

    @Test
    public void testEventHappened() throws Exception {
        IPacketWriter w = mock(IPacketWriter.class);
        WriteEventListener l = WriteEventListener.obtain(w);
        PacketData sc = new PacketData(33);
        l.eventHappened(new Event<PacketData>("abc", sc));
        verify(w).handleWrite(sc);
    }

}
