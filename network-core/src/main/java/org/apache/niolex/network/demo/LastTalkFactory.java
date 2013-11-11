/**
 * LastTalkFactory.java
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
package org.apache.niolex.network.demo;

import org.apache.niolex.network.IPacketHandler;
import org.apache.niolex.network.IPacketWriter;
import org.apache.niolex.network.PacketData;
import org.apache.niolex.network.handler.IHandlerFactory;

/**
 * Create a LastTalk Handler inside. Used in DemoServer.
 *
 * @author Xie, Jiyun
 *
 */
public class LastTalkFactory implements IHandlerFactory {

	/**
	 * Override super method
	 * @see org.apache.niolex.network.handler.IHandlerFactory#createHandler(org.apache.niolex.network.IPacketWriter)
	 */
    @Override
    public IPacketHandler createHandler(IPacketWriter wt) {
        return new IPacketHandler() {
            private String lastTalk;

            @Override
            public void handleClose(IPacketWriter wt) {
            }

            @Override
            public void handleRead(PacketData sc, IPacketWriter wt) {
                String thisTalk = new String(sc.getData());
                if (lastTalk == null) {
                    lastTalk = "Hello " + wt.getRemoteName() + ", we will do last talk replay.";
                }
                sc.setData(lastTalk.getBytes());
                sc.setLength(sc.getData().length);
                System.out.println("Last talk: " + lastTalk + "\nThis talk: " + thisTalk);
                lastTalk = thisTalk;
                wt.handleWrite(sc);
            }

        };
    }

}
