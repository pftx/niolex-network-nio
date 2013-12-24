/**
 * ListNodeCommand.java
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
 * List all the nodes of a version.
 *
 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2013-12-24
 */
public class ListNodeCommand extends BaseCommand {

    /**
     * This is the override of super method.
     * @see org.apache.niolex.address.cmd.ICommand#processCmd(org.apache.niolex.address.optool.OPToolService, java.util.List)
     */
    @Override
    public void processCmd(OPToolService optool, List<String> cmdOps) throws Exception {
        if (cmdOps.size() == 2) {
            int version = Integer.parseInt(cmdOps.get(1));
            PathUtil.Path p = PathUtil.decodePath(optool.getRoot(), EVN.curPath);
            if (p.getService() == null) {
                error("listNode can only work inside a service path.");
                return;
            }
            List<String> list = optool.listNodeByVersion(p.getService(), version);
            out("total " + list.size());
            for (String s : list) {
                out("\t" + s);
            }
        } else {
            error("Usage: listNode <version>");
        }
    }

}
