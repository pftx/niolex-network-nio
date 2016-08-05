/**
 * RpcServiceFactoryTest.java
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
package org.apache.niolex.network.cli.init;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.apache.niolex.commons.reflect.FieldUtil;
import org.apache.niolex.network.cli.RetryHandler;
import org.apache.niolex.network.cli.bui.JsonRpcBuilder;
import org.apache.niolex.network.cli.conf.BaseConfiger;
import org.apache.niolex.network.cli.conf.RpcConfigBean;
import org.apache.niolex.network.cli.conf.RpcConfiger;
import org.apache.niolex.network.cli.handler.IServiceHandler;
import org.apache.niolex.network.demo.json.DemoJsonRpcServer;
import org.apache.niolex.network.rpc.anno.RpcConfig;
import org.apache.niolex.network.rpc.anno.RpcMethod;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Lists;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-6-5
 */
public class RpcServiceFactoryTest {
    private static RpcServiceFactory rpcFactory;

	@BeforeClass
	public static void up() throws IOException {
		DemoJsonRpcServer.main(null);
		ServiceHandlerFactory.registerBuilder("network/json", new JsonRpcBuilder());
		rpcFactory = RpcServiceFactory.getInstance("/org/apache/niolex/network/cli/bui/rpc.properties");
	}

	@AfterClass
	public static void down() {
		DemoJsonRpcServer.stop();
	}

	/**
	 * Test method for
	 * {@link org.apache.niolex.network.cli.init.RpcServiceFactory#getInstance(java.lang.String, org.apache.niolex.network.cli.init.ServiceHandlerFactory)}
	 * .
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testGetInstance() throws IOException {
		rpcFactory.getService("nan", ServiceHandlerFactory.class);
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.apache.niolex.network.cli.init.RpcServiceFactory#getService(java.lang.String, java.lang.Class)}.
	 * @throws IOException
	 */
	@Test
	public void testGetServiceStringClassOfT() throws IOException {
		LocalService ser = rpcFactory.getService(LocalService.class);
		for (int i = 0; i < 10; ++i) {
			int r = ser.add(2, 3214, 123, 12, i);
			System.out.println(r);
			assertEquals(3351 + i, r);
		}
	}

	/**
	 * Test method for
	 * {@link org.apache.niolex.network.cli.init.RpcServiceFactory#getService(java.lang.String, java.lang.Class)}.
	 * @throws IOException
	 */
	@Test
	public void testGetServiceStringClassOfF() throws IOException {
		EfService ser = rpcFactory.getService(EfService.class);
		for (int i = 0; i < 10; ++i) {
			int r = ser.add(2, 3214, 123, 12, i);
			System.out.println(r);
			assertEquals(3351 + i, r);
		}
	}

	/**
	 * Test method for {@link org.apache.niolex.network.cli.init.RpcServiceFactory#getConfiger()}.
	 * @throws IOException
	 */
	@Test
	public void testGetConfiger() throws IOException {
		RpcConfiger con = rpcFactory.getConfiger();
		RpcConfigBean bean = con.getConfig();
		assertEquals("/cgi-bin/services/WdgetService.cgi", bean.serviceUrl);
	}

	@Test
    public void testFakeHandler() throws Throwable {
	    Map<String, RetryHandler<IServiceHandler>> handlers = FieldUtil.getValue(rpcFactory, "handlers");
	    @SuppressWarnings("unchecked")
        RetryHandler<IServiceHandler> h = mock(RetryHandler.class);
	    List<IServiceHandler> value = Lists.newArrayList();
	    value.add(mock(IServiceHandler.class));
        when(h.getHandlers()).thenReturn(value);
	    handlers.put("fake", h);
	    Fake f = rpcFactory.getService(Fake.class);
	    String p = "Lex";
	    f.sayHi(p);
	    verify(h, times(1)).invoke(any(Object.class), any(Method.class), any(Object[].class));
	}

}

@RpcConfig(BaseConfiger.DEFAULT)
interface LocalService {

	@RpcMethod(14)
	public int add(int... args);

	@RpcMethod(15)
	public int size(List<String> arg);

	@RpcMethod(16)
	public String tr();
}

interface EfService {

	@RpcMethod(14)
	public int add(int... args);

	@RpcMethod(15)
	public int size(List<String> arg);

	@RpcMethod(16)
	public String tr();
}

@RpcConfig("fake")
interface Fake {
    public void sayHi(String name);
}
