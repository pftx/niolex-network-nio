/**
 * CodeMap.java
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
package org.apache.niolex.config.core;

/**
 * The interface to store all the packet codes.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-7-3
 */
public interface CodeMap {

	// Add.
	short GROUP_ADD = 1000;
	// Subscribe.
	short GROUP_SUB = 1001;
	// Diff.
	short GROUP_DIF = 1002;
	// Sync.
	short GROUP_SYN = 1003;
	// Data.
	short GROUP_DAT = 1004;
	// Not found.
	short GROUP_NOF = 1005;
	// No auth.
	short GROUP_NOA = 1006;

	// Auth and init subscribe.
	short AUTH_SUBS = 2000;
	// Auth failed.
	short AUTH_FAIL = 2001;

	// Add a new config group.
	short ADMIN_ADD_GROUP = 3001;
	// Refresh the data of an existing group.
	short ADMIN_REFRESH_GROUP = 3004;
	// Add a new config item.
	short ADMIN_ADD_CONFIG = 3002;
	// Update an old config item.
	short ADMIN_UPDATE_CONFIG = 3003;
	// Get an existing config item.
	short ADMIN_GET_CONFIG = 3005;
	// Add a new user.
	short ADMIN_ADD_USER = 3006;
	// Update an existing user.
	short ADMIN_UPDATE_USER = 3007;
	// Add read authorize to user.
	short ADMIN_ADD_AUTH = 3008;
	// Remove read authorize from user.
	short ADMIN_REMOVE_AUTH = 3009;

	// The response to add config group request.
	short RES_ADD_GROUP = 4001;
	// The response to add config item request.
	short RES_ADD_ITEM = 4002;
	// The response to refresh config item request.
	short RES_UPDATE_ITEM = 4003;
	// Refresh the group data from DB.
	short RES_REFRESH_GROUP = 4004;
	// The response to add user request.
	short RES_ADD_USER = 4005;
	// The response to update user request.
	short RES_UPDATE_USER = 4006;
	// The response to add read authorization.
	short RES_ADD_AUTH = 4007;
	// The response to remove read authorization.
	short RES_REMOVE_AUTH = 4008;
}
