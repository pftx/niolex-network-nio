/**
 * BufferManager.java
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
package org.apache.niolex.network.server;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.niolex.commons.util.BufferUtil;
import org.apache.niolex.network.Config;

/**
 * This class is used to manage direct buffer. Because allocate and free direct buffer
 * is very time consuming, so we manage it here instead of give it to GC.
 *
 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2013-11-22
 */
public class BufferManager {

    /**
     * The direct send buffer size.
     */
    private static final int DIRECT_BUFFER_SIZE = Config.SERVER_DIRECT_BUFFER_SIZE;

    /**
     * The internal queue used to manage all the buffers.
     */
    private final ConcurrentLinkedQueue<ByteBuffer> bufferQueue = new ConcurrentLinkedQueue<ByteBuffer>();

    /**
     * The counter used to count the current queue size.
     */
    private final AtomicInteger currentQueueSize = new AtomicInteger();

    /**
     * The max queue size.
     */
    private int maxQueueSize = Config.BUFFER_MGR_MAX_QUEUE_SIZE;

    /**
     * Get a direct buffer from this buffer manager.
     *
     * @return a direct buffer
     */
    public ByteBuffer getBuffer() {
        // Use the buffer from queue first.
        ByteBuffer buf = bufferQueue.poll();
        if (buf == null) {
            buf = ByteBuffer.allocateDirect(DIRECT_BUFFER_SIZE);
        } else {
            currentQueueSize.decrementAndGet();
        }
        return buf;
    }

    /**
     * Give this direct buffer back to the manager.
     *
     * @param directBuffer the buffer
     * @return true if we accept this buffer, false if we dropped it
     */
    public boolean giveBack(ByteBuffer directBuffer) {
        if (currentQueueSize.incrementAndGet() <= maxQueueSize) {
            bufferQueue.add(directBuffer);
            return true;
        } else {
            // Clean native memory as fast as possible.
            BufferUtil.cleanNativeMem(directBuffer);
            currentQueueSize.decrementAndGet();
            return false;
        }
    }

    /**
     * @return the max queue size
     */
    public int getMaxQueueSize() {
        return maxQueueSize;
    }

    /**
     * @param maxQueueSize the max queue size to set
     */
    public void setMaxQueueSize(int maxQueueSize) {
        this.maxQueueSize = maxQueueSize;
    }

}
