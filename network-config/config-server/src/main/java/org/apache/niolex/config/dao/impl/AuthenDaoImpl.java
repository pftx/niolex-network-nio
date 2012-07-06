/**
 * AuthenDaoImpl.java
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
package org.apache.niolex.config.dao.impl;

import org.apache.niolex.config.dao.AuthenDao;
import org.springframework.stereotype.Repository;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-7-6
 */
@Repository
public class AuthenDaoImpl implements AuthenDao {

	/**
	 * Override super method
	 * @see org.apache.niolex.config.dao.AuthenDao#authUser(java.lang.String, java.lang.String)
	 */
	@Override
	public long authUser(String username, String disgest) {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.config.dao.AuthenDao#addUser(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public boolean addUser(String username, String disgest, String role) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.config.dao.AuthenDao#updateUser(long, java.lang.String, java.lang.String)
	 */
	@Override
	public boolean updateUser(long userid, String disgest, String role) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.config.dao.AuthenDao#hasReadAuth(long, long)
	 */
	@Override
	public boolean hasReadAuth(long userid, long groupId) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.config.dao.AuthenDao#addReadAuth(long, long)
	 */
	@Override
	public boolean addReadAuth(long userid, long groupId) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.config.dao.AuthenDao#delReadAuth(long, long)
	 */
	@Override
	public boolean delReadAuth(long userid, long groupId) {
		// TODO Auto-generated method stub
		return false;
	}

}
