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

import org.apache.niolex.commons.concurrent.ThreadUtil;
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
 * <br>
 * If user set {@link #forceHeartBeat}, then we will heart beat all the clients,
 * otherwise only heart beat those registered with {@link Config#CODE_REGR_HBEAT}.
 * <br>
 * User can set the heart beat interval with {@link #setHeartBeatInterval(int)}.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-6-14
 */
public class HeartBeatAdapter implements IPacketHandler, WriteEventListener, Runnable {
	private static final Logger LOG = LoggerFactory.getLogger(HeartBeatAdapter.class);

	private static final String KEY = Config.ATTACH_KEY_HEART_BEAT;

	/**
	 * The queue to save all the clients who needs heart beat.
	 * The iterator of this queue will not throw ConcurrentModificationException in multiple
	 * thread modification.
	 */
	private final ConcurrentLinkedQueue<IPacketWriter> clientQueue = new ConcurrentLinkedQueue<IPacketWriter>();

	// The Handler need to be adapted.
	private final IPacketHandler other;

    /**
     * The interval to send heart beat if no packet sent between this time.
     */
    private int heartBeatInterval = Config.SERVER_HEARTBEAT_INTERVAL;

    /**
     * The max heart beat relaxation time. If packet not sent during (heartBeatInterval - maxHeartBeatRelaxation),
     * we will send a heart beat.
     */
    private int maxHeartBeatRelaxation = heartBeatInterval / 8;

    /**
     * The working status of this adapter.
     */
    private volatile boolean isWorking;

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
     * @param other the packet handler to be adapted
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
    	thread = new Thread(this, "HeartBeatAdapter");
    	thread.setDaemon(true);
    	thread.start();
    }

    /**
     * Stop the internal thread.
     */
    public void stop() {
    	isWorking = false;
    	thread.interrupt();
    	ThreadUtil.join(thread);
    }

	/**
	 * {@inheritDoc}
	 *
	 * Override super method
	 * @see org.apache.niolex.network.IPacketHandler#handlePacket(PacketData, IPacketWriter)
	 */
	@Override
	public void handlePacket(PacketData sc, IPacketWriter wt) {
		// If current is force heart beat, we check every time.
		if (forceHeartBeat && wt.getAttached(KEY) == null) {
			registerHeartBeat(wt);
		}
		// Process this packet.
		if (sc.getCode() != Config.CODE_REGR_HBEAT) {
			other.handlePacket(sc, wt);
		} else {
			if (wt.getAttached(KEY) == null) {
				registerHeartBeat(wt);
			}
		}
	}

	/**
	 * Register this writer for heart beat.
	 * 
	 * @param wt the packet writer used to register heart beat
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
	 * @see org.apache.niolex.network.event.WriteEventListener#afterSent(WriteEvent)
	 */
	@Override
	public void afterSent(WriteEvent wEvent) {
		IPacketWriter wt = wEvent.getPacketWriter();
		Long ttm = wt.getAttached(KEY);
		if (ttm != null) {
		    long cttm = System.currentTimeMillis();
		    
		    // We relax attach time stamp for 50ms to increase performance.
		    if (ttm + 50 < cttm)
		        wt.attachData(KEY, cttm);
		}
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.IPacketHandler#handleClose(IPacketWriter)
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
			ThreadUtil.sleep(heartBeatInterval / 4);
		}
	}


    /**
     * Handle the heart beat problem of the writers attached with this adapter.
     */
    protected void handleHeartBeat() {
    	Iterator<IPacketWriter> it = clientQueue.iterator();
    	while (it.hasNext()) {
    		IPacketWriter wt = it.next();
    		Long ttm = wt.getAttached(KEY);
    		if (ttm != null) {
    			// Send heart beat if and only if last send time is earlier than
    			// One heart beat interval.
    			if (ttm + heartBeatInterval < System.currentTimeMillis() + maxHeartBeatRelaxation) {
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
	 * There will be a +-(12.5%) max relaxation for heart beat.
	 *
	 * @param heartBeatInterval the heart beat internal to set in milliseconds
	 */
	public void setHeartBeatInterval(int heartBeatInterval) {
        this.heartBeatInterval = heartBeatInterval;
        this.maxHeartBeatRelaxation = heartBeatInterval / 8;
    }

	/**
	 * Whether do we need to send heart beat to all clients.
	 * If this flag is true, client will not need to register heart beat.
	 *
	 * @param forceHeartBeat the force heart beat flag to set
	 */
	public void setForceHeartBeat(boolean forceHeartBeat) {
		this.forceHeartBeat = forceHeartBeat;
	}

	/**
	 * Return the current working status of this adapter.
	 *
	 * @return the current status
	 */
	public boolean isWorking() {
		return isWorking;
	}

}
