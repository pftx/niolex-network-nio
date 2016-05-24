/**
 * AuthenDao.java
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
package org.apache.niolex.config.dao;

import org.apache.niolex.config.bean.UserInfo;

/**
 * Communicate with DB, deal with auth info.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-7-5
 */
public interface AuthenDao {

	/**
	 * Authenticate user.
	 *
	 * @param username the user name
	 * @param digest the user password digest
	 * @return userid if success, -1 otherwise.
	 */
	public UserInfo authUser(String username, String digest);

	/**
	 * Add this user into DB.
	 *
	 * @param username the user name
	 * @param digest the user password digest
	 * @param role the user role
	 * @return true if success, false otherwise.
	 */
	public boolean addUser(String username, String digest, String role);

	/**
	 * Update this user's information.
	 * 
	 * @param username the user name
	 * @param digest the user password digest
	 * @param role the user role
	 * @return true if success, false otherwise.
	 */
	public boolean updateUser(String username, String digest, String role);

	/**
	 * Get user info by this user name.
	 *
	 * @param userName the user name
	 * @return the user information, null if not found.
	 */
	public UserInfo getUser(String userName);

	/**
	 * Check whether this user has the right to read the specified group config.
	 * 
	 * @param userid the user ID
	 * @param groupId the group ID
	 * @return true if success, false otherwise.
	 */
	public boolean hasReadAuth(long userid, long groupId);

	/**
	 * Add the read authorization to this user.
	 * 
	 * @param userid the user ID
	 * @param groupId the group ID
	 * @return true if success, false otherwise.
	 */
	public boolean addReadAuth(long userid, long groupId);

	/**
	 * Delete read authorization from this user.
	 * 
	 * @param userid the user ID
	 * @param groupId the group ID
	 * @return true if success, false otherwise.
	 */
	public boolean delReadAuth(long userid, long groupId);

}
