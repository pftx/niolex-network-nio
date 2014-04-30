/**
 * IPacketWriter.java
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
package org.apache.niolex.network;

import org.apache.niolex.network.event.WriteEventListener;


/**
 * The Packet Writer Interface.
 * This is the really basic interface for packet handling.
 * The server side and client side core network components will implement it.
 *
 * @author Xie, Jiyun
 *
 */
public interface IPacketWriter {

    /**
     * The string representation of the remote peer. i.e. The IP address.
     *
     * @return the remote peer name
     */
    public String getRemoteName();

    /**
     * Write Packet to the remote peer.
     * You can write as many packets as you want.
     * Please do not write any packets after the channel is closed, or the system
     * Behavior will be undefined.
     * This method will return immediately after the packet is put into the outgoing
     * queue. It will not indicate the packet has been sent.
     *
     * @see org.apache.niolex.network.event.WriteEventListener
     *
     * @param sc The Packet to write
     */
    public void handleWrite(PacketData sc);

    /**
     * WriteEventListener is the listener fired after packet send to client.
     * The {@link #handleWrite(PacketData)} method return immediately after the packet is put into
     * the outgoing queue. This method will tell you the packet is sent by event.
     *
     * @param listener
     */
    public void addEventListener(WriteEventListener listener);

    /**
     * Attach some data to this object, please use your unique key, all system internal data key
     * will start with <b>SYS_</b>, please keep away from them.
     *
     * @param key the user defined key
     * @param value the user attachment
     * @return the current stored object, or null if nothing is stored currently.
     */
    public Object attachData(String key, Object value);

    /**
     * Get the attached data from this Writer, or null if nothing is stored.
     *
     * @param key the attachment key
     * @return the attached object
     */
    public <T> T getAttached(String key);

}
