/**
 * MethodExecuteItem.java
 *
 * Copyright 2016 the original author or authors.
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
package org.apache.niolex.network.rpc.internal;

/**
 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
 * @version 0.6.1
 * @since Aug 5, 2016
 */
public class MethodExecuteItem {

    private final short methodCode;
    private final boolean isOneWay;

    public MethodExecuteItem(short methodCode, boolean isOneWay) {
        super();
        this.methodCode = methodCode;
        this.isOneWay = isOneWay;
    }

    public short getMethodCode() {
        return methodCode;
    }

    public boolean isOneWay() {
        return isOneWay;
    }

}
