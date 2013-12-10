/**
 * CoreTest.java
 *
 * Copyright 2013 The original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.niolex.address.core;

import java.io.IOException;

import org.apache.niolex.address.client.Consumer;
import org.apache.niolex.address.ext.AdvancedProducer;
import org.apache.niolex.address.op.OPMain;
import org.apache.niolex.address.op.ServerMain;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5
 * @since 2013-3-15
 */
public class CoreTest {

    protected static final Logger LOG = LoggerFactory.getLogger(CoreTest.class);

    public static final String ZK_ADDR = "localhost:9181";
    public static final String ZK_ROOT = "find";
    public static final String TEST_SERVICE = "org.apache.niolex.address.Test";
    public static AdvancedProducer PRO_DU;
    public static Consumer CON_SU;

    static {
        try {
            PRO_DU = new AdvancedProducer(ZK_ADDR, 5000);
            PRO_DU.setRoot(ZK_ROOT);
            PRO_DU.addAuthInfo(ServerMain.SVR_NAME, ServerMain.SVR_PASSWORD);
            // --------------------
            CON_SU = new Consumer(ZK_ADDR, 5000);
            CON_SU.setRoot(ZK_ROOT);
            CON_SU.addAuthInfo(OPMain.CLI_NAME, OPMain.CLI_PASSWORD);
        } catch (IOException e) {
            LOG.error("Error occured when create producer.", e);
        }
    }

    public static AdvancedProducer getProducer() {
        return PRO_DU;
    }

    public static Consumer getConsumer() {
        return CON_SU;
    }

    @Test
    public void testLOG() {
        System.out.println("[IN] CoreTest");
    }

}
