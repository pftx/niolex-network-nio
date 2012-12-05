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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.niolex.network.name.bean.AddressRecord.Status;
import org.apache.niolex.network.name.event.IDispatcher;

/**
 * Store all the records in this class. We can store them in DB after worlds.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-6-21
 */
public class RecordStorage {

	/**
	 * The total storage.
	 */
	private final Map<String, Map<String, AddressRecord>> mapStorage = new ConcurrentHashMap<String, Map<String, AddressRecord>>();
	private final Lock createLock = new ReentrantLock();
	// 当服务断线后需要保留的时间
	private int deleteTime;
	// 当服务过期后发布实现的分发器
	private IDispatcher dispatcher;

	/**
	 * Store the records into memory.
	 *
	 * @param bean
	 * @return
	 */
	public AddressRecord store(AddressRegiBean bean) {
		Map<String, AddressRecord> map = null;
		createLock.lock();
		try {
			map = mapStorage.get(bean.getAddressKey());
			if (map == null) {
				map = new ConcurrentHashMap<String, AddressRecord>();
				mapStorage.put(bean.getAddressKey(), map);
			}
		} finally {
			createLock.unlock();
		}
		synchronized (map) {
			AddressRecord rec = map.get(bean.getAddressValue());
			if (rec == null) {
				rec = new AddressRecord(bean);
				map.put(bean.getAddressValue(), rec);
			} else {
				rec.setStatus(Status.OK);
			}
			return rec;
		}
	}

	/**
	 * Delete old garbage data from memory.
	 */
	public void deleteGarbage() {
		for (Map.Entry<String, Map<String, AddressRecord>> mapEntry : mapStorage.entrySet()) {
			createLock.lock();
			try {
				if (mapEntry.getValue().size() == 0) {
					mapStorage.remove(mapEntry.getKey());
					continue;
				}
			} finally {
				createLock.unlock();
			}
			Map<String, AddressRecord> map = mapEntry.getValue();
			synchronized (map) {
				for (AddressRecord rec : map.values()) {
					switch (rec.getStatus()) {
						case DEL:
							if (rec.getLastTime() + deleteTime < System.currentTimeMillis()) {
								map.remove(rec.getAddressValue());
							}
							break;
						case DISCONNECTED:
							if (rec.getLastTime() + deleteTime < System.currentTimeMillis()) {
								rec.setStatus(Status.DEL);
								dispatcher.fireEvent(rec);
							}
							break;
					}//End of switch
				}//End of for
			}//End of Sync
		}
	}

	/**
	 * Get all the addresses by this addressKey.
	 * @param addressKey
	 * @return null If key not found.
	 */
	public List<String> getAddress(String addressKey) {
		Map<String, AddressRecord> map = null;
		map = mapStorage.get(addressKey);
		List<String> list = new ArrayList<String>();
		if (map != null) {
			for (AddressRecord rec : map.values()) {
				if (rec.getStatus() != Status.DEL) {
					list.add(rec.getAddressValue());
				}
			}
		}
		return list;
	}

	public int getDeleteTime() {
		return deleteTime;
	}

	public void setDeleteTime(int deleteTime) {
		this.deleteTime = deleteTime;
	}

	public IDispatcher getDispatcher() {
		return dispatcher;
	}

	public void setDispatcher(IDispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}

}
