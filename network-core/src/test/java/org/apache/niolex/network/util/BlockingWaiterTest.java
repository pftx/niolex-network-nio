/**
 * PacketWaiterTest.java
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
package org.apache.niolex.network.util;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.niolex.commons.util.Pair;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-27
 */
public class BlockingWaiterTest {

	/**
	 * Test method for {@link org.apache.niolex.network.util.BlockingWaiter#waitForResult(java.lang.Object, long)}.
	 */
	@Test
	public void testWaitForResult() throws Exception {
		final BlockingWaiter<String> test = new BlockingWaiter<String>();
		final AtomicInteger au = new AtomicInteger(0);
		Thread t = new Thread() {
			public void run() {
				try {
					System.out.println("St " + 10);
					String out = test.initWait("testWaitForResult").waitForResult(100);
					System.out.println(out);
					au.incrementAndGet();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};
		t.start();
		Thread.sleep(10);
		System.out.println("St " + 20);
		boolean b = test.release("testWaitForResult", "Not yet implemented");
		System.out.println("St " + b);
		t.join();
		assertEquals(1, au.intValue());
	}

	/**
	 * Test method for {@link org.apache.niolex.network.util.BlockingWaiter#waitForResult(java.lang.Object, long)}.
	 */
	@Test
	public void testInit() throws Exception {
		final BlockingWaiter<String> test = new BlockingWaiter<String>();
		final AtomicInteger au = new AtomicInteger(0);
		final Thread[] ts = new Thread[10];
		for (int i = 0; i < 10; ++i) {
			final int k = i;
			Thread t = new Thread() {
				public void run() {
					try {
						System.out.println("Init " + k);
						Pair<Boolean, BlockingWaiter<String>.WaitOn> out = test.init("testWaitForResult");
						System.out.println("Init " + k + " " + out.a);
						String s = out.b.waitForResult(100);
						System.out.println(s);
						au.incrementAndGet();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			};
			ts[i] = t;
			t.start();
		}
		Thread.sleep(10);
		System.out.println("Init OK");
		boolean b = test.release("testWaitForResult", "Not yet implemented");
		System.out.println("Released Status " + b);
		for (int i = 0; i < 10; ++i) {
			ts[i].join();
		}
		assertEquals(10, au.intValue());
	}

	/**
	 * Test method for {@link org.apache.niolex.network.util.BlockingWaiter#release(java.lang.Object, org.apache.niolex.network.PacketData)}.
	 */
	@Test
	public void testRelease() throws Exception {
		final BlockingWaiter<String> test = new BlockingWaiter<String>();
		final AtomicInteger au = new AtomicInteger(0);
		final int SIZE = 1024;
		final Thread[] thrs = new Thread[SIZE];
		for (int i = 0; i < SIZE; ++i) {
			final int k = i;
			Thread t = new Thread() {
				public void run() {
					try {
						System.out.println("St " + 10);
						String out = test.waitForResult("1+" + k, 200);
						System.out.println(out);
						au.incrementAndGet();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			};
			t.start();
			thrs[i] = t;
		}
		Thread.sleep(10);
		System.out.println("St " + 20);
		boolean b = test.release("testWaitForResult", "Not yet implemented");
		System.out.println("St " + b);
		for (int i = 0; i < SIZE; ++i) {
			boolean c = test.release("1+" + i, "It's " + i + " good.");
			System.out.println("St " + c);
		}

		for (Thread t : thrs)
			t.join();
		assertEquals(SIZE, au.intValue());
	}


	/**
	 * Test method for {@link org.apache.niolex.network.util.BlockingWaiter#release(java.lang.Object, org.apache.niolex.network.PacketData)}.
	 */
	@Test
	public void testVerse() throws Exception {
		final BlockingWaiter<String> test = new BlockingWaiter<String>();
		BlockingWaiter<String>.WaitOn on = test.initWait("goodmood");
		test.release("goodmood", "@link org.apache.niolex.network.util.Blocking");
		String b = on.waitForResult(200);
		System.out.println("Vt " + b);
	}


	/**
	 * Test method for {@link org.apache.niolex.network.util.BlockingWaiter#release(java.lang.Object, org.apache.niolex.network.PacketData)}.
	 */
	@Test(expected=RuntimeException.class)
	public void testException() throws Exception {
		final BlockingWaiter<String> test = new BlockingWaiter<String>();
		BlockingWaiter<String>.WaitOn on = test.initWait("goodmood");
		test.release("goodmood", new RuntimeException("@link org.apache.niolex.network.util.Blocking"));
		String b = on.waitForResult(200);
		System.out.println("Vt " + b);
	}

	/**
	 * Test method for {@link org.apache.niolex.network.util.BlockingWaiter#release(java.lang.Object, org.apache.niolex.network.PacketData)}.
	 */
	@Test
	public void testExceptionRe() throws Exception {
		final BlockingWaiter<String> test = new BlockingWaiter<String>();
		BlockingWaiter<String>.WaitOn on = test.initWait("goodmood");
		test.release("goodmood3", new RuntimeException("@link org.apache.niolex.network.util.Blocking"));
		String b = on.waitForResult(200);
		System.out.println("Vt " + b);
	}


}
