/**
 * ConsumerTest.java
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
package org.apache.niolex.address.client;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.apache.niolex.address.core.CoreTest;
import org.apache.niolex.address.op.OPMain;
import org.apache.niolex.commons.bean.MutableOne;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-6-25
 */
public class ConsumerTest {

	private Consumer common = CoreTest.CON_SU;

    @Test
    public void testGetCurrentVersionPlus() {
        int k = common.getCurrentVersion(CoreTest.TEST_SERVICE, "1+");
        assertEquals(4, k);
    }

    @Test
    public void testGetCurrentVersionRange() {
        int k = common.getCurrentVersion(CoreTest.TEST_SERVICE, "1-3");
        assertEquals(2, k);
    }

    @Test
    public void testGetCurrentVersionFixed() {
        int k = common.getCurrentVersion(CoreTest.TEST_SERVICE, "3");
        assertEquals(3, k);
    }

	public Consumer createConsumer() throws Exception {
	    Consumer consumer = new Consumer(CoreTest.ZK_ADDR, 5000);
		consumer.addAuthInfo(OPMain.CLI_NAME, OPMain.CLI_PASSWORD);
		return consumer;
	}

	@Test(expected=IllegalStateException.class)
    public void testRoot_Err_0() throws Exception {
	    try {
	        Consumer consumer = createConsumer();
	        consumer.getAddressList(CoreTest.TEST_SERVICE, "1-5", "shard");
	    } catch (Exception e) {throw e;}
	}

	@Test(expected=IllegalArgumentException.class)
	public void testVer_Err_0() throws Exception {
	    try {
	        common.getAddressList(CoreTest.TEST_SERVICE, "1-5+", "shard");
    	} catch (Exception e) {throw e;}
	}

	@Test(expected=IllegalStateException.class)
	public void testVer_Err_01() throws Exception {
	    try {
	        common.getAddressList(CoreTest.TEST_SERVICE, "5+", "shard");
	    } catch (Exception e) {throw e;}
	}

    @Test
    public void testGetAllStats() {
        MutableOne<List<String>> allStats = common.getAllStats(CoreTest.TEST_SERVICE, "1");
        System.out.println("[STATES] " + allStats.data());
        assertEquals(4, allStats.data().size());
    }

    @Test
    public void testGetAddressList() {
        MutableOne<List<String>> addressList = common.getAddressList(CoreTest.TEST_SERVICE, "1", "B");
        System.out.println("[NODES] " + addressList.data());
        assertEquals(3, addressList.data().size());
    }

}
