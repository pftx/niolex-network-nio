/**
 * Updater.java
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
package org.apache.niolex.config.admin;

/**
 * The inteface for updater.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-7-9
 */
public interface Updater {

	public void subscribeAuthInfo(String username, String password);

	public String addGroup(String groupName) throws Exception;

	public String refreshGroup(String groupName) throws Exception;

	public String addItem(String groupName, String key, String value) throws Exception;

	public String updateItem(String groupName, String key, String value) throws Exception;

	public String getItem(String groupName, String key) throws Exception;

	public String addUser(String username, String password, String userRole) throws Exception;

	public String changePassword(String username, String password) throws Exception;

	public String updateUser(String username, String password, String userRole) throws Exception;

	public String addAuth(String username, String groupName) throws Exception;

	public String removeAuth(String username, String groupName) throws Exception;

}
