/**
 * SetMetaCommand.java
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
 * Set the meta data of this client.
 *
 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2013-12-23
 */
public class SetMetaCommand extends BaseCommand {

    /**
     * This is the override of super method.
     * @see org.apache.niolex.address.cmd.ICommand#processCmd(org.apache.niolex.address.optool.OPToolService, java.util.List)
     */
    @Override
    public void processCmd(OPToolService optool, List<String> cmdOps) throws Exception {
        if (cmdOps.size() != 4) {
            error("Usage: setMeta <userName> <Key> <Value>");
            return;
        }
        String clientName = cmdOps.get(1);
        PathUtil.Path p = PathUtil.decodePath(optool.getRoot(), EVN.curPath);
        switch (p.getLevel()) {
            case CVERSION:
            case SVERSION:
            case CLIENT:
            case STATE:
            case NODE:
                break;
            default:
                error("setMeta only work inside a version path.");
                return;
        }
        optool.updateMetaData(clientName, p.getService(), p.getVersion(), cmdOps.get(2), cmdOps.get(3));
        out("OK");
    }

}
