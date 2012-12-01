/**
 * HeartBeatAdapter.java
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
package org.apache.niolex.network.adapter;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.niolex.network.Config;
import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.event.WriteEvent;
import org.apache.niolex.network.event.WriteEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle the heart beat problem of the writers attached with this adapter.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-14
 */
public class HeartBeatAdapter implements IPacketHandler, WriteEventListener, Runnable {
	private static final Logger LOG = LoggerFactory.getLogger(HeartBeatAdapter.class);

	private static final String KEY = Config.ATTACH_KEY_HEART_BEAT;

	/**
	 * The queue to save all the clients who need heart beat.
	 * The iterator of this queue will not throw ConcurrentModificationException in multiple
	 * thread modification.
	 */
	private ConcurrentLinkedQueue<IPacketWriter> clientQueue = new ConcurrentLinkedQueue<IPacketWriter>();


	// The Handler need to be adapted.
	private IPacketHandler other;

    /**
     * The interval to send heart beat if no packet sent between this time.
     */
    private int heartBeatInterval = Config.SERVER_HEARTBEAT_INTERVAL;

    /**
     * The max heart beat error.
     */
    private int maxHeartBeatError = heartBeatInterval / 8;

    /**
     * The status of this adapter.
     */
    private boolean isWorking;

	/**
	 * The internal thread.
	 */
    private Thread thread;

    /**
     * Whether do we need to send heart beat to all clients.
     */
    private boolean forceHeartBeat;

    /**
     * The constructor of this adapter.
     *
     * @param other
     */
    public HeartBeatAdapter(IPacketHandler other) {
		super();
		this.other = other;
	}

	/**
     * Start the internal thread to send heart beat.
     */
    public void start() {
    	isWorking = true;
    	thread = new Thread(this);
    	thread.setDaemon(true);
    	thread.start();
    }

    /**
     * Stop the internal thread.
     */
    public void stop() {
    	isWorking = false;
    	thread.interrupt();
    	try {
    		thread.join();
		} catch (InterruptedException e) {
			// Failed to join is ok.
		}
    }

	/**
	 * Override super method
	 * @see org.apache.niolex.network.IPacketHandler#handleRead(org.apache.niolex.network.PacketData, org.apache.niolex.network.IPacketWriter)
	 */
	@Override
	public void handleRead(PacketData sc, IPacketWriter wt) {
		// If current is force heart beat, we check every time.
		if (forceHeartBeat && wt.getAttached(KEY) == null) {
			registerHeartBeat(wt);
		}
		// Process this packet.
		if (sc.getCode() != Config.CODE_REGR_HBEAT) {
			other.handleRead(sc, wt);
		} else {
			if (wt.getAttached(KEY) == null) {
				registerHeartBeat(wt);
			}
		}
	}

	/**
	 * Register this writer for heart beat.
	 * @param wt
	 */
	public void registerHeartBeat(IPacketWriter wt) {
		// Attach the current time stamp to the packet writer, and save it to the queue.
		wt.attachData(KEY, System.currentTimeMillis());
		wt.addEventListener(this);
		clientQueue.add(wt);
		LOG.info("Client {} is registerd for heart beat.", wt.getRemoteName());
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.event.WriteEventListener#afterSend(org.apache.niolex.network.event.WriteEvent)
	 */
	@Override
	public void afterSend(WriteEvent wEvent) {
		IPacketWriter wt = wEvent.getPacketWriter();
		Long ttm = wt.getAttached(KEY);
		if (ttm != null) {
	    	wt.attachData(KEY, System.currentTimeMillis());
		}
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.IPacketHandler#handleClose(org.apache.niolex.network.IPacketWriter)
	 */
	@Override
	public void handleClose(IPacketWriter wt) {
		// Clean the time stamp. The client will be removed when fire heart beat.
		wt.attachData(KEY, null);
		other.handleClose(wt);
	}


	/**
	 * Override super method
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		while (isWorking) {
			handleHeartBeat();
			try {
				Thread.sleep(heartBeatInterval / 4);
			} catch (InterruptedException e) {
				// Failed to sleep is ok.
			}
		}
	}


    /**
     * Handle the heart beat problem of the writers attached with this adapter.
     * Any sub class with there own writers will need to invoke this method.
     */
    protected void handleHeartBeat() {
    	Iterator<IPacketWriter> it = clientQueue.iterator();
    	while (it.hasNext()) {
    		IPacketWriter wt = it.next();
    		Long ttm = wt.getAttached(KEY);
    		if (ttm != null) {
    			// Send heart beat if and only if last send time is earlier than
    			// One heart beat interval.
    			if (ttm + heartBeatInterval < System.currentTimeMillis() + maxHeartBeatError) {
    				wt.handleWrite(PacketData.getHeartBeatPacket());
    				wt.attachData(KEY, System.currentTimeMillis());
    			}
        	} else {
        		it.remove();
        	}
        }
    }

    /**
	 * Return The heartBeatInterval
	 */
	public int getHeartBeatInterval() {
        return heartBeatInterval;
    }

    /**
	 * The heartBeatInterval to set
	 * There will be a +-(12.5%) max error for heart beat.
	 *
	 * @param heartBeatInterval
	 */
	public void setHeartBeatInterval(int heartBeatInterval) {
        this.heartBeatInterval = heartBeatInterval;
        this.maxHeartBeatError = heartBeatInterval / 8;
    }

	/**
	 * Whether do we need to send heart beat to all clients.
	 * If this flag is true, client will not need to register heart beat.
	 *
	 * @param forceHeartBeat
	 */
	public void setForceHeartBeat(boolean forceHeartBeat) {
		this.forceHeartBeat = forceHeartBeat;
	}

	/**
	 * Return the status of this adapter
	 * @return
	 */
	public boolean isWorking() {
		return isWorking;
	}

}
