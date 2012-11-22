/**
 * PacketTranslaterTest.java
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
package org.apache.niolex.config.core;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.niolex.commons.codec.StringUtil;
import org.apache.niolex.config.bean.SyncBean;
import org.apache.niolex.network.PacketData;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5, $Date: 2012-11-21$
 */
public class PacketTranslaterTest {

	/**
	 * Test method for {@link org.apache.niolex.config.core.PacketTranslater#translate(java.util.List)}.
	 */
	@Test
	public void testTranslateListOfSyncBean() {
		SyncBean abc = new SyncBean();
		abc.setGroupName("ggg");
		Map<String, Long> groupData = new HashMap<String, Long>();
		abc.setGroupData(groupData);
		groupData.put("abc", 1277766667777733l);
		byte[] ab = PacketTranslater.translate(Collections.singletonList(abc)).getData();
		String raw = StringUtil.utf8ByteToStr(ab);
		System.out.println(raw);
		assertEquals("[{\"groupName\":\"ggg\",\"groupData\":{\"abc\":1277766667777733}}]", raw);
	}

	/**
	 * Test method for {@link org.apache.niolex.config.core.PacketTranslater#toSyncBean(org.apache.niolex.network.PacketData)}.
	 */
	@Test
	public void testToSyncBean() {
		byte[] ab = StringUtil.strToUtf8Byte("[{\"groupName\":\"ggg\",\"groupData\":{\"abc\":1277766667777733}}]");
		List<SyncBean> aba = PacketTranslater.toSyncBean(new PacketData(5, ab));
		assertEquals(1277766667777733l, aba.get(0).getGroupData().get("abc").longValue());
	}

}
