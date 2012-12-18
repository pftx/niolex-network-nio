/**
 * BaseClientTest.java
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
package org.apache.niolex.network.client;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.net.Socket;

import org.apache.niolex.network.PacketData;
import org.junit.Test;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5, $Date: 2012-12-4$
 */
public class BaseClientTest {

    /**
     * Test method for {@link org.apache.niolex.network.client.BaseClient#safeClose()}.
     * @throws IOException
     */
    @Test
    public void testSafeClose() throws IOException {
        BaseClient cli = new BaseClient(){

            @Override
            public void connect() throws IOException {
                this.socket = mock(Socket.class);
                doThrow(new IOException("This is test testSafeClose.")).when(socket).close();
            }

            @Override
            public void stop() {
            }

            @Override
            public void handleWrite(PacketData sc) {

            }};
        cli.setServerAddress("localhost:8123");
        cli.connect();
        Exception e = cli.safeClose();
        assertEquals(e.getMessage(), "This is test testSafeClose.");
    }

}
