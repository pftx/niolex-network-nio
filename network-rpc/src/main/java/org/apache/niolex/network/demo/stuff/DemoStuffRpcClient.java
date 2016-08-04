/**
 * DemoStuffRpcClient.java
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
package org.apache.niolex.network.demo.stuff;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.niolex.commons.test.Check;
import org.apache.niolex.network.client.PacketClient;
import org.apache.niolex.network.rpc.cli.BaseInvoker;
import org.apache.niolex.network.rpc.cli.BlockingStub;
import org.apache.niolex.network.rpc.conv.ProtoStuffConverter;

/**
 * Demo client
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0, Date: 2012-11-7
 */
public class DemoStuffRpcClient {

	/**
	 * @param a
	 * @throws IOException
	 */
	public static void main(String[] a) throws IOException {
        PacketClient c = new PacketClient(new InetSocketAddress("localhost", 8808));
        BaseInvoker invoker = new BaseInvoker(c);
        invoker.connect();
        BlockingStub client = new BlockingStub(invoker, new ProtoStuffConverter());

		final RpcService service = client.getService(RpcService.class);
		IntArray args = new IntArray();
		args.arr = new int[] {3, 6, 8, 10};
		int out = service.add(args);
		System.out.println("Out => " + out + " == 27");
		Check.eq(27, out, "service.add");
		
		StringArray arg = new StringArray();
		arg.arr = new String[]{"Hi, ", "Lex!"};
        int s = service.size(arg);
        Check.eq(2, s, "service.size");
        
        String q = service.concat(arg);
        Check.eq("Hi, Lex!", q, "service.concat");
        
        invoker.stop();
	}

}
