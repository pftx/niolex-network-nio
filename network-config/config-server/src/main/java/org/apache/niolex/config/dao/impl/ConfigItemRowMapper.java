/**
 * GroupConfigRowMapper.java
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

import org.apache.niolex.config.bean.ConfigItem;
import org.springframework.jdbc.core.RowMapper;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-7-6
 */
public class ConfigItemRowMapper implements RowMapper<ConfigItem> {

	public static final ConfigItemRowMapper INSTANCE = new ConfigItemRowMapper();

	/**
	 * Override super method
	 * @see org.springframework.jdbc.core.RowMapper#mapRow(java.sql.ResultSet, int)
	 */
	@Override
	public ConfigItem mapRow(ResultSet rs, int rowNum) throws SQLException {
		// groupid, ckey, value, curid, uurid, updatetime
		ConfigItem config = new ConfigItem();
		config.setGroupId(rs.getInt("groupid"));
		config.setcUid(rs.getInt("curid"));
		config.setuUid(rs.getInt("uurid"));
		config.setKey(rs.getString("ckey"));
		config.setValue(rs.getString("value"));
		config.setUpdateTime(rs.getTimestamp("updatetime").getTime());
		return config;
	}

}
