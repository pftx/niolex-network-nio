/**
 * BaseCommand.java
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

import org.apache.niolex.address.cmd.ICommand;
import org.apache.niolex.commons.util.SystemUtil;

/**
 * The base command, some common methods here.
 *
 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2013-12-17
 */
public abstract class BaseCommand implements ICommand {

    protected void error(String str) {
        System.err.println(str);
        System.err.flush();
        SystemUtil.sleep(5);
    }

    protected void out(Object str) {
        System.out.println(str);
    }

    protected String getPath(List<String> cmdOps) {
        if (cmdOps.size() == 1)
            return EVN.curPath;
        else if (cmdOps.size() == 2)
            return EVN.getAbsolutePath(cmdOps.get(1));
        else
            return null;
    }

    protected String getPrintPath(List<String> cmdOps) {
        if (cmdOps.size() <= 1)
            return ".";
        else
            return cmdOps.get(1);
    }

}
