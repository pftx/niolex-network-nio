/**
 * ConfigGroupRowMapper.java
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

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.niolex.config.bean.ConfigGroup;
import org.springframework.jdbc.core.RowMapper;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-7-6
 */
public class ConfigGroupRowMapper implements RowMapper<ConfigGroup> {

	public static final ConfigGroupRowMapper INSTANCE = new ConfigGroupRowMapper();

	/**
	 * Override super method
	 * @see org.springframework.jdbc.core.RowMapper#mapRow(java.sql.ResultSet, int)
	 */
	@Override
	public ConfigGroup mapRow(ResultSet rs, int rowNum) throws SQLException {
		ConfigGroup config = new ConfigGroup();
		config.setGroupId(rs.getInt("groupid"));
		config.setGroupName(rs.getString("groupname"));
		return config;
	}

}
