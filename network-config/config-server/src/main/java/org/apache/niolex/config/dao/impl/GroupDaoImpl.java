/**
 * GroupDaoImpl.java
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

import java.sql.Timestamp;
import java.util.List;

import javax.annotation.Resource;

import org.apache.niolex.config.bean.GroupConfig;
import org.apache.niolex.config.dao.GroupDao;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-7-6
 */
@Repository
public class GroupDaoImpl implements GroupDao {

	@Resource
	private JdbcTemplate template;


	/**
	 * Override super method
	 * @see org.apache.niolex.config.dao.GroupDao#addGroup(java.lang.String)
	 */
	@Override
	public boolean addGroup(String groupName) {
		final String sql = "insert into group_info (groupname) values (?)";
		try {
			template.update(sql, groupName);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.config.dao.GroupDao#loadAllGroups()
	 */
	@Override
	public List<GroupConfig> loadAllGroups() {
		final String sql = "select groupid, groupname from group_info order by groupid";
		return template.query(sql, GroupConfigRowMapper.INSTANCE);
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.config.dao.GroupDao#loadDBTime()
	 */
	@Override
	public long loadDBTime() {
		final String sql = "select current_timestamp()";
		return template.queryForObject(sql, Timestamp.class).getTime();
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.config.dao.GroupDao#loadGroup(java.lang.String)
	 */
	@Override
	public GroupConfig loadGroup(String groupName) {
		final String sql = "select groupid, groupname from group_info where groupname = ?";
		List<GroupConfig> list = template.query(sql,
				new Object[] {groupName}, GroupConfigRowMapper.INSTANCE);
		return list.size() == 0 ? null : list.get(0);
	}

}
