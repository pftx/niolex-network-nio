/**
 * AuthenService.java
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
package org.apache.niolex.config.service;

import org.apache.niolex.config.bean.ConfigGroup;
import org.apache.niolex.config.bean.SubscribeBean;
import org.apache.niolex.config.bean.UserInfo;
import org.apache.niolex.network.IPacketWriter;

/**
 * Authentication Service.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-7-5
 */
public interface AuthenService {

	/**
	 * Use the information in SubscribeBean to do authentication.
	 * If success, store user info in IPacketWriter.
	 *
	 * @param bean
	 * @param wt
	 * @return true if success, false otherwise.
	 */
	public boolean authUser(SubscribeBean bean, IPacketWriter wt);

	/**
	 * Get the attached User Id of this client.
	 * @param wt
	 * @return -1 if not found.
	 */
	public int getUserId(IPacketWriter wt);

	/**
	 * Add a new user into DB.
	 * Only Admin can add user with user role, Op can only add common user.
	 * @param info
	 * @param wt
	 * @return the detailed message about the status of add user
	 */
	public String addUser(UserInfo info, IPacketWriter wt);

	/**
	 * Update a user role or password. Only Admin can update user role.
	 * @param info
	 * @param wt
	 * @return the detailed message about the status of update user
	 */
	public String updateUser(UserInfo info, IPacketWriter wt);

	/**
	 * Check whether this client has the right to read this group config.
	 *
	 * @param group
	 * @param wt
	 * @return true if have read auth, false otherwise
	 */
	public boolean hasReadAuth(ConfigGroup group, IPacketWriter wt);

	/**
	 * Check whether this client has the right to add and change config.
	 *
	 * @param wt
	 * @return true if have config auth, false otherwise
	 */
	public boolean hasConfigAuth(IPacketWriter wt);


	/**
	 * Add read authorization of this group to this user.
	 * @param userName
	 * @param groupName
	 * @param wt
	 * @return the detailed message about the status of add read auth
	 */
	public String addReadAuth(String userName, String groupName, IPacketWriter wt);

	/**
	 * Remove read authorization of this group to this user.
	 * @param userName
	 * @param groupName
	 * @param wt
	 * @return the detailed message about the status of remove read auth
	 */
	public String removeReadAuth(String userName, String groupName, IPacketWriter wt);
}
