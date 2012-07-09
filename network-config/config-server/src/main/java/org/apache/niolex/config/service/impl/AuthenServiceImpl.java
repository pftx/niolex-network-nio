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
import org.apache.niolex.config.bean.ConfigGroup;
import org.apache.niolex.config.bean.SubscribeBean;
import org.apache.niolex.config.bean.UserInfo;
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
		UserInfo info = authenDao.authUser(bean.getUserName(), digest);
		if (info == null) {
			return false;
		}
		wt.attachData(AttachKey.USER_INFO, info);
		return true;
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.config.service.AuthenService#hasReadAuth(org.apache.niolex.config.bean.ConfigGroup, org.apache.niolex.network.IPacketWriter)
	 */
	@Override
	public boolean hasReadAuth(ConfigGroup group, IPacketWriter wt) {
		UserInfo info = wt.getAttached(AttachKey.USER_INFO);
		if (info == null) {
			return false;
		}
		if (info.getUserRole().equalsIgnoreCase("OP")
				|| info.getUserRole().equalsIgnoreCase("ADMIN")) {
			return true;
		}
		return authenDao.hasReadAuth(info.getUserId(), group.getGroupId());
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.config.service.AuthenService#hasConfigAuth(org.apache.niolex.network.IPacketWriter)
	 */
	@Override
	public boolean hasConfigAuth(IPacketWriter wt) {
		UserInfo info = wt.getAttached(AttachKey.USER_INFO);
		if (info == null) {
			return false;
		}
		return info.getUserRole().equalsIgnoreCase("OP")
				|| info.getUserRole().equalsIgnoreCase("ADMIN");
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.config.service.AuthenService#getUserId(org.apache.niolex.network.IPacketWriter)
	 */
	@Override
	public int getUserId(IPacketWriter wt) {
		UserInfo info = wt.getAttached(AttachKey.USER_INFO);
		if (info == null) {
			return -1;
		}
		return info.getUserId();
	}

}
