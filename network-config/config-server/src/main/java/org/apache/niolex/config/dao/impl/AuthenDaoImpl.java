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

import java.util.List;

import javax.annotation.Resource;

import org.apache.niolex.config.bean.UserInfo;
import org.apache.niolex.config.dao.AuthenDao;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-7-6
 */
@Repository
public class AuthenDaoImpl implements AuthenDao {


	@Resource
	private JdbcTemplate template;

	/**
	 * Override super method
	 * @see org.apache.niolex.config.dao.AuthenDao#authUser(java.lang.String, java.lang.String)
	 */
	@Override
	public UserInfo authUser(String username, String disgest) {
		final String sql = "select urid, urole from user_info where urname = ? and digest = ?";
		List<UserInfo> list = template.query(sql, new Object[] {username, disgest}, UserInfoRowMapper.INSTANCE);
		if (list != null && list.size() > 0) {
			UserInfo info = list.get(0);
			info.setUserName(username);
			info.setPassword(disgest);
			return info;
		}
		return null;
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.config.dao.AuthenDao#addUser(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public boolean addUser(String username, String digest, String role) {
		final String sql = "insert into user_info (urname, digest, urole) values (?, ?, ?)";
		try {
			template.update(sql, username, digest, role);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.config.dao.AuthenDao#updateUser(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public boolean updateUser(String username, String digest, String role) {
		if (role == null) {
			final String sql = "update user_info set digest = ? where urname = ?";
			return template.update(sql, digest, username) > 0;
		} else {
			final String sql = "update user_info set digest = ?, urole = ? where urname = ?";
			return template.update(sql, digest, role, username) > 0;
		}
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.config.dao.AuthenDao#getUser(java.lang.String)
	 */
	@Override
	public UserInfo getUser(String userName) {
		final String sql = "select urid, urole from user_info where urname = ?";
		List<UserInfo> list = template.query(sql,  new Object[] {userName}, UserInfoRowMapper.INSTANCE);
		if (list == null || list.size() == 0)
			return null;
		return list.get(0);
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.config.dao.AuthenDao#hasReadAuth(long, long)
	 */
	@Override
	public boolean hasReadAuth(long userid, long groupId) {
		final String sql = "select 1 from auth_info where aurid = ? and groupid = ?";
		List<Integer> list = template.queryForList(sql, Integer.class, userid, groupId);
		return list.size() > 0;
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.config.dao.AuthenDao#addReadAuth(long, long)
	 */
	@Override
	public boolean addReadAuth(long userid, long groupId) {
		final String sql = "replace into auth_info(aurid, groupid) values (?, ?)";
		template.update(sql, userid, groupId);
		return true;
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.config.dao.AuthenDao#delReadAuth(long, long)
	 */
	@Override
	public boolean delReadAuth(long userid, long groupId) {
		final String sql = "delete from auth_info where aurid = ? and groupid = ?";
		return template.update(sql, userid, groupId) > 0;
	}

}
