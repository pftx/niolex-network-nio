/**
 * RpcExecuteItem.java
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
package org.apache.niolex.network.rpc.internal;

import java.lang.reflect.Method;

import org.apache.niolex.network.rpc.svr.RpcPacketHandler;

/**
 * Rpc execute item, internal usage. Store server side method execution information.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0, Date: 2012-6-1
 * @see RpcPacketHandler
 */
public class RpcExecuteItem extends MethodExecuteItem {

	/**
	 * The target method.
	 */
    private final Method method;

	/**
	 * The implementation object.
	 */
    private final Object target;

    /**
     * Create a new Rpc execute item.
     * 
     * @param code the method execution code
     * @param isOneWay whether it's a one way method
     * @param method the method instance
     * @param target the target object used to execute this method
     */
    public RpcExecuteItem(short code, boolean isOneWay, Method method, Object target) {
        super(code, isOneWay);
        this.method = method;
        this.target = target;
    }

    public Method getMethod() {
		return method;
	}

	public Object getTarget() {
		return target;
	}

}
