/**
 * Main.java
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
package org.apache.niolex.config.main;

import org.apache.niolex.commons.remote.BeanServer;
import org.apache.niolex.commons.remote.ConnectionWorker;
import org.apache.niolex.commons.remote.OSInfo;
import org.apache.niolex.config.core.MemoryStorage;
import org.apache.niolex.config.event.ConfigEventDispatcher;
import org.apache.niolex.config.server.ConfigServer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-7-6
 */
public class Main {

	public static void main(String[] args) throws Exception {
        ApplicationContext ctx = new FileSystemXmlApplicationContext(
                "conf/spring.xml");
        ConfigServer cServer = ctx.getBean(ConfigServer.class);
        if (!cServer.start()) {
        	System.out.println("Failed to start Config Server, system will exit...");
        	System.exit(-1);
        }
        ConnectionWorker.setAuthInfo("d178b4c149");
        BeanServer beanS = new BeanServer();
        beanS.putIfAbsent("os", new OSInfo());
        beanS.putIfAbsent("conf", ctx.getBean(MemoryStorage.class));
        beanS.putIfAbsent("dispatch", ctx.getBean(ConfigEventDispatcher.class));
        beanS.putIfAbsent("server", cServer);
        if (!beanS.start()) {
        	System.out.println("Failed to start Bean Server, system will exit...");
        	System.exit(-1);
        }
	}

}
