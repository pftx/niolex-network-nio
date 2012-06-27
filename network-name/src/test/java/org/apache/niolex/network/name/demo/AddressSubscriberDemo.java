/**
 * AddressSubscriberDemo.java
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
package org.apache.niolex.network.name.demo;

import java.util.List;

import org.apache.niolex.commons.event.EventListener;
import org.apache.niolex.network.name.bean.AddressRecord;
import org.apache.niolex.network.name.client.AddressSubscriber;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-27
 */
public class AddressSubscriberDemo {

    /**
     * The Client Demo
     *
     * @param args
     */
    public static void main(String[] args) throws Exception {
    	AddressSubscriber c = new AddressSubscriber("localhost:8181");
        EventListener<AddressRecord> listn = new EventListener<AddressRecord>() {

			@Override
			public void eventHappened(AddressRecord e) {
				System.out.println("Address changed: " + e.getStatus()
						+ ", " + e.getAddressKey() + ", " + e.getAddressValue());

			}};
		List<String> ls = c.getServiceAddrList("network/name", listn);
		System.out.println("Address list: " + ls);
        Thread.sleep(100000);
    }

}