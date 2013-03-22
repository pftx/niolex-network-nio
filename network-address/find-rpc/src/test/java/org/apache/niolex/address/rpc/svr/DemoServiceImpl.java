/**
 * DemoServiceImpl.java
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

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.niolex.address.rpc.DemoService;
import org.apache.niolex.commons.util.DateTimeUtil;


/**
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5
 * @since 2013-1-7
 */
public class DemoServiceImpl implements DemoService {
    
    private int tick;

    /**
     * Override super method
     * @see org.apache.niolex.find.rpc.DemoService#calc(int[])
     */
    @Override
    public int calc(int ...arr) {
        if (arr == null || arr.length == 0) {
            return 0;
        }
        int len = arr.length - 1, k = 0;
        for (int i = 0; i < len; ++i) {
            k += arr[i];
        }
        return k * arr[len];
    }

    /**
     * Override super method
     * @see org.apache.niolex.find.rpc.DemoService#getTime()
     */
    @Override
    public String getTime() {
        try {
            return InetAddress.getLocalHost().getHostAddress() + ":" + DateTimeUtil.formatDate2DateTimeStr();
        } catch (UnknownHostException e) {
            return "N/A:" + DateTimeUtil.formatDate2DateTimeStr();
        }
    }

    /**
     * Override super method
     * @see org.apache.niolex.find.rpc.DemoService#tick()
     */
    @Override
    public void tick(int tk) {
        System.out.println("Tick time - " + (++tick) + " : " + tk);
    }

}
