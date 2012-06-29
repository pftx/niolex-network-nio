/**
 * DemoAddressEventListener.java
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

import org.apache.niolex.network.name.client.AddressEventListener;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-29
 */
public class DemoAddressEventListener implements AddressEventListener {

	@Override
	public void addressAdd(String addressValue) {
		System.out.println("Address add: " + addressValue);
	}

	@Override
	public void addressRemove(String addressValue) {
		System.out.println("Address remove: " + addressValue);
	}

	@Override
	public void addressRefresh(List<String> addressList) {
		System.out.println("Address refresh: " + addressList);
	}

}
