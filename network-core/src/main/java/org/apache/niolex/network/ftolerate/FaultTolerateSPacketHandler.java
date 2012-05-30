/**
 * FaultTolerateSPacketHandler.java
 *
 * Copyright 2011 Niolex, Inc.
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
package org.apache.niolex.network.ftolerate;

import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.handler.SessionPacketHandler;

/**
 * This is the fault tolerate session packet handler.
 * In this handler, the first connect packet need to be FTSPacket, otherwise it can not handle the
 * fault toleration.
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-5-30
 */
public class FaultTolerateSPacketHandler extends SessionPacketHandler {


	@Override
    public void handleError(IPacketWriter wt) {

	}

}
