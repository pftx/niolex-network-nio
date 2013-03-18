/**
 * WatcherItemTest.java
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
package org.apache.niolex.address.core;

import static org.junit.Assert.*;

import org.apache.niolex.address.core.RecoverableWatcher;
import org.apache.niolex.address.core.WatcherItem;
import org.apache.zookeeper.WatchedEvent;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-25
 */
public class WatcherItemTest {

	/**
	 * Test method for {@link org.apache.niolex.find.core.WatcherItem#WatcherItem(java.lang.String, org.apache.zookeeper.Watcher, boolean)}.
	 */
	@Test
	public void testWatcherItem() {
	    RecoverableWatcher df = new RecoverableWatcher() {

			@Override
			public void process(WatchedEvent event) {
				System.out.println(event);
			}

            @Override
            public void reconnected(String path) {
                System.out.println("Reconnected: " + path);
            }};
		String qw = "j098432 0a9d8f";
		WatcherItem i = new WatcherItem(qw , df, false);
		assertEquals(i.getPath(), qw);
		assertEquals(i.getWat(), df);
		assertFalse(i.isChildren());
	}

	/**
	 * Test method for {@link org.apache.niolex.find.core.WatcherItem#getPath()}.
	 */
	@Test
	public void testGetPath() {
	    RecoverableWatcher df = new RecoverableWatcher() {

			@Override
			public void process(WatchedEvent event) {
				System.out.println(event);
			}

            @Override
            public void reconnected(String path) {
                System.out.println("Reconnected: " + path);
            }};
		String qw = "j098432 0a9d8f";
		WatcherItem i = new WatcherItem(qw , df, true);
		assertEquals(i.getWat(), df);
		assertTrue(i.isChildren());
	}

	/**
	 * Test method for {@link org.apache.niolex.find.core.WatcherItem#isChildren()}.
	 */
	@Test
	public void testIsChildren() {
	    RecoverableWatcher df = new RecoverableWatcher() {

			@Override
			public void process(WatchedEvent event) {
				System.out.println(event);
			}

            @Override
            public void reconnected(String path) {
                System.out.println("Reconnected: " + path);
            }};
		String qw = "j098432 0a9d8f";
		WatcherItem i = new WatcherItem(qw , df, true);
		WatcherItem s = new WatcherItem(qw , df, false);
		assertNotSame(i, s);
	}

}
