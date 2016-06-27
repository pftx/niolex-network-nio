/**
 * ConfigItem.java
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
package org.apache.niolex.network.rpc;

/**
 * The Rpc configure class. Configure with an interface and a implementation target.
 * This is for server side usage.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0, Date: 2012-6-1
 */
public class ConfigItem {

	/**
	 * The interface.
	 */
	private Class<?> interfaze;

	/**
	 * The implementation target.
	 */
	private Object target;

	/**
	 * Create an empty object.
	 */
	public ConfigItem() {
		super();
	}

	/**
	 * Create an item with specified parameters.
	 *
	 * @param sinterface the service interface
	 * @param target the service implementation
	 */
	public ConfigItem(Class<?> sinterface, Object target) {
		super();
		this.interfaze = sinterface;
		this.target = target;
	}

	public Class<?> getInterface() {
		return interfaze;
	}

	public void setInterface(Class<?> interfs) {
		this.interfaze = interfs;
	}

	public Object getTarget() {
		return target;
	}

	public void setTarget(Object target) {
		this.target = target;
	}

}
