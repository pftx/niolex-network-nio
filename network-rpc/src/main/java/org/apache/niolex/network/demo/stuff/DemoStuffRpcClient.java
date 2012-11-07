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

import org.apache.niolex.network.client.PacketClient;
import org.apache.niolex.network.rpc.PacketInvoker;
import org.apache.niolex.network.rpc.RpcClient;
import org.apache.niolex.network.rpc.ser.ProtoStuffConverter;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-11-7
 */
public class DemoStuffRpcClient {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] a) throws IOException {
		PacketClient c = new PacketClient(new InetSocketAddress("localhost", 8808));
		RpcClient client = new RpcClient(c, new PacketInvoker(), new ProtoStuffConverter());
		client.connect();

		final RpcService service = client.getService(RpcService.class);
		IntArray args = new IntArray();
		args.arr = new int[] {3, 6, 8, 10};
		int out = service.add(args);
		System.out.println("Out => " + out + " == 27");
		client.stop();
	}

}
