/**
 * CenterConnector.java
 * 
 * Copyright 2012 The original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.niolex.config.service;

import java.io.IOException;

import org.apache.niolex.config.admin.UpdaterClient;
import org.apache.niolex.config.bean.ConfigGroup;
import org.apache.niolex.network.IPacketWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5
 * @since 2012-12-12
 */
public class CenterConnector extends UpdaterClient {
    private static final Logger LOG = LoggerFactory.getLogger(CenterConnector.class);
    private String username;
    
    /**
     * @param serverAddress the server address to config server
     * @throws IOException if I/O related error occurred
     */
    public CenterConnector(String serverAddress) throws IOException {
        super(serverAddress);
    }

    /**
     * Override super method
     * @see org.apache.niolex.config.admin.UpdaterClient#subscribeAuthInfo(java.lang.String, java.lang.String)
     */
    @Override
    public String subscribeAuthInfo(String username, String password) throws Exception {
        this.username = username;
        return super.subscribeAuthInfo(username, password);
    }
    
    public boolean isWorking() {
        return super.client.isWorking();
    }
    
    /**
     * Get the config group.
     * 
     * @param groupName the config group name
     * @return the group
     */
    public ConfigGroup getConfig(String groupName) {
        return super.storage.get(groupName);
    }

    public String findGroupName(int groupId) {
        return super.storage.findGroupName(groupId);
    }
    
    @Override
    public void handleClose(IPacketWriter wt) {
        client.stop();
        LOG.info("Connection for [{}] is closed.", username);
    }
    
    @Override
    public void stop() {
        client.stop();
        LOG.info("Connection for [{}] is closed.", username);
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }
    
}
