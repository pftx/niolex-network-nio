/**
 * RpcInterface.java
 * 
 * Copyright 2012 Niolex, Inc.
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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate the target interface to config some properties for Find-Rpc.
 * This annotation is Required.
 * 
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.5, $Date: 2012-11-30$
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface RpcInterface {

    /**
     * Optionally specify <code>serviceType</code> of the current interface.
     * default to use application/json as the transcoding protocol.
     */
    String serviceType() default "network-json";
    
    /**
     * Optionally specify <code>serviceName</code> of the current interface.
     * default to the current interface name with package together.
     */
    String serviceName() default "";
    
    /**
     * Optionally specify <code>version</code> of the current interface.
     * default to 100.
     */
    int version() default 100;
}
