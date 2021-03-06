/**
 * AddServerAuthCommand.java
 *
 * Copyright 2013 the original author or authors.
 *
 * We licenses this file to you under the Apache License, version 2.0
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
package org.apache.niolex.address.cmd.impl;

import java.util.List;

import org.apache.niolex.address.optool.OPToolService;
import org.apache.niolex.address.util.PathUtil;

/**
 * Add server authorization.
 *
 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2013-12-23
 */
public class AddServerAuthCommand extends BaseCommand {

    /**
     * This is the override of super method.
     * @see org.apache.niolex.address.cmd.ICommand#processCmd(org.apache.niolex.address.optool.OPToolService, java.util.List)
     */
    @Override
    public void processCmd(OPToolService optool, List<String> cmdOps) throws Exception {
        if (cmdOps.size() == 3) {
            String path = EVN.getAbsolutePath(cmdOps.get(1));
            String serverName = cmdOps.get(2);
            PathUtil.Path p = PathUtil.decodePath(optool.getRoot(), path);
            switch (p.getLevel()) {
                case STATE:
                case NODE:
                    optool.addServerAuth(serverName, p.getService(), p.getVersion(), p.getState());
                    break;
                case CVERSION:
                case SVERSION:
                    optool.addServerMetaAuth(serverName, p.getService(), p.getVersion());
                    optool.addServerAuth(serverName, p.getService(), p.getVersion());
                    break;
                default:
                    error("addServerAuth can only work inside a version or state path.");
                    return;
            }
            out("OK");
        } else {
            error("Usage: addServerAuth <path> <userName>");
        }
    }

}
