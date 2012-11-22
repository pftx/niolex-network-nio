/**
 * SubscribeBean.java
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
package org.apache.niolex.config.bean;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This bean is used to subscribe client to config server.
 *
 * It contains authentication information and the config group user want to have.
 * Every config group will be authorized separately. So there will be the case that
 * user have the authority to read some groups but do not have for the others.
 * We will return the status of each config group one by one.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-7-3
 */
public class SubscribeBean {

	/**
	 * The user name;
	 */
	private String userName;

	/**
	 * The password.
	 */
	private String password;

	/**
	 * The group list this node interested.
	 */
	private Set<String> groupSet = Collections.synchronizedSet(new HashSet<String>());

	//---------------------- GETTER & SETTER ---------------------------------

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Set<String> getGroupSet() {
		return groupSet;
	}

	public void setGroupSet(Set<String> groupList) {
		this.groupSet = groupList;
	}

	@Override
	public String toString() {
		return "{" + userName + ", " + password + "}";
	}

}
