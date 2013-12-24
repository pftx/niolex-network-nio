/**
 * CreateCommand.java
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
import org.apache.niolex.commons.codec.StringUtil;

/**
 * Create node.
 *
 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2013-12-23
 */
public class CreateCommand extends BaseCommand {

    /**
     * This is the override of super method.
     * @see org.apache.niolex.address.cmd.ICommand#processCmd(org.apache.niolex.address.optool.OPToolService, java.util.List)
     */
    @Override
    public void processCmd(OPToolService optool, List<String> cmdOps) throws Exception {
        if (cmdOps.size() < 2) {
            error("Usage: create <path> <data>");
        }
        String path = EVN.getAbsolutePath(cmdOps.get(1));
        PathUtil.Path p = PathUtil.decodePath(optool.getRoot(), path);
        switch (p.getLevel()) {
            case SERVICE:
                optool.addService(p.getService());
                break;
            case SVERSION:
                optool.addVersion(p.getService(), p.getVersion());
                break;
            case CVERSION:
                optool.addMetaVersion(p.getService(), p.getVersion());
                break;
            case STATE:
                optool.addState(p.getService(), p.getVersion(), p.getState());
                break;
            case NODE:
                byte[] data = null;
                if (cmdOps.size() >= 3) {
                    data = StringUtil.strToUtf8Byte(cmdOps.get(2));
                }
                optool.createNode(path, data);
                break;
            default:
                if (p.getVersion() == -1) {
                    error("Version node must be integer.");
                } else {
                    error("create can only work inside a state path.");
                }
                return;
        }
        out("OK");
    }

}
