/**
 * ICommand.java
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
package org.apache.niolex.address.cmd;

import java.util.List;

import org.apache.niolex.address.optool.Environment;
import org.apache.niolex.address.optool.OPToolService;


/**
 * The command interface, all the commands need implement this.
 *
 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2013-12-16
 */
public interface ICommand {

    Environment EVN = Environment.getInstance();

    /**
     * Process the command.
     *
     * @param optool the operation tool
     * @param cmdOps the command options
     * @throws Exception if necessary
     */
    void processCmd(OPToolService optool, List<String> cmdOps) throws Exception;

}
