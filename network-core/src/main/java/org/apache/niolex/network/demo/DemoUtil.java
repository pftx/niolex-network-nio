/**
 * DemoUtil.java
 *
 * Copyright 2013 the original author or authors.
 *
 * We licenses this file to you under the Apache License, version 2.0
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

import org.apache.niolex.commons.codec.IntegerUtil;

/**
 * The demo utility for parsing arguments.
 *
 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2013-11-8
 */
public class DemoUtil {

    public static String HOST = "localhost";
    public static int PORT = 8808;
    public static int TIMEOUT = 600000;
    public static int POOL_SIZE = 0;
    public static int BUF_SIZE = 4096;
    public static int LAST = 0;

    /**
     * Parse the arguments from the command line.
     *
     * @param args the arguments
     */
    public static final void parseArgs(String[] args) {
        if (args == null || args.length == 0) {
            HOST = "localhost";
            PORT = 8808;
            TIMEOUT = 600000;
            POOL_SIZE = 0;
            BUF_SIZE = 4096;
            LAST = -1;
            return;
        }
        final int len = args.length;
        for (int i = 0; i < len; ++i) {
            String s = args[i];
            if (s.length() == 2 && s.charAt(0) == '-') {
                switch(s.charAt(1)) {
                    case 'p':
                        PORT = Integer.parseInt(args[++i]);
                        break;
                    case 'h':
                        HOST = args[++i];
                        break;
                    case 'x':
                    case 'P':
                    case 's':
                        POOL_SIZE = Integer.parseInt(args[++i]);
                        break;
                    case 'k':
                    case 'b':
                        BUF_SIZE = (int) IntegerUtil.fromSize(args[++i]);
                        break;
                    case 't':
                        TIMEOUT = Integer.parseInt(args[++i]);
                        break;
                }
            } else {
                LAST = Integer.parseInt(args[i]);
            }
        }
    }

}
