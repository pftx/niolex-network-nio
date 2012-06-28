/**
 * AddressEventListener.java
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
package org.apache.niolex.network.name.client;

import java.util.List;

/**
 * Listen to the address change from name server.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-28
 */
public interface AddressEventListener {

	/**
	 * A new working server added into name server.
	 * @param addressValue
	 */
	public void addressAdd(String addressValue);

	/**
	 * A server disconnected from name server.
	 * @param addressValue
	 */
	public void addressRemove(String addressValue);

	/**
	 * This means a total refresh of current address list.
	 * @param addressList
	 */
	public void addressRefresh(List<String> addressList);

}
