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

/**
 * Communicate with DB, deal with auth info.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-7-5
 */
public interface AuthenDao {

	/**
	 * Authenticate user.
	 *
	 * @param username
	 * @param disgest
	 * @return userid if success, -1 otherwise.
	 */
	public long authUser(String username, String disgest);

	/**
	 * Add this user into DB.
	 *
	 * @param username
	 * @param disgest
	 * @param role
	 * @return
	 */
	public boolean addUser(String username, String disgest, String role);

	/**
	 * Update this user information.
	 * @param userid
	 * @param disgest
	 * @param role
	 * @return
	 */
	public boolean updateUser(long userid, String disgest, String role);

	/**
	 * Check whether this user has the right to read the specified group config.
	 * @param userid
	 * @param groupId
	 * @return
	 */
	public boolean hasReadAuth(long userid, long groupId);

	/**
	 * Add the read authorize to this user.
	 * @param userid
	 * @param groupId
	 * @return
	 */
	public boolean addReadAuth(long userid, long groupId);

	/**
	 * Delete read authorize from this user.
	 * @param userid
	 * @param groupId
	 * @return
	 */
	public boolean delReadAuth(long userid, long groupId);
}
