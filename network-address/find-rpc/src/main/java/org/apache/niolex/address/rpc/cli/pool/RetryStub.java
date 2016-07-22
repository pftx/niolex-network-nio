/**
 * RetryStub.java
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
package org.apache.niolex.address.rpc.cli.pool;

import java.util.HashSet;
import java.util.List;

import org.apache.niolex.address.rpc.cli.BaseStub;
import org.apache.niolex.address.rpc.cli.NodeInfo;
import org.apache.niolex.commons.bean.MutableOne;

/**
 * We use the retry handler to generate client stub.
 * 
 * @author <a href="mailto:xiejiyun@foxmail.com">Xie, Jiyun</a>
 * @version 2.1.2
 * @since Jul 15, 2016
 */
public class RetryStub<T> extends BaseStub<T> {

    /**
     * Constructor
     * @param interfaze
     * @param mutableOne
     */
    public RetryStub(Class<T> interfaze, MutableOne<List<String>> mutableOne) {
        super(interfaze, mutableOne);
        // TODO Auto-generated constructor stub
    }

    /**
     * This is the override of super method.
     * @see org.apache.niolex.address.rpc.cli.BaseStub#markDeleted(java.util.HashSet)
     */
    @Override
    protected void markDeleted(HashSet<NodeInfo> delSet) {
        // TODO Auto-generated method stub
        
    }

    /**
     * This is the override of super method.
     * @see org.apache.niolex.address.rpc.cli.BaseStub#markNew(java.util.HashSet)
     */
    @Override
    protected void markNew(HashSet<NodeInfo> addSet) {
        // TODO Auto-generated method stub
        
    }

    /**
     * This is the override of super method.
     * @see org.apache.niolex.address.rpc.cli.BaseStub#build()
     */
    @Override
    public BaseStub<T> build() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * This is the override of super method.
     * @see org.apache.niolex.address.rpc.cli.BaseStub#destroy()
     */
    @Override
    public void destroy() {
        // TODO Auto-generated method stub
        
    }

}
