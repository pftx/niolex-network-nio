/**
 * AuthenServiceImpl.java
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
package org.apache.niolex.config.service.impl;

import org.apache.niolex.commons.codec.SHAUtil;
import org.apache.niolex.config.bean.GroupConfig;
import org.apache.niolex.config.bean.SubscribeBean;
import org.apache.niolex.config.config.AttachKey;
import org.apache.niolex.config.dao.AuthenDao;
import org.apache.niolex.config.service.AuthenService;
import org.apache.niolex.network.IPacketWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-7-6
 */
@Service
public class AuthenServiceImpl implements AuthenService {

	private static final Logger LOG = LoggerFactory.getLogger(AuthenServiceImpl.class);
	private static final String PASSWORD_DIG = "4d6e6d7f52798";

	@Autowired
	private AuthenDao authenDao;

	/**
	 * Override super method
	 * @see org.apache.niolex.config.service.AuthenService#authUser(org.apache.niolex.config.bean.SubscribeBean, org.apache.niolex.network.IPacketWriter)
	 */
	@Override
	public boolean authUser(SubscribeBean bean, IPacketWriter wt) {
		String digest;
		try {
			digest = SHAUtil.sha1(PASSWORD_DIG, bean.getPassword());
		} catch (Exception e) {
			LOG.error("Failed to generate password digest.", e);
			return false;
		}
		long userId = authenDao.authUser(bean.getUserName(), digest);
		if (userId < 0) {
			return false;
		}
		wt.attachData(AttachKey.USER_ID, userId);
		return true;
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.config.service.AuthenService#hasReadAuth(org.apache.niolex.config.bean.GroupConfig, org.apache.niolex.network.IPacketWriter)
	 */
	@Override
	public boolean hasReadAuth(GroupConfig group, IPacketWriter wt) {
		Long userId = wt.getAttached(AttachKey.USER_ID);
		if (userId == null) {
			return false;
		}

		return authenDao.hasReadAuth(userId, group.getGroupId());
	}

}
