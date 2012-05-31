/**
 * TBasePacketWriter.java
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-5-31
 */
public class TBasePacketWriter extends BasePacketWriter {

	/**
	 * Override super method
	 * @see org.apache.niolex.network.IPacketWriter#getRemoteName()
	 */
	@Override
	public String getRemoteName() {
		return "Mock";
	}

	/**
	 * Override super method
	 * @see org.apache.niolex.network.IPacketWriter#getRemainPackets()
	 */
	@Override
	public Collection<PacketData> getRemainPackets() {
		List<PacketData> dest = new ArrayList<PacketData>();
		Collections.copy(dest, this.sendPacketList);
		return dest;
	}

}
