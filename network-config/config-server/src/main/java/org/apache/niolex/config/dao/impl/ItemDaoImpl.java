/**
 * ItemDaoImpl.java
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

import org.apache.niolex.config.bean.ConfigItem;
import org.apache.niolex.config.dao.ItemDao;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-7-6
 */
@Repository
public class ItemDaoImpl implements ItemDao {

	@Resource
	private JdbcTemplate template;

	/**
	 * Override super method
	 * @see org.apache.niolex.config.dao.ItemDao#loadAllConfigItems(long)
	 */
	@Override
	public List<ConfigItem> loadAllConfigItems(long startTime) {
		final String sql = "select groupid, ckey, value, curid, uurid, updatetime from config_info where updatetime >= ? order by groupid";
		return template.query(sql, new Object[] {new Timestamp(startTime)}, ConfigItemRowMapper.INSTANCE);
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.config.dao.ItemDao#loadGroupItems(int)
	 */
	@Override
	public List<ConfigItem> loadGroupItems(int groupId) {
		final String sql = "select groupid, ckey, value, curid, uurid, updatetime from config_info where groupid = ?";
		return template.query(sql, new Object[] {groupId}, ConfigItemRowMapper.INSTANCE);
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.config.dao.ItemDao#updateConfig(org.apache.niolex.config.bean.ConfigItem)
	 */
	@Override
	public boolean updateConfig(ConfigItem item) {
		final String sql = "update config_info set value = ?, uurid = ? where groupid = ? and ckey = ? and updatetime = ?";
		Object[] param = new Object[] {item.getValue(), item.getuUid(), item.getGroupId(), item.getKey(), new Timestamp(item.getUpdateTime())};
		return template.update(sql, param) > 0;
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.config.dao.ItemDao#addConfig(org.apache.niolex.config.bean.ConfigItem)
	 */
	@Override
	public boolean addConfig(ConfigItem item) {
		final String sql = "insert into config_info (groupid, ckey, value, curid, uurid) values (?, ?, ?, ?, ?)";
		try {
			template.update(sql, item.getGroupId(), item.getKey(), item.getValue(),
					item.getcUid(), item.getuUid());
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.config.dao.ItemDao#getConfig(int, java.lang.String)
	 */
	@Override
	public ConfigItem getConfig(int groupId, String key) {
		final String sql = "select groupid, ckey, value, curid, uurid, updatetime from config_info where groupid = ? and ckey = ?";
		List<ConfigItem> list = template.query(sql, new Object[] {groupId, key}, ConfigItemRowMapper.INSTANCE);
		return list.size() == 0 ? null : list.get(0);
	}
}
