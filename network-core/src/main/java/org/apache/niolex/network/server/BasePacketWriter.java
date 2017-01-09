/**
 * BasePacketWriter.java
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
package org.apache.niolex.network.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.event.WriteEvent;
import org.apache.niolex.network.event.WriteEventListener;

/**
 * The base BasePacketWriter, handle object attach and the outstanding packet storage.
 * PacketData will be stored in a ConcurrentLinkedQueue, which is good at concurrent writes.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-5-30
 */
public abstract class BasePacketWriter implements IPacketWriter {

    /**
     * Attachment map, save all attachments here.
     */
    protected Map<String, Object> attachMap = new HashMap<String, Object>();

    /**
     * Store all the packets need to be sent to client.
     */
    private ConcurrentLinkedQueue<PacketData> sendPacketsQueue = new ConcurrentLinkedQueue<PacketData>();

    /**
     * The packet write event listener list.
     */
    private ArrayList<WriteEventListener> listenerList = new ArrayList<WriteEventListener>(2);

    /**
     * The channel status.
     */
    private volatile boolean isChannelClosed = false;

    /**
     * This is an empty constructor.
     */
    public BasePacketWriter() {
        super();
    }

    /**
     * We put the packet into the internal queue.
     * When channel is closed, we forbid user from send data.
     *
     * @throws IllegalStateException When This Channel is Closed.
     * @see org.apache.niolex.network.IPacketWriter#handleWrite(org.apache.niolex.network.PacketData)
     */
    @Override
    public void handleWrite(PacketData sc) {
        if (isChannelClosed) {
            throw new IllegalStateException("This Channel is Closed.");
        }
        sendPacketsQueue.add(sc);
    }

    /**
     * Override super method
     * 
     * @see org.apache.niolex.network.IPacketWriter#addEventListener(org.apache.niolex.network.event.WriteEventListener)
     */
    @Override
    public void addEventListener(WriteEventListener listener) {
        listenerList.add(listener);
    }

    /**
     * Sub class need to use this method to clean all the internal
     * data structure and mark this channel as closed.
     *
     * Please invoke super method when you try to override.
     */
    protected void channelClosed() {
        if (isChannelClosed) {
            return;
        }
        isChannelClosed = true;
        attachMap.clear();
        attachMap = null;
        // We do not clear this queue, because some adapter might want to use it.
        // But we still need to set it to null to help GC.
        sendPacketsQueue = null;
        listenerList.clear();
        listenerList = null;
    }

    /**
     * Sub class need to use this method to fire send event.
     * Please do not override.
     *
     * @param sc the packet sent just now
     */
    protected void fireSendEvent(PacketData sc) {
        WriteEvent wEvent = new WriteEvent(this, sc);
        for (WriteEventListener listener : listenerList) {
            listener.afterSent(wEvent);
        }
    }

    /**
     * Sub class need to use this method to get packets to send to client.
     *
     * @return the next packet to be sent to client
     */
    protected PacketData handleNext() {
        return sendPacketsQueue.poll();
    }

    /**
     * Override super method
     * 
     * @see org.apache.niolex.network.IPacketWriter#attachData(java.lang.String, java.lang.Object)
     */
    @Override
    public Object attachData(String key, Object value) {
        return attachMap.put(key, value);
    }

    /**
     * Override super method
     * 
     * @see org.apache.niolex.network.IPacketWriter#getAttached(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAttached(String key) {
        if (isChannelClosed) {
            return null;
        }
        return (T) attachMap.get(key);
    }

    /**
     * Return whether the send packets queue is empty or not.
     *
     * @return true if it's empty
     */
    public boolean isEmpty() {
        return sendPacketsQueue.isEmpty();
    }

    /**
     * Get the current non send packet queue.
     *
     * @return the current queue
     */
    public ConcurrentLinkedQueue<PacketData> getRemainQueue() {
        return sendPacketsQueue;
    }

}
