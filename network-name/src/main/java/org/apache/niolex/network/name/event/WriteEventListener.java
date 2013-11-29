/**
 * WriteEventListener.java
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

import org.apache.niolex.commons.event.Event;
import org.apache.niolex.commons.event.EventListener;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;

/**
 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2013-11-29
 */
public class WriteEventListener extends EventListener<Event<PacketData>> {

    /**
     * Attach the event listener.
     */
    public static final String ATTACH_KEY = "SYS_HAND_EVENT_LISTN";

    /**
     * Obtain the write event listener from this packet writer.
     * We will first find from the attachments, if not found, we create a new one
     * and attach it to this writer.
     *
     * @param w the packet writer
     * @return the write event listener
     */
    public static final WriteEventListener obtain(IPacketWriter w) {
        WriteEventListener listn = w.getAttached(ATTACH_KEY);
        if (listn == null) {
            listn = new WriteEventListener(w);
            w.attachData(ATTACH_KEY, listn);
        }
        return listn;
    }

    private final IPacketWriter writer;

    /**
     * The private constructor. Please use the static method {@link #obtain(IPacketWriter)}.
     *
     * @param writer
     */
    private WriteEventListener(IPacketWriter writer) {
        super();
        this.writer = writer;
    }

    /**
     * This is the override of super method.
     * @see org.apache.niolex.commons.event.EventListener#eventHappened(Event)
     */
    @Override
    public void eventHappened(Event<PacketData> e) {
        writer.handleWrite(e.getEventValue());
    }

}
