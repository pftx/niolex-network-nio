/**
 * DemoService.java
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
package org.apache.niolex.address.rpc;

import org.apache.niolex.address.rpc.RpcInterface;
import org.apache.niolex.network.rpc.anno.RpcMethod;

/**
 * Demo for test. Interface.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5
 * @since 2013-1-7
 */
@RpcInterface
public interface DemoService {

    @RpcMethod(1)
    public int calc(int ...arr);

    @RpcMethod(2)
    public String getTime();

    @RpcMethod(3)
    public void tick(int tk);
}
