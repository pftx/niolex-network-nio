/**
 * NameServerDemo.java
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
package org.apache.niolex.network.name.core;

import static org.mockito.Mockito.mock;

import java.io.IOException;

import org.apache.niolex.network.name.bean.RecordStorage;
import org.apache.niolex.network.name.event.IDispatcher;
import org.apache.niolex.network.server.NioServer;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-27
 */
public class NameServerDemo {

	private static NioServer s = new NioServer();
	private static NameServer name;

	static {
        s.setPort(8181);
        name = new NameServer(s);
        name.setStorage(new RecordStorage());
        IDispatcher dd = mock(IDispatcher.class);
        name.setDispatcher(dd);
	}

    /**
     * The Server Demo
     * @param args
     */
    public static void main(String[] args) throws IOException {
    	name.start();
    }

    public static void stop() {
    	name.stop();
    }

}
