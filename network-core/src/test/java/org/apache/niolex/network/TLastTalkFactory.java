/**
 * TLastTalkFactory.java
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

import org.apache.niolex.commons.concurrent.ThreadUtil;
import org.apache.niolex.network.handler.IHandlerFactory;

/**
 * For test use, when code == 5, we sleep.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-5-31
 */
public class TLastTalkFactory implements IHandlerFactory {

	@Override
    public IPacketHandler createHandler(IPacketWriter wt) {

        return new IPacketHandler() {
            private String lastTalk;

            @Override
            public void handleClose(IPacketWriter wt) {
            }

            @Override
            public void handlePacket(PacketData sc, IPacketWriter wt) {
                String thisTalk = new String(sc.getData(), Config.SERVER_ENCODING);
                if (lastTalk == null) {
                    lastTalk= "Hello, " + wt.getRemoteName();
                }
                sc.setData(lastTalk.getBytes());
                sc.setLength(sc.getData().length);
                System.out.println("Ready.\n------------\nLast talk: "
                		+ lastTalk + "\nThis talk: " + thisTalk + "\n-----------------");
                lastTalk = thisTalk;
                if (sc.getCode() == 5) {
                    ThreadUtil.sleep(CoreRunner.CO_SLEEP);
                }
                if (sc.getCode() == 6) {
                	wt.handleWrite(PacketData.getHeartBeatPacket());
                	wt.handleWrite(PacketData.getHeartBeatPacket());
                }
                System.out.println("Send data now...");
                wt.handleWrite(sc);
            }

        };
	}
}
