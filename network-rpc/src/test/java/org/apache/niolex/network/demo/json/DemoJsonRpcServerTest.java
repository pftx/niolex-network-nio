/**
 * RpcServerTest.java
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
package org.apache.niolex.network.demo.json;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.apache.niolex.commons.reflect.MethodUtil;
import org.apache.niolex.network.demo.json.DemoJsonRpcClient;
import org.apache.niolex.network.demo.json.DemoJsonRpcServer;
import org.apache.niolex.network.demo.json.RpcServiceImpl;
import org.apache.niolex.network.demo.json.SocketJsonRpcClient;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-4
 */
public class DemoJsonRpcServerTest {

	/**
	 * Test method for {@link org.apache.niolex.network.demo.json.DemoJsonRpcServer#main(java.lang.String[])}.
	 * @throws Throwable
	 */
	@Test
	@SuppressWarnings("unused")
	public void testMain() throws Throwable {
		DemoJsonRpcClient c = new DemoJsonRpcClient();
		SocketJsonRpcClient g = new SocketJsonRpcClient();
		DemoJsonRpcServer s = new DemoJsonRpcServer();
		DemoJsonRpcServer.main(new String[] {"2", "4"});
		DemoJsonRpcClient.main(null);
		SocketJsonRpcClient.main(null);
		DemoJsonRpcServer.stop();
	}

	@Test
	public void test() throws Throwable {
		Method method = MethodUtil.getMethods(RpcServiceImpl.class, "testMe")[0];
		Type t = method.getGenericReturnType();
		System.out.println("test " + t.toString());
		Type[] ts = method.getGenericParameterTypes();
		System.out.println("test " + ts.length);

		method = MethodUtil.getMethods(RpcServiceImpl.class, "add")[0];
		t = method.getGenericReturnType();
		System.out.println("test " + t.toString());
		ts = method.getGenericParameterTypes();
		System.out.println("test " + ts.length);
		new RpcServiceImpl().testMe();
		SocketJsonRpcClient.assertt(1, 0, "OSDODKK");
	}


    /**
     * The Server Demo
     * @param args
     */
    public static void main(String[] args) throws IOException {
    	DemoJsonRpcServer.main(args);
    }

}
