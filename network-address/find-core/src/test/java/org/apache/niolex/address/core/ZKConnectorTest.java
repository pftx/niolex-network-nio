/**
 * ZKConnectorTest.java
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

import java.io.IOException;

import org.apache.niolex.address.core.FindException;
import org.apache.niolex.address.core.RecoverableWatcher;
import org.apache.niolex.address.core.ZKConnector;
import org.apache.niolex.address.op.StatesMain;
import org.apache.zookeeper.WatchedEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-7-2
 */
public class ZKConnectorTest {

	private ZKConnector zKConnector;
	private String path = "/find/services/org.apache.niolex.address.Test/versions/1/A";

	@Before
	public void createZKConnector() throws Exception {
		zKConnector = new ZKConnector(CoreTest.ZK_ADDR, 5000);
		zKConnector.setRoot("find");
	}

	@After
	public void closeZKConnector() throws Exception {
	    zKConnector.close();
	}

	@Test(expected=IllegalArgumentException.class)
    public void testSmallTimeout() throws IOException {
	    new ZKConnector(CoreTest.ZK_ADDR, 1000);
        fail("Should not create.");
    }

	@Test
	public void testWaitTillDeath() throws IOException {
	    Thread a = new Thread() { public void run() { try {
	    new ZKConnector(CoreTest.ZK_ADDR, 5000);
	    } catch (Exception e) {}}};
	    a.start();
	    a.interrupt();
	    a.interrupt();
	    a.interrupt();
	    a.interrupt();
	    a.interrupt();
	}

	/**
	 * Test method for {@link org.apache.niolex.find.core.ZKConnector#addAuthInfo(java.lang.String, java.lang.String)}.
	 */
	@Test(expected=FindException.class)
	public void testAddAuthInfo() {
		zKConnector.createNode(path + "/testaddr", false);
		fail("Should not create.");
	}

	/**
	 * Test method for {@link org.apache.niolex.find.core.ZKConnector#reconnect()}.
	 */
	@Test(expected=FindException.class)
	public void testCreateNodeNoAuth() {
	    zKConnector.addAuthInfo(StatesMain.CLI_NAME, StatesMain.CLI_PASSWORD);
	    String s = zKConnector.createNode(path + "/TestNode", null, false, true);
	    System.out.println("永久自增节点 " + s);
	    zKConnector.deleteNode(s);
	}

	/**
	 * Test method for {@link org.apache.niolex.find.core.ZKConnector#reconnect()}.
	 */
	@Test
	public void testCreateNodeyt() {
		zKConnector.addAuthInfo(StatesMain.SVR_NAME, StatesMain.SVR_PASSWORD);
		String s = zKConnector.createNode(path + "/TestNode", null, false, true);
		System.out.println("永久自增节点 " + s);
		zKConnector.deleteNode(s);
	}

	@Test
    public void testCreateNodett() {
        zKConnector.addAuthInfo(StatesMain.SVR_NAME, StatesMain.SVR_PASSWORD);
        String s = zKConnector.createNode(path + "/TestNode", null, true, true);
        System.out.println("临时自增节点 " + s);
        zKConnector.deleteNode(s);
    }

	@Test
    public void testCreateNodeyf() {
        zKConnector.addAuthInfo(StatesMain.SVR_NAME, StatesMain.SVR_PASSWORD);
        String s = zKConnector.createNode(path + "/TestNode2", null, false, false);
        System.out.println("永久节点 " + s);
        zKConnector.deleteNode(s);
    }

	@Test
    public void testCreateNodetf() {
        zKConnector.addAuthInfo(StatesMain.SVR_NAME, StatesMain.SVR_PASSWORD);
        String s = zKConnector.createNode(path + "/TestNode3", null, true, false);
        System.out.println("临时节点 " + s);
        zKConnector.deleteNode(s);
    }


    @Test(expected=FindException.class)
    public void testDeleteNode() {
        zKConnector.deleteNode(path + "/TestNode4");
    }

	/**
	 * Test method for {@link org.apache.niolex.find.core.ZKConnector#submitWatcher(java.lang.String, org.apache.zookeeper.Watcher, boolean)}.
	 */
	@Test
	public void testSubmitWatcher() {
		zKConnector.addAuthInfo(StatesMain.SVR_NAME, StatesMain.SVR_PASSWORD);
		RecoverableWatcher wat = new RecoverableWatcher() {

			@Override
			public void process(WatchedEvent event) {
				System.out.println(event);
			}

            @Override
            public void reconnected(String path) {
                System.out.println("Reconnected: " + path);
            }};
		Object event = zKConnector.submitWatcher(path, wat, true);
		System.out.println(event);
	}

	/**
	 * Test method for {@link org.apache.niolex.find.core.ZKConnector#createNode(java.lang.String, boolean)}.
	 */
	@Test(expected=FindException.class)
	public void testWatch() {
		zKConnector.addAuthInfo(StatesMain.SVR_NAME, StatesMain.SVR_PASSWORD);
		RecoverableWatcher wat = new RecoverableWatcher() {

			@Override
			public void process(WatchedEvent event) {
				System.out.println(event);
			}

            @Override
            public void reconnected(String path) {
                System.out.println("Reconnected: " + path);
            }};
		Object event = zKConnector.submitWatcher(path + "/abc", (RecoverableWatcher) wat, false);
		System.out.println(event);
	}

	/**
	 * Test method for {@link org.apache.niolex.find.core.ZKConnector#getRoot()}.
	 */
	@Test
	public void testGetRoot() {
		assertEquals("/find", zKConnector.getRoot());
	}

	@Test
	public void testReconn() {
	    zKConnector.reconnect();
	}
}
