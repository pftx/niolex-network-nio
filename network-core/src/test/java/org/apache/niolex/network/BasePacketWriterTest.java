/**
 * BasePacketWriterTest.java
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
package org.apache.niolex.network;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-5-30
 */
public class BasePacketWriterTest {
	BasePacketWriter bpw;

	@Before
	public void setup() {
		bpw = new TBasePacketWriter ();
	}

	/**
	 * Test method for {@link org.apache.niolex.network.BasePacketWriter#handleWrite(org.apache.niolex.network.PacketData)}.
	 */
	@Test
	public void testHandleWrite() {
		bpw.handleWrite(null);
	}

	/**
	 * Test method for {@link org.apache.niolex.network.BasePacketWriter#attachData(java.lang.String, java.lang.Object)}.
	 */
	@Test
	public void testAttachData() {
		bpw.attachData("IDIJF", "Not yet implemented");
		assertEquals("Not yet implemented", bpw.getAttached("IDIJF"));
	}

	@Test
	public void testHandleWriteM() throws InterruptedException {
		Runnable r = new Runnable() {

			@Override
			public void run() {
				while (bpw.handleNext() == null) {
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				while (bpw.handleNext() == null) {
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				System.out.println("Out Time: " + System.currentTimeMillis());

			}};
		;
		Thread t0 = new Thread(r);
		Thread t1 = new Thread(r);
		Thread t2 = new Thread(r);
		t0.start();
		t1.start();
		t2.start();
		Thread.sleep(10);
		System.out.println("Inn Time: " + System.currentTimeMillis());
		bpw.handleWrite(null);
		bpw.handleWrite(PacketData.getHeartBeatPacket());
		bpw.handleWrite(null);
		bpw.handleWrite(PacketData.getHeartBeatPacket());
		bpw.handleWrite(null);
		bpw.handleWrite(PacketData.getHeartBeatPacket());
		PacketData pc = new PacketData(4, new byte[4]);
		bpw.handleWrite(null);
		bpw.handleWrite(pc);
		bpw.handleWrite(null);
		bpw.handleWrite(pc);
		bpw.handleWrite(null);
		bpw.handleWrite(pc);
		bpw.handleWrite(null);
		t0.join();
		t1.join();
		t2.join();
	}
}
