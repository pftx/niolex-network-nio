/**
 * ClientPoolMain.java
 *
 * Copyright 2013 Niolex, Inc.
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
package org.apache.niolex.address.rpc.cli;

import org.apache.niolex.address.rpc.DemoService;
import org.apache.niolex.commons.util.SystemUtil;

/**
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5
 * @since 2013-1-7
 */
public class ClientPoolMain {

    /**
     * @param args
     */
    public static void main(String[] args) {
        // 配置环境变量
        System.setProperty("zk.cluster.address", "localhost:9181");
        System.setProperty("zk.session.timeout", "30000");
        System.setProperty("zk.root", "find");
        RpcClientFactory factory = new RpcClientFactory("redis-client", "abcde");
        // 准备启动了
        if (!factory.connectToZK()) {
            System.out.println("Failed to connect to ZK.");
            System.exit(-1);
        }
        // 获得远程对象，这里有一个连接池，简单起见我们就直接获得对象了
        BaseStub<DemoService> pool = factory.newBuilder(DemoService.class).buildPool();
        DemoService demo = pool.build().getService();
        // 开始操作
        int c = demo.calc(3, 4, 5);
        if (c != 35) {
            System.out.println("calc 的结果不对: " + c);
        }
        // 在服务器上打印tick - 0 ~ 100
        for (int i = 0; i < 100; ++i) {
            demo.tick(i);
        }
        System.out.println("服务器当前时间: " + demo.getTime());
        if (args == null)
            SystemUtil.sleep(5000);
        for (int i = 0; i < 30; ++i) {
            demo.tick(i);
        }
        c = demo.calc(5, 7 -13, 9, 3);
        System.out.println("服务器当前时间: " + demo.getTime());
        System.out.println("calc[5, 7 -13, 9, 3]的结果(24): " + c);
        // 结束
        factory.disconnectFromZK();
        pool.destroy();
    }

}
