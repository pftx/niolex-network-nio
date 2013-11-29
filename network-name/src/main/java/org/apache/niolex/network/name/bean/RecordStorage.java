/**
 * RecordStorage.java
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
package org.apache.niolex.network.name.bean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.niolex.commons.concurrent.ConcurrentUtil;
import org.apache.niolex.commons.event.IEventDispatcher;
import org.apache.niolex.commons.util.Runme;
import org.apache.niolex.network.name.bean.AddressRecord.Status;
import org.apache.niolex.network.name.core.Context;

/**
 * Store all the address records in this class.
 * This is for faster serving, we will need to store them in DB in the future.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-6-21
 */
public class RecordStorage extends Runme {

    /**
     * This class is used to store all the records for one address key.
     * We use concurrent hash map to avoid any lock.
     *
     * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
     * @version 1.0.0
     * @since 2013-11-28
     */
    private class RecordContainer {
        // The map to store all the records for one address key.
        private final ConcurrentHashMap<String, AddressRecord> recordMap =
                new ConcurrentHashMap<String, AddressRecord>();

        /**
         * Add this bean as an address record.
         *
         * @param bean the address register bean
         * @return the address record associated with this bean
         */
        public AddressRecord addRecord(AddressRegiBean bean) {
            AddressRecord rec = recordMap.get(bean.getAddressValue());
            if (rec == null) {
                rec = ConcurrentUtil.initMap(recordMap, bean.getAddressValue(), new AddressRecord(bean));
            }
            rec.setStatus(Status.OK);
            return rec;
        }

        /**
         * @return  <tt>true</tt> if this record container is empty
         * @see java.util.concurrent.ConcurrentHashMap#isEmpty()
         */
        public boolean isEmpty() {
            return recordMap.isEmpty();
        }

        /**
         * Delete useless address records.
         */
        public void deleteGarbage() {
            Iterator<Entry<String, AddressRecord>> iter = recordMap.entrySet().iterator();
            while (iter.hasNext()) {
                AddressRecord rec = iter.next().getValue();
                switch (rec.getStatus()) {
                    case DEL:
                        if (rec.getLastTime() + deleteTime < System.currentTimeMillis()) {
                            iter.remove();
                        }
                        break;
                    case DISCONNECTED:
                        if (rec.getLastTime() + deleteTime < System.currentTimeMillis()) {
                            rec.setStatus(Status.DEL);
                            Context.fireEvent(dispatcher, rec);
                        }
                        break;
                }//End of switch
            }//End of inner while
        }

        /**
         * Get all the addresses in this container.
         *
         * @return the address list
         */
        public List<String> getAddress() {
            List<String> list = new ArrayList<String>();
            for (AddressRecord rec : recordMap.values()) {
                if (rec.getStatus() != Status.DEL) {
                    list.add(rec.getAddressValue());
                }
            }
            return list;
        }

    }

	/**
	 * The total storage, it's just a concurrent hash map.
	 */
	private final ConcurrentHashMap<String, RecordContainer> mapStorage = new ConcurrentHashMap<String, RecordContainer>();

	// 当服务过期后发布实现的分发器
	private final IEventDispatcher dispatcher;

	// 当服务断线后需要保留的时间
	private int deleteTime;

	/**
     * Construct a record storage with the specified parameters.
     *
     * @param dispatcher the event dispatcher
     * @param deleteTime the delete time after server disconnected
     */
    public RecordStorage(IEventDispatcher dispatcher, int deleteTime) {
        super();
        this.dispatcher = dispatcher;
        this.deleteTime = deleteTime;
    }

    /**
	 * Store the record into memory.
	 *
	 * @param bean the address record
	 * @return the address record associated with this bean
	 */
	public AddressRecord store(AddressRegiBean bean) {
	    RecordContainer container = mapStorage.get(bean.getAddressKey());
		if (container == null) {
		    container = ConcurrentUtil.initMap(mapStorage, bean.getAddressKey(), new RecordContainer());
		}
		return container.addRecord(bean);
	}

	/**
	 * Delete old garbage data from memory.
	 */
	@Override
	public void runMe() {
	    Iterator<Entry<String, RecordContainer>> iter = mapStorage.entrySet().iterator();
		while (iter.hasNext()) {
		    Entry<String, RecordContainer> mapEntry = iter.next();
		    RecordContainer container = mapEntry.getValue();
		    container.deleteGarbage();
		    if (container.isEmpty()) {
		        iter.remove();
		    }
		}
	}

	/**
	 * Get all the addresses by this address key.
	 *
	 * @param addressKey the address key
	 * @return the address list, or null if key not found
	 */
	public List<String> getAddress(String addressKey) {
	    RecordContainer container = mapStorage.get(addressKey);
        if (container != null) {
            return container.getAddress();
		}
		return null;
	}

	public int getDeleteTime() {
		return deleteTime;
	}

	public void setDeleteTime(int deleteTime) {
		this.deleteTime = deleteTime;
	}

}
