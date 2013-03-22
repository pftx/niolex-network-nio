/**
 * RpcServerMain.java
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
package org.apache.niolex.address.rpc.svr;

import java.util.Collections;
import java.util.List;

import org.apache.niolex.address.rpc.DemoService;
import org.apache.niolex.address.rpc.svr.RpcExpose;
import org.apache.niolex.address.rpc.svr.RpcServer;


/**
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5
 * @since 2013-1-7
 */
public class RpcServerMain {
    private static int port = 8723;

    /**
     * @param args
     */
    public static void main(String[] args) {
        // 获得要暴露的对象，可以从Spring容器里面拿出来，这里简单new
        DemoService demo = new DemoServiceImpl();
        
        // 配置需要远程暴露的对象，可以一次性暴露一个List
        RpcExpose exp = new RpcExpose(demo);
        exp.setWeight(1);
        List<RpcExpose> exposeList = Collections.singletonList(exp);

        // 准备启动了，这些属性都可以通过相应的setter直接设置
        System.setProperty("zk.cluster.address", "10.22.241.233:8181");
        System.setProperty("zk.session.timeout", "10000");
        System.setProperty("zk.svr.username", "redis");
        System.setProperty("zk.svr.password", "mailto:xiejiyun");
        System.setProperty("zk.root", "dev");
        RpcServer rpc = new RpcServer();
        rpc.setHandlerThreadsNumber(5);
        rpc.setSelectorThreadsNumber(2);
        rpc.setAcceptTimeOut(10000);
        rpc.setPort(port);
        rpc.setExposeList(exposeList);
        if (!rpc.start()) {
            System.out.println("Failed to start rpc server.");
            System.exit(-1);
        } else {
            System.out.println("====================================");
            System.out.println("Server started.");
            System.out.println("====================================");
        }
    }

}
